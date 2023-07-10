package com.jedmahonisgroup.gamepoint.ui.deals


import android.annotation.SuppressLint
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.Toolbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jedmahonisgroup.gamepoint.BuildConfig
import com.jedmahonisgroup.gamepoint.GamePointApplication
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.RedeemPointsListener
import com.jedmahonisgroup.gamepoint.adapters.deal.DealDetailAdapter
import com.jedmahonisgroup.gamepoint.model.*
import com.jedmahonisgroup.gamepoint.ui.BaseFragment
import com.jedmahonisgroup.gamepoint.utils.Constants
import com.jedmahonisgroup.gamepoint.utils.Constants.REDEEMED_DEAL
import com.jedmahonisgroup.gamepoint.utils.Constants.TIMER_ACTIVE
import com.jedmahonisgroup.gamepoint.utils.SharedPreferencesGamePointSharedPrefsRepo
import com.jedmahonisgroup.gamepoint.utils.StringFormatter
import com.jedmahonisgroup.gamepoint.utils.StringFormatter.getDate
import com.jedmahonisgroup.gamepoint.utils.StringFormatter.getFormatedTimeStamp
import com.jedmahonisgroup.gamepoint.utils.gamePointSharedPrefsRepo
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.confirm_redeem_dialog.view.*
import kotlinx.android.synthetic.main.error_posting_deal_alert.view.*
import kotlinx.android.synthetic.main.event_expired_error.view.*
import kotlinx.android.synthetic.main.event_expired_error.view.close
import kotlinx.android.synthetic.main.redeem_now_alert.view.*
import org.joda.time.LocalDateTime
import org.joda.time.Period
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.PeriodFormatter
import org.joda.time.format.PeriodFormatterBuilder


class DealDetailFragment : BaseFragment(), RedeemPointsListener {


    private val TAG: String = DealDetailFragment::class.java.simpleName

    private var listener: RedeemDetailsListener? = null
    //UI
    private var mBackButton: ImageButton? = null
    private var mRedeemPoints: Button? = null
    private var mHeaderImage: ImageView? = null
    private var mParentDetail: RelativeLayout? = null
    private var mLogoImageDetail: ImageView? = null
    private var mDealLocationBtn: Button? = null
    private var mDealUserPoints: Button? = null
    private var mDealDescription: TextView? = null
    private var mVisitWebsite: Button? = null
    private var mTitleTextView: TextView? = null
    private var mDealDetailToolbar: Toolbar? = null

    //Share Preference
    private lateinit var sharedPrefsRepo: gamePointSharedPrefsRepo
    private val disposables = CompositeDisposable()
    private val handler = Handler()
    private var runnableCode: Runnable? = null

    //data from activity
    private var mDeal: DealsUi? = null
    private var mToken: String? = null
    private var mUser: UserResponse? = null

    //Recyclerview
    private var recyclerView: androidx.recyclerview.widget.RecyclerView? = null
    private var adapter: DealDetailAdapter? = null

    private var dealDetailEmptyView: RelativeLayout? = null

    private var dealDetailViewModel: DealDetailViewModel? = null
    private val PREFS_FILENAME = "com.jedmahonisgroup.gamepoint"

    //misc
    private var mDealBeingRedeemed: DealModel? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        //val view: View = inflater.inflate(R.layout.fragment_home, container, false);
        super.onCreateView(inflater, container, savedInstanceState)
        val view: View = inflater.inflate(R.layout.fragment_redeem_location, container, false)

        //val view: View = inflater.inflate(R.layout.fragment_redeem_location, container, false)
        dealDetailViewModel = ViewModelProviders.of(this).get(DealDetailViewModel::class.java)
        sharedPreferences = activity?.applicationContext!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

        getEvent()
        sharedPrefsRepo = SharedPreferencesGamePointSharedPrefsRepo.create(requireActivity().baseContext)

        mBackButton = view.findViewById(R.id.back_arrow)
        mHeaderImage = view.findViewById(R.id.headerImage)
        mParentDetail = view.findViewById(R.id.parentDetail)
        mLogoImageDetail = view.findViewById(R.id.logoImageDetail)
        mDealLocationBtn = view.findViewById(R.id.dealLocationBtn)
        getSchoolColor()?.let { mDealLocationBtn!!.setBackgroundColor(it) }
        mDealDescription = view.findViewById(R.id.dealDescription)
        mVisitWebsite = view.findViewById(R.id.visitWebsite)
        getSchoolColor()?.let { mVisitWebsite!!.setTextColor(it) }
        mTitleTextView = view.findViewById(R.id.titleTextViewDealDetail)
        mDealDetailToolbar = view.findViewById(R.id.redeeem_location_toolbar)

        recyclerView = view.findViewById(R.id.dealDetailRv)

        dealDetailEmptyView = view.findViewById(R.id.dealDetailNullView)

        setupUi()
        return view
    }

    override fun onResume() {
        super.onResume()
        setToolbarColors(mDealDetailToolbar!!)
    }

     override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is RedeemDetailsListener) {
            listener = context
        } else {
            throw ClassCastException(requireContext().toString() + " must implement DealDetailFragment")
        }
    }

    private fun getEvent() {
        val gson = Gson()
        val token = requireArguments().getString("token")

        mToken = token

        val deal = requireArguments().getString("event")
        Log.i(TAG, "received deal data: $deal")
        mDeal = gson.fromJson(deal, DealsUi::class.java)
        mUser = GamePointApplication.shared!!.getCurrentUser(requireContext())
    }

    @SuppressLint("SetTextI18n")
    private fun setupUi() {
        if (mDeal != null) {

            setBackgroundImage(mHeaderImage)

            if (!mDeal?.business?.logo.isNullOrEmpty()) {
                Picasso.get()
                        .load(mDeal?.business?.logo)
                        .into(mLogoImageDetail)
            }

            mVisitWebsite?.setOnClickListener {
                val uri = Uri.parse(mDeal?.business?.website) // missing 'http://' will cause crashed
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
            mTitleTextView?.setText("${mDeal?.dealBusinessName} Rewards")

            val sat = mDeal?.deal!!.distinctBy { it.id }
            Log.e("JMG", "sat: " + sat)
            val uniqueDeals: Set<DealModel> = HashSet<DealModel>(sat)
            Log.e("JMG", "uniqueDeals: " + uniqueDeals)
            val act = ArrayList<DealModel>()
            var index = -1
            for (unique in uniqueDeals) {
                index = index + 1
                for (addy in unique.businesses.addresses) {
                    Log.e("JMG", "addy.id: " + addy.id + " mDeal!!.address.id: " + mDeal!!.address.id)
                    if (addy.id == mDeal!!.address.id || !mDeal!!.deal!!.get(index).coupon_code.isNullOrBlank() ) {
                        act.add(unique)

                    }
                }
            }
            Log.e("JMG", "act: " + act)
            var descString = "Only valid at the " + mDeal!!.address.street + " " + mDeal!!.address.city + ", " + mDeal!!.address.state + " " + mDeal!!.address.zipcode
            if (!mDeal!!.deal!!.get(0).coupon_code.isNullOrEmpty()) {
                var webString = "the website"
                if (!!mDeal!!.deal!!.get(0).businesses.website.isNullOrEmpty()) {
                    webString = mDeal!!.deal!!.get(0).businesses.website
                }
                descString = "Visit " + webString + " to redeem."
            }
            mDealDescription!!.text = descString

            val gson = Gson()
            val type = object : TypeToken<ArrayList<CurrentRedeemingDealModel>>() {}.type
            val redeemedStr = sharedPreferences.getString("redeemedStr", "")
            val redeemedDeals = gson.fromJson<ArrayList<CurrentRedeemingDealModel>>(redeemedStr, type)

            Log.e("JMG", "redeemedDeals: " + redeemedDeals)

            displayRecyclerView(act.distinctBy { it.id }, redeemedDeals)
            val isDealBeingRedeemed = sharedPreferences.getBoolean(TIMER_ACTIVE, false)
//            if (isDealBeingRedeemed) {
//                if (mDealBeingRedeemed!=null){
//                    displayRedeemAlertDialog(mDealBeingRedeemed)
//
//                }else{
//                    val gson = Gson()
//                    val dealStr = sharedPreferences.getString("DealBeingRedeemed", "")
//                     mDealBeingRedeemed = gson.fromJson(dealStr, DealModel::class.java)
//                     displayRedeemAlertDialog(mDealBeingRedeemed)
//
//                }
//            }
        }

        setUserPointsText(mDealUserPoints)
        val urlString = "geo:0,0?q=${mDeal?.address?.latitude},${mDeal?.address?.longitude} (${mDeal?.dealBusinessName})"
        mDealLocationBtn?.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW,
                    Uri.parse(urlString))
            startActivity(intent)
        }

        mBackButton?.setOnClickListener {
            listener?.onBackToRedeem()
        }


    }

    private fun launchDirections(longitude: Double, latitude: Double, name: String) {
        mDealLocationBtn?.setOnClickListener {
            Log.e(TAG, "launchDirections clicked")
            val intent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("geo:0,0?q=$latitude,$longitude ($name)"))
            startActivity(intent)
        }

    }

    private fun setBackgroundImage(header: ImageView?) {

        if (!mDeal?.business?.image!!.isNullOrEmpty()) {
            Picasso.get()
                    .load(mDeal?.business?.image)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(header, object : com.squareup.picasso.Callback {
                        override fun onError(e: Exception?) {
                            Log.e(TAG, "No cached images, lead from server instead $e")
                            if (!mDeal?.business?.image.isNullOrEmpty()) {
                                Picasso.get()
                                        .load(mDeal?.business?.image)
                                        .into(header, object : com.squareup.picasso.Callback {
                                            override fun onError(e: Exception?) {
                                                Log.e(TAG, "Error loading deal background image a second time: $e")
                                                Log.e(TAG, "Image Path: ${BuildConfig.BASE_URL + mDeal?.business?.image}")
                                            }

                                            override fun onSuccess() {
                                                //there were images from the cache
                                            }
                                        })
                            }
                        }

                        override fun onSuccess() {
                            //there were images from the cache
                        }
                    })
        }
    }

    private fun setUserPointsText(userPoints: Button?) {
        disposables.add(sharedPrefsRepo.points(Constants.TOTAL_POINTS_KEY).subscribe({
            userPoints?.text = it.toString()
        }, {

            Log.e("Error : ", "", it)
        })!!)
    }

    private fun resultsData(deal: DealModel) {
        dealDetailViewModel?.postedDealSucessfully?.observe(this, Observer {
            //Start time
            if (it != null) {
                Log.i(TAG, "successfully posted deal: $it")
                //Snackbar.make(mParentDetail as View, "Deal Redeemed", Snackbar.LENGTH_LONG).show()
                listener?.onAddBadge()
                sharedPreferences.edit().putBoolean("refreshUserAfterRedeemingADeal", true).apply()
                listener?.refreshUserAfterRedeemingADeal()
                redeemedDeals(deal)


                setupUi()
                displayRedeemAlertDialog(deal)
            }

        })
        dealDetailViewModel?.errorPostingDeal?.observe(this, Observer {
            Log.e(TAG, "problem posting deal: $it")
            //show alert dialog
            errorRedeemingDealAlert(it)
        })
    }

    private fun displayRecyclerView(activeDeals: List<DealModel>, redeemedDeals: ArrayList<CurrentRedeemingDealModel>?) {
        if (!activeDeals.isNullOrEmpty()) {
            //there exists deals
            val newDealList = ArrayList<DealModel>()
            for (deal in activeDeals) {
                val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                val dt = formatter.parseDateTime(StringFormatter.getDate().toString())
                val now: LocalDateTime = LocalDateTime(dt)

                if (!redeemedDeals.isNullOrEmpty()) {
                    for (redeemedDeal in redeemedDeals) {
                        val expTime = StringFormatter.getFormatedTimeStamp(redeemedDeal.expireTimeStamp)

                        if (deal.id == redeemedDeal.dealId && mDeal!!.address.id == redeemedDeal.addressId && now.isBefore(expTime) ) {
                            //this is the deal being redeemed set bool to true
                           deal.isBeingRedeemed = true
                     }
                    }
                }
                newDealList.add(deal)
            }
            adapter = DealDetailAdapter(this@DealDetailFragment)
            recyclerView?.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.activity)
            recyclerView?.adapter = adapter
            adapter!!.refreshList(newDealList)

        } else {
            //no deals found
            adapter = DealDetailAdapter(this@DealDetailFragment)
            recyclerView?.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.activity)
            recyclerView?.adapter = adapter
            adapter!!.refreshList(activeDeals)

        }


    }

    private fun redeemNowAlert(deal: DealModel) {
        var infoText: String = if (deal.coupon_code.isNullOrEmpty()){
            "Are you sure you would like to spend ${deal.point_value} points to get ${deal.name}? You MUST be at this location to redeem. Once used, your points will be deducted."
        }else{
            "Are you sure you want to redeem this deal? Once you select redeem your points will be deducted and you will have 24 hours to redeem this coupon code upon check out on their online store."
        }
        Log.e("JMG", "deal.subtitle: " + deal.subtitle)
        if (!deal.subtitle.isNullOrEmpty()) {
            infoText = deal.subtitle
        }

        val mDialogView = LayoutInflater.from(activity).inflate(R.layout.redeem_now_alert, null)
        val mBuilder = activity?.let { it1 ->
            AlertDialog.Builder(it1)
                    .setView(mDialogView)
        }

        val mAlertDialog = mBuilder?.show()
        val mRedeemNow: TextView = mAlertDialog?.findViewById(R.id.redeemNowInfo)!!
        val mDealDescription: TextView = mAlertDialog.findViewById(R.id.dealDescription)!!
        mRedeemNow.text = infoText
        mDealDescription.text = deal.description
        mAlertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        mDialogView.redeem.setOnClickListener {
            mAlertDialog.dismiss()
            redeemDeal(deal)
        }
        mDialogView.cancel.setOnClickListener {
            mAlertDialog.dismiss()

        }
    }

    private fun saveDealInfo() {
        val gson = Gson()
        val str = gson.toJson(mDeal)

        val editor = sharedPreferences.edit()
        editor.putString(REDEEMED_DEAL, str)
        editor.apply()
    }

    private fun redeemTimestampString(deal: DealModel?, redeemedDeals: ArrayList<CurrentRedeemingDealModel>): String? {
        var redeemTimestamp: String? = null

        for (d in redeemedDeals) {
            if (d.addressId == mDeal!!.address.id && d.dealId == deal!!.id) {
                //this deal is being saved
                redeemTimestamp = d.redeemTimeStamp
            }
        }
        return redeemTimestamp
    }

    private fun expireTimeStampString(deal: DealModel?, redeemedDeals: ArrayList<CurrentRedeemingDealModel>): String? {
        var expireTimeStamp: String? = null

        for (d in redeemedDeals) {
            if (d.addressId == mDeal!!.address.id && d.dealId == deal!!.id) {
                //this deal is being saved
                expireTimeStamp = d.expireTimeStamp
            }
        }

        return expireTimeStamp
    }

    private fun displayRedeemAlertDialog(deal: DealModel?) {
        saveDealInfo()
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        val dt = formatter.parseDateTime(getDate().toString())
        val now: LocalDateTime = LocalDateTime(dt)

        val gson = Gson()
        val type = object : TypeToken<ArrayList<CurrentRedeemingDealModel>>() {}.type
        val redeemedStr = sharedPreferences.getString("redeemedStr", "")
        val redeemedDeals = gson.fromJson<ArrayList<CurrentRedeemingDealModel>>(redeemedStr, type)


        if (!redeemedDeals.isNullOrEmpty()) {
            try {
                var infoText = "Show your phone to the cashier to complete the transaction"
                if (!deal?.coupon_code.isNullOrEmpty()) {
                    infoText = "Use this code upon check out on their online store."
                }

                val mDialogView = LayoutInflater.from(activity).inflate(R.layout.confirm_redeem_dialog, null)
                val mBuilder = activity?.let { it1 -> AlertDialog.Builder(it1).setView(mDialogView) }
                val mAlertDialog = mBuilder?.show()

                val mTitle: TextView = mAlertDialog?.findViewById(R.id.titleCheckedIN)!!
                val mCoupon: TextView = mAlertDialog.findViewById(R.id.dealCoupon)!!
                val info: TextView = mAlertDialog.findViewById(R.id.infoCheckedIn)!!
                val timerText: TextView = mAlertDialog.findViewById(R.id.timer)!!
                val checkmarkInCircleImageView: ImageView = mAlertDialog.findViewById(R.id.checkmark_in_circle_image_view)!!
                val completeTransactionButton: Button = mAlertDialog?.findViewById(R.id.completeTransaction)!!
                if (!deal?.coupon_code.isNullOrEmpty()) {
                    completeTransactionButton.text = "Visit Website"
                } else {
                    completeTransactionButton.text = "Complete Transaction"
                }
                if (!deal?.subtitle.isNullOrEmpty()) {
                    infoText = deal!!.subtitle
                    mTitle.visibility = View.GONE
                    checkmarkInCircleImageView.visibility = View.VISIBLE
                    timerText.visibility = View.INVISIBLE
                    mCoupon.visibility = View.INVISIBLE
                    completeTransactionButton.text = "Complete Transaction"
                }

                info.text = infoText
                mTitle.text = deal?.name
                mCoupon.text = deal?.coupon_code

                val expTime = getFormatedTimeStamp(expireTimeStampString(deal, redeemedDeals).toString())
                val redTime = getFormatedTimeStamp(redeemTimestampString(deal, redeemedDeals).toString())

                if (now.isAfter(redTime) && now.isBefore(expTime) && deal?.subtitle.isNullOrEmpty()) {
                    runnableCode = object : Runnable {
                        override fun run() {
                            val mNow = formatter.parseDateTime(getDate().toString()).toLocalDateTime()

                            val period = Period(mNow, expTime)
                            val periodFormatter: PeriodFormatter = PeriodFormatterBuilder()
                                    .printZeroAlways()
                                    .minimumPrintedDigits(2)
                                    .appendHours().appendSeparator(":")
                                    .appendMinutes().appendSeparator(":")
                                    .appendSeconds()
                                    .toFormatter() // produce

                            Log.i(TAG, "Every second on the second: ${periodFormatter.print(period)}")
                            timerText.text = periodFormatter.print(period)
                            Log.e(TAG, "timer text ${periodFormatter.print(period)}")
                            handler.postDelayed(this, 1000)
                        }
                    }
                    handler.post(runnableCode as Runnable)
                } else {
                    Log.e(TAG, "time is expired")
                    //should remove this deal from the db
                    if (deal?.subtitle.isNullOrEmpty()) {
                        removeDealFromSp(deal)
                    }
                }
                mAlertDialog.setCanceledOnTouchOutside(true)
                mAlertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                //  mAlertDialog.setCancelable(false)
                completeTransactionButton.setOnClickListener {
                    Log.e("JMG", "should remove badges")

                    try {
                        handler.removeCallbacks(runnableCode!!)
                    } catch (e: Exception) {
                        Log.e("JMG", "e; DealDetailFragment: " + e.localizedMessage)
                    }
                    handler.removeMessages(1)

                    listener?.onRemoveBadge()

                    val editor = sharedPreferences.edit()
                    editor.putBoolean(TIMER_ACTIVE, false)
                    editor.apply()

                    if (!deal?.coupon_code.isNullOrEmpty() && deal?.subtitle.isNullOrEmpty()) {
                        val i = Intent(Intent.ACTION_VIEW)
                        i.data = Uri.parse(deal!!.url)
                        startActivity(i)
                    } else {
                        removeDealFromSp(deal)
                    }

                    mAlertDialog.dismiss()
                }

                mAlertDialog.setOnCancelListener {
                    //when the alert dialog is closed. stop the thread stuff.
                    try {
                        handler.removeCallbacks(runnableCode!!)
                    } catch (e: Exception) {
                        Log.e("JMG", "e dealDetail 536: " + e.localizedMessage)
                    }
                }

            } catch (e: java.lang.NullPointerException) {
                Log.e(TAG, "time was null $e")
            }
        } else {
            Log.e(TAG, " displayRedeemAlertDialog: we have a miracle, this was not supposed to be null ")
        }
    }

    private fun removeDealFromSp(deal: DealModel?) {
        //update the recyclerview because this deal is no longer redeemable
        val newList = ArrayList<DealModel>()
        for (d in mDeal?.deal!!) {
            if (d == deal) {
                d.isBeingRedeemed = false
            }
            newList.add(d)
        }
        adapter?.refreshList(newList)
        adapter?.notifyDataSetChanged()

        //read from sp
        val gson = Gson()
        val type = object : TypeToken<ArrayList<CurrentRedeemingDealModel>>() {}.type
        val redeemedStr = sharedPreferences.getString("redeemedStr", "")
        val redeemedDeals = gson.fromJson<ArrayList<CurrentRedeemingDealModel>>(redeemedStr, type)

        //remove this deal from the redeemed deals list because its expired
        val updatedList = ArrayList<CurrentRedeemingDealModel>()
        val iterator = redeemedDeals.iterator()
        for (i in iterator) {
            for (address in deal!!.businesses.addresses) {
                if (i.dealId == deal.id && (i.addressId == address.id || !deal.coupon_code.isNullOrBlank())) {
                    Log.e("JMG", "568")
                    iterator.remove()
                } else {
                    Log.e("JMG", "571")
                    updatedList.add(i)
                }
            }
        }
//        for (redeemedDeal in redeemedDeals){
//            for (address in deal!!.businesses.addresses){
//                if (redeemedDeal.dealId == deal!!.id && redeemedDeal.addressId == address.id){
//                    //this deal cannot be redeemed anymore. remove it
//                    redeemedDeals.remove(redeemedDeal)
//                }
//
//            }
//            //might need to remove this later because duhh
//            updatedList.add(redeemedDeal)
//        }

        //write new list to shared prefs
        val updatedListStr = gson.toJson(redeemedDeals)

        val editor = sharedPreferences.edit()
        editor.putString("redeemedStr", updatedListStr)
        editor.apply()
        setupUi()
    }

    private fun redeemDeal(deal: DealModel) {
        try {
            val dm = DealBody(
                    user_id = mUser!!.id,
                    deal_id = deal.id
            )

            val gson = Gson()
            Log.i(TAG, "deal body ====> ${gson.toJson(dm)}")
            dealDetailViewModel?.postDeal(mToken!!, dm)
            resultsData(deal)
        } catch (e: NullPointerException) {
            Log.e(TAG, "token was null")
            Snackbar.make(mParentDetail as View, "something went wrong, try again later", Snackbar.LENGTH_LONG).show()
            //show user the token was null
        }
    }

    private fun errorRedeemingDealAlert(it: String?) {
        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(context).inflate(R.layout.error_posting_deal_alert, null)
        //AlertDialogBuilder
        mDialogView.info.text = it
        val mBuilder = this.let {
            AlertDialog.Builder(requireContext())
                    .setView(mDialogView)
        }
        //show dialog
        val mAlertDialog = mBuilder?.show()
        mAlertDialog!!.setMessage(it)
        mAlertDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        //login button cick of custom layout
        mDialogView.close.setOnClickListener {
            Log.e(TAG, "mAlertDialog?.dismiss(): $it")
            mAlertDialog?.dismiss()


        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
        try {
            handler.removeCallbacks(runnableCode!!)
            handler.removeMessages(1)
        } catch (e: Exception) {
            Log.e("JMG", "e found onDestory DealDetailFrag: " + e.localizedMessage)
        }

    }

    interface RedeemDetailsListener {
        fun onBackToRedeem()
        fun onAddBadge()
        fun onRemoveBadge()
        fun refreshUserAfterRedeemingADeal()
    }

    override fun onRedeemPointsClicked(deal: DealModel) {
//        if (deal.isBeingRedeemed){
//        }else{
//
//        }
        // displayRedeemAlertDialog(deal)
        try {
            if (mUser!!.redeemable.toInt() < deal.point_value) {
                notEnoughPoints()
            } else {
                saveDealBeingRedeemed(deal)
                redeemNowAlert(deal)
            }
        } catch (e: Exception) {
//            saveDealBeingRedeemed(deal)
//            redeemNowAlert(deal)
            Toast.makeText(context, "Error redeeming: " + e.localizedMessage, Toast.LENGTH_LONG)
        }

//        val isDealBeingRedeemed = sharedPreferences.getBoolean(TIMER_ACTIVE, false)
//        if (!isDealBeingRedeemed) {
//            //redeemNowAlert(deal)
//            mDealBeingRedeemed = deal
//        } else {
//           //i guess we wount be display things anymore
//            // displayRedeemAlertDialog(deal)
//        }

    }

    fun notEnoughPoints () {
        val mDialogView = LayoutInflater.from(context).inflate(R.layout.event_expired_error, null)
        //AlertDialogBuilder
        val mBuilder = this.let { it1 ->
            AlertDialog.Builder(requireContext())
                    .setView(mDialogView)
        }
        val mInfo = mDialogView.findViewById<TextView>(R.id.eventDetailAlertInfo)
        mInfo.text = "You don't have enough points to redeem this deal. You can earn points by going to events and picking teams."
        //show dialog
        val mAlertDialog = mBuilder?.show()
        mAlertDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        //login button cick of custom layout
        mDialogView.close.setOnClickListener {
            mAlertDialog?.dismiss()

        }
    }

    override fun onActiveDealClicked(deal: DealModel) {
        //launch alert
        displayRedeemAlertDialog(deal)
        Log.e(TAG, "activating already deal $deal")
    }

    private fun redeemedDeals(deal: DealModel) {
        //only saving the address id's and the deal ids
        val addressArray = ArrayList<Int>()
        val idArray = ArrayList<Int>()

        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        val dtCheckIn = formatter.parseDateTime(getDate().toString())

        val redeemTimeStamp = dtCheckIn.toDateTime().toString()
        val expireTimestamp: String
        expireTimestamp = if (deal.coupon_code.isNullOrEmpty()) {
            //there is no coupon only add 1 hour
            dtCheckIn.plusHours(1).toString()

        } else {
            //there is a coupon so this means it is an online code only
            dtCheckIn.plusHours(24).toString()
        }
        addressArray.add(mDeal!!.address.id)
        idArray.add(deal.id)

        val gson = Gson()
        val type = object : TypeToken<ArrayList<CurrentRedeemingDealModel>>() {}.type
        val redeemedStr = sharedPreferences.getString("redeemedStr", "")
        val redeemedDeals = gson.fromJson<ArrayList<CurrentRedeemingDealModel>>(redeemedStr, type)

        val currentRedeemed = ArrayList<CurrentRedeemingDealModel>()
        if (!redeemedStr.isNullOrEmpty()) {
            //there are no deals in the queue
            val currentRedeemModel = CurrentRedeemingDealModel(
                    redeemTimeStamp = redeemTimeStamp,
                    expireTimeStamp = expireTimestamp,
                    addressId = (mDeal!!.address.id),
                    dealId = deal.id)
            //avoid adding duplicate
            if (!redeemedDeals.contains(currentRedeemModel)) {
                redeemedDeals.add(currentRedeemModel)
                saveRedeemedDealList(redeemedDeals)
            } else {
                Log.e(TAG, "oops, user tried to redeeme the same deal twice.")
            }


        } else {
            //there are deals in the queue, so add this one to that queue
            // val currentRedeemModel = CurrentRedeemingDealModel(addressArray = addressArray, dealIdArray = idArray)
            val currentRedeemModel = CurrentRedeemingDealModel(
                    redeemTimeStamp = redeemTimeStamp,
                    expireTimeStamp = expireTimestamp,
                    addressId = (mDeal!!.address.id),
                    dealId = deal.id)
            currentRedeemed.add(currentRedeemModel)
            saveRedeemedDealList(currentRedeemed)
        }
    }

    private fun saveRedeemedDealList(redemptionList: ArrayList<CurrentRedeemingDealModel>) {
        val gson = Gson()
        val redemptionStr = gson.toJson(redemptionList)

        val editor = sharedPreferences.edit()
        editor.putString("redeemedStr", redemptionStr)
        editor.apply()
    }

    private fun saveDealBeingRedeemed(deal: DealModel) {
        val gson = Gson()
        val dealStr = gson.toJson(deal)
        val editor = sharedPreferences.edit()
        editor.putString("DealBeingRedeemed", dealStr)
        editor.apply()
    }


}




