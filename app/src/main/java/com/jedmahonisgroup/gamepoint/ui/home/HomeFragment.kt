package com.jedmahonisgroup.gamepoint.ui.home

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jedmahonisgroup.gamepoint.GamePointDealDetailListener
import com.jedmahonisgroup.gamepoint.GamePointResultListener
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.adapters.DayAdapter
import com.jedmahonisgroup.gamepoint.adapters.deal.DealAdapter
import com.jedmahonisgroup.gamepoint.adapters.deal.HomeDealAdapter
import com.jedmahonisgroup.gamepoint.helpers.Extensions.hide
import com.jedmahonisgroup.gamepoint.helpers.Extensions.show
import com.jedmahonisgroup.gamepoint.helpers.SharedPreferencesHelper
import com.jedmahonisgroup.gamepoint.injection.ViewModelFactory
import com.jedmahonisgroup.gamepoint.model.*
import com.jedmahonisgroup.gamepoint.model.events.Address
import com.jedmahonisgroup.gamepoint.model.events.EventsModel
import com.jedmahonisgroup.gamepoint.model.gameshow.GameShow
import com.jedmahonisgroup.gamepoint.ui.BaseFragment
import com.jedmahonisgroup.gamepoint.ui.deals.DealsViewModel
import com.jedmahonisgroup.gamepoint.ui.events.EventViewModel
import com.jedmahonisgroup.gamepoint.ui.pickEm.PickEmViewModel
import com.jedmahonisgroup.gamepoint.utils.LogUtil
import com.jedmahonisgroup.gamepoint.utils.StringFormatter
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.imgThumbnail
import kotlinx.android.synthetic.main.fragment_home.txtDate
import kotlinx.android.synthetic.main.fragment_home.txtTime
import kotlinx.android.synthetic.main.fragment_home.youtubePlayerView

import kotlinx.android.synthetic.main.fragment_redeem.*
import org.joda.time.LocalDateTime

import org.joda.time.Period
import org.joda.time.format.DateTimeFormat
import java.net.URL
import javax.inject.Inject

/**
 * Created by nienle on 18,January,2023
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
class HomeFragment : BaseFragment() {

    interface HomeFragmentItemClickCallback{
        fun onPickEmItemClicked(data: Pair<Int, GameShow>)
        fun onEventClicked(data: EventsModel)
        fun onRewardClicked(data: DealsUi)
        fun onViewMoreEventBtnClicked()
        fun onViewMoreRewardBtnClicked()
    }

    private lateinit var clickCallback: HomeFragmentItemClickCallback
    private lateinit var eventViewModel: EventViewModel
    private lateinit var gameShowViewModel: PickEmViewModel
    private lateinit var rewardViewModel: DealsViewModel

    private lateinit var youtubePlayer : YouTubePlayer

    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    companion object {
        fun newInstance(callback: HomeFragmentItemClickCallback) : HomeFragment {
            val f = HomeFragment()
            f.clickCallback =callback
            return f
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val rootView = inflater.inflate(R.layout.fragment_home, null)

        sharedPreferencesHelper = SharedPreferencesHelper(requireContext().getSharedPreferences("com.jedmahonisgroup.gamepoint", Context.MODE_PRIVATE), Gson())

        //viewModel = ViewModelProvider(activity as AppCompatActivity)[PickEmViewModel::class.java]
        gameShowViewModel = ViewModelProviders.of(this,
            ViewModelFactory(this.activity as AppCompatActivity))[PickEmViewModel::class.java]
        eventViewModel = ViewModelProviders.of(this,
            ViewModelFactory(this.activity as AppCompatActivity))[EventViewModel::class.java]
        rewardViewModel = ViewModelProviders.of(this,
            ViewModelFactory(this.activity as AppCompatActivity))[DealsViewModel::class.java]



        gameShowViewModel.gameShowFromServer.observe(viewLifecycleOwner) {
            handleGameShow(it)
        }

        rewardViewModel.dealsData.observe(viewLifecycleOwner) {
            handleDeals(it)
        }

        eventViewModel.eventsFromServer.observe(viewLifecycleOwner) {
            handleEvents(it)
        }


        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swiper.isEnabled = false

        btnViewAllEvents.setOnClickListener { clickCallback.onViewMoreEventBtnClicked() }
        btnViewAllRewards.setOnClickListener { clickCallback.onViewMoreRewardBtnClicked() }

        youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener(){
            override fun onReady(youTubePlayer: YouTubePlayer) {

                // youTubePlayer.cueVideo("J6eWe4b_Vpo",0f)
                this@HomeFragment.youtubePlayer = youTubePlayer
                getData()

            }
        })

    }

    override fun onResume() {
        super.onResume()
        val token = requireArguments().getString("token")
        LogUtil.d("HomeFragment > onResume > 118: $token")
    }


    private fun handleGameShow(data: StateNotifier<Pair<Int, GameShow>>){
        try{

            if (data.state != State.SUCCESS){
                containerGameShow.hide()
                return
            }
            containerGameShow.show()

            val game = data.data?.second ?: return
            if (game.video.isNotEmpty()){
                val queries = StringFormatter.splitQuery(URL(game.video))
                LogUtil.d("PickEmFragment > showContent > 104: ${queries.get("v")}")
                if (queries.containsKey("v")) {
                    youtubePlayer.cueVideo(queries["v"]!!, 0f)
                }
                youtubePlayerView.visibility = View.VISIBLE
                imgThumbnail.visibility = View.GONE
            }else if (game.photo.isNotEmpty()){

                youtubePlayerView.visibility = View.GONE
                imgThumbnail.visibility = View.VISIBLE
                Picasso.get().load(game.photo).into(imgThumbnail)
            }else {

                youtubePlayerView.visibility = View.GONE
                imgThumbnail.visibility = View.GONE
            }

            val gameTime = Period(game.endDate, game.startDate)

            txtDate.text = game.startDate.toString(DateTimeFormat.forPattern("E d MMM"))
            txtTime.text = game.endDate.toString(DateTimeFormat.forPattern("E d MMM"))
            txtGameName.text = game.name
            //txtGameDescription.text = game.description
            btnPickEm.setOnClickListener { 
                LogUtil.d("HomeFragment > handleGameShow > 182: ")
                clickCallback.onPickEmItemClicked(data.data) 
            }
        }catch(t: Throwable){
            LogUtil.e("HomeFragment > handleGameShow > 79: ${t.localizedMessage}")
        }
    }



    private fun handleDeals(data: List<DealModel>){
        LogUtil.d("HomeFragment > handleDeals > 166: ${data.size}")
        val test = makeDealsUiList(data)
        val dealData = dealDataFromResponse(test)

        val splitDealData = if (dealData.size <= 2) dealData else dealData.subList(0,2)
        rv_rewards.apply {
            //set a linear layout manager
            layoutManager = LinearLayoutManager(context)
            adapter = HomeDealAdapter(splitDealData, object : GamePointDealDetailListener{
                override fun onLaunchDetailScreen(data: DealsUi) {
                    clickCallback.onRewardClicked(data)
                }

            }, getSchoolColorAsString())
        }
    }

    private fun removeExpiredDealsFromSp(dealData: List<DealsUi>): ArrayList<CurrentRedeemingDealModel>? {
        val gson = Gson()
        val type = object : TypeToken<ArrayList<CurrentRedeemingDealModel>>() {}.type
        val redeemedStr = sharedPreferences.getString("redeemedStr", "")
        val redeemedDeals = gson.fromJson<ArrayList<CurrentRedeemingDealModel>>(redeemedStr, type)

        //time right now
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        val dt = formatter.parseDateTime(StringFormatter.getDate().toString())
        val now: LocalDateTime = LocalDateTime(dt)

        val newRedeemedList = ArrayList<CurrentRedeemingDealModel>()
        return if (!redeemedStr.isNullOrEmpty()){
            //so in this case, there exist deals
            val iterator = redeemedDeals.iterator()
            for (i in iterator){
                val expTime = StringFormatter.getFormatedTimeStamp(i.expireTimeStamp)
                if (now.isAfter(expTime)){
                    //deal is expired please remove it
                    iterator.remove()
                }
            }
            redeemedDeals
        }else{
            //in this case there are zero deals in the sp so just return the original list
            redeemedDeals
        }
    }


    private fun dealDataFromResponse(dealData: List<DealsUi>): ArrayList<DealsUi> {
        val redeemedDeals = removeExpiredDealsFromSp(dealData)
        val newList = ArrayList<DealsUi>()
        if (!redeemedDeals.isNullOrEmpty()) {

            //find where the address id's and
            for (dealsUi in dealData) {
                //these are the deals we have
                for (liveDeal in dealsUi.deal!!) {
                    //these are the deals being redeemed
                    for (currentRedeeming in redeemedDeals){
                        if (dealsUi.address.id == currentRedeeming.addressId && liveDeal.id == currentRedeeming.dealId) {
                            dealsUi.activeDealCount = dealsUi.activeDealCount.inc()

                        }
                    }
                }
                newList.add(dealsUi)
            }
            return newList
        } else {
            return ArrayList(dealData)
        }
    }

    private fun dealsonlineUi(address: Address, dealsUi: DealsUi?, active: ArrayList<DealModel>, d: DealModel): DealsUi {
        var dealsUi1 = dealsUi
        val businessLocation: Location = Location("")
        val userLocation: Location = Location("")
        dealsUi1 = DealsUi(
            deal = ArrayList(active.distinctBy { it.id }),
            business = d.businesses,
            address = address,
            //addressId = addresses.id,
            dealBusinessName = d.businesses.name,
            distance = (userLocation.distanceTo(businessLocation) * 0.000621371192).toInt().toString(),
            numberOfDeals = 2,
            hero_image = d.hero_image,
            activeDealCount = 0,
            online = true
        )
        return dealsUi1
    }
    private fun makeDealsUiList(deal: List<DealModel>): ArrayList<DealsUi> {
        var dealsUi: DealsUi? = null
        //this holds the new lists
        val dealsUiList: ArrayList<DealsUi> = ArrayList()
        //keeps track of the business ids
        val murArrayList: ArrayList<String> = ArrayList()
        val onlineArrayList: ArrayList<String> = ArrayList()
        val active: ArrayList<DealModel> = ArrayList()
        var d: DealModel?
        for (i in 0 until deal.size) {
            if (true){
                if (!deal[i].coupon_code.isNullOrEmpty()){
                    d = deal[i]
                    active.add(d)
                    val address = com.jedmahonisgroup.gamepoint.model.events.Address(1, "Online", "", 0, "", d.business_id.toString(), 0, 0.0, 0.0,
                        com.jedmahonisgroup.gamepoint.model.events.State(0, "", "")
                    )
                    dealsUi = dealsonlineUi(address, dealsUi, active, d)
                    if (!onlineArrayList.contains(d.business_id.toString())) {
                        onlineArrayList.add(d.business_id.toString())
                        dealsUiList.add(dealsUi)
                    } else {
                        for (i in 0 until dealsUiList.size){
                            if (dealsUiList[i].business.id == d.business_id){
                                dealsUiList[i].deal!!.add(d)
                                dealsUiList[i].numberOfDeals++
                                Log.e("deal", " " + dealsUiList[i].deal!!.size)
                            }
                        }
                    }
                }
            }
        }
        return dealsUiList
    }



    private var adapter: DayAdapter? = null
    private fun handleEvents(data: List<EventsModel>){
        LogUtil.d("HomeFragment > handleEvents > 173: ${data.size}")
        val timeStamps = ArrayList<String>()
        val dayList = ArrayList<Int>()


        for (event in data) {
            val startTime = StringFormatter.convertTimestampToDate(event.startTime)

            timeStamps.add(startTime)
        }

        val uniqueTimeStamps: Set<String> = HashSet<String>(timeStamps)

        for (unique in uniqueTimeStamps) {
            dayList.add(StringFormatter.getDayOfYear(unique))
        }


        //sort the dayList in accending order
        val original: Array<Int> = dayList.toList().toTypedArray()
        val sortedArray: Array<Int> = original.sortedArray()
        adapter = DayAdapter(object : GamePointResultListener {
            override fun onCardClicked(data: EventsModel) {
                clickCallback.onEventClicked(data)
            }

        }, getSchoolColorAsString(),getSchoolSecondaryColorAsString())
        rv_events?.layoutManager = LinearLayoutManager(this.activity)
        rv_events?.adapter = adapter
        adapter!!.refreshList(sortedArray, data)
    }


    private fun getData() {
        val token = sharedPreferencesHelper.getAccountToken()
        LogUtil.d("HomeFragment > getData > 54: $token")
        try {
            if (token != null) {
                gameShowViewModel.getActiveGameShow(token, false)
                eventViewModel.getEventsData(token)
                rewardViewModel.getDeals(token)
            }
        } catch (e: Exception) {
            LogUtil.e("HomeFragment > getData > 61: ${e.localizedMessage}")
        }
    }
}