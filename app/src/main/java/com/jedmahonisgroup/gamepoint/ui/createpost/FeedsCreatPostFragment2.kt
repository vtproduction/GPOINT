package com.jedmahonisgroup.gamepoint.ui.createpost

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.jedmahonisgroup.gamepoint.GamePointApplication
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.adapters.createpostgallery.CreatePostEventAdapter
import com.jedmahonisgroup.gamepoint.adapters.feeds.FeedsAdapter
import com.jedmahonisgroup.gamepoint.injection.ViewModelFactory
import com.jedmahonisgroup.gamepoint.model.*
import com.jedmahonisgroup.gamepoint.model.events.EventsModel
import com.jedmahonisgroup.gamepoint.model.events.EventsModelUi
import com.jedmahonisgroup.gamepoint.model.feeds.PostsModel
import com.jedmahonisgroup.gamepoint.utils.onItemClick
import kotlinx.android.synthetic.main.fragment_feeds.*
import kotlinx.android.synthetic.main.fragment_feeds_creat_post_2ndfragment.*
import kotlinx.android.synthetic.main.fragment_feeds_view_profile.*
import kotlinx.android.synthetic.main.fragment_feeds_view_profile.feedsNoDataView
import java.io.ByteArrayOutputStream
import java.util.ArrayList


class FeedsCreatPostFragment2 : Fragment() {

    private var lastposition: Int = 0
    private var token: String? = null

    private var mbutton: ImageButton? = null

    private var TOKEN: String? = null
    private var mUser: UserResponse? = null

    private lateinit var postViewModel: FeedsCreatPostViewModel

    private var imagev : ImageView? = null
    private var backbtn : ImageView? = null

    private var bodyText: EditText? = null

    var uiListEvent: ArrayList<EventsModelUi>? = null

    private var recyclerView: RecyclerView? = null

    private var binding: ViewDataBinding? = null

    private var eventSelected: Boolean? = false


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        postViewModel = ViewModelProviders.of(this, ViewModelFactory(this.activity as AppCompatActivity)).get(FeedsCreatPostViewModel::class.java)


        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_feeds_creat_post_2ndfragment, container, false)
        val rootView = binding?.root

        getToken()

        setUpUi(rootView)

        responseFromServer()

        return rootView

    }


    @SuppressLint("WrongConstant")
    private fun setUpUi(rootView: View?) {

        imagev = rootView?.findViewById(R.id.FeedsCPF2)
        backbtn = rootView?.findViewById(R.id.creatPost2backBtn)
        recyclerView = rootView?.findViewById(R.id.RCVLLCP2)
        bodyText = rootView?.findViewById(R.id.createPost2EditText)



        backbtn?.setOnClickListener {
            fragmentManager?.popBackStack()

        }

        Glide.with(requireContext())
                .load(requireArguments().getString("path"))
                .into(imagev!!)



        mbutton = rootView?.findViewById(R.id.button4)

        mbutton?.setOnClickListener {

            val imagesss : String = "data:image/png;base64," + convertToBase64(requireArguments().getString("path").toString())

            if (!bodyText?.text.toString().isEmpty()) {

                if(eventSelected!!){




                val posttt = Post(
                        post = UserPost(
                                body = bodyText?.text.toString(),
                                image = imagesss,
                                venue_id = uiListEvent!![lastposition].venue_id,
                                event_id = uiListEvent!![lastposition].id

                        )
                )
                postViewModel.postUserPost(TOKEN!!, posttt)

                getFragmentManager()?.popBackStack("1", 1)

                }else{

                    Toast.makeText(rootView!!.context,"No event Selected",2).show()

                }

            }else{
                Toast.makeText(rootView!!.context,"Insert Text",2).show()
            }

        }

        postViewModel?.geteventsFromServer(TOKEN!!)


        recyclerView?.onItemClick { RCVLLCP2, position, v ->

            if (lastposition == position){
                eventSelected = !eventSelected!!
                uiListEvent!![lastposition].selected = !uiListEvent!![lastposition].selected


            }else{
                eventSelected = true
                uiListEvent!![lastposition].selected = false
                uiListEvent!![position].selected = true
                lastposition = position
            }


            recyclerView?.adapter?.notifyDataSetChanged()




        }

    }



    private fun getToken() {
        token = requireArguments().getString("token")


        TOKEN = token

        val gson = Gson()
        mUser = GamePointApplication.shared!!.getCurrentUser(requireContext())




    }



    private fun convertToBase64(imagePath: String): String {

        val bm = BitmapFactory.decodeFile(imagePath)

        val baos = ByteArrayOutputStream()

        bm.compress(Bitmap.CompressFormat.JPEG, 50, baos)

        val byteArrayImage = baos.toByteArray()

        return Base64.encodeToString(byteArrayImage, Base64.DEFAULT)

    }


    private fun responseFromServer() {

        // lets observe server data response
//        mSwipeToRefresh?.isRefreshing = true


        postViewModel.eventsFromServer.observe(viewLifecycleOwner, Observer { feeds ->

            if (feeds != null) {
//                mSwipeToRefresh!!.isRefreshing = false
                Log.i(TAG, "here are the picks data is: $feeds")

                displayData(feeds)


            } else {
                //picks are null
                VPrecycle?.visibility = View.INVISIBLE
                feedsNoDataView?.visibility = View.VISIBLE

            }


        })

        postViewModel.serverDataFailed.observe(viewLifecycleOwner, Observer { response ->
            Log.e(TAG, "problem fetching picks from server: $response")
            //mSwipeToRefresh!!.isRefreshing = false
//            myFeedsRv?.visibility = View.INVISIBLE
//            feedsNoDataView?.visibility = View.VISIBLE
        })

//        first, try to get data from the database


        postViewModel.failedFeedsFromDb.observe(viewLifecycleOwner, Observer {
            //            myPicksRLNoData?.visibility = View.INVISIBLE
//            myDataEmptyView?.visibility = View.INVISIBLE

//            myPicksRv?.visibility = View.VISIBLE


        })


        //could not save streak to db
        postViewModel.errorSavingFeeds.observe(viewLifecycleOwner, Observer {
            Log.e(TAG, "problem saving feeds to db: $it")



        })

        /**
         * Post deal response
         */

        //posted deal successfully
        postViewModel.postSuccess.observe(viewLifecycleOwner, Observer {
            Log.i(TAG, "Request Sent with success: $it")



        })

        //failed posting deal
        postViewModel.postFail.observe(viewLifecycleOwner, Observer {
            Log.e(TAG, "problem posting deal: $it")
            //display alert dialog

        })

    }


    private fun displayData(feeds: List<EventsModel>) {
        try {
            if (feeds.isEmpty()) {
                //display the empty screen
                setupRecyclerview(feeds)


                VPrecycle?.visibility = View.INVISIBLE
                feedsNoDataView?.visibility = View.VISIBLE
                Log.e(TAG, "Feeds were empty")

            } else if (!feeds.isNullOrEmpty()) {
                Log.e(TAG, "feeds were neither null nor empty")

                VPrecycle?.visibility = View.VISIBLE
                feedsNoDataView?.visibility = View.INVISIBLE
                setupRecyclerview(feeds)
            }
        } catch (e: NullPointerException) {
            Log.e(TAG, "feeds were null $e")
            VPrecycle?.visibility = View.INVISIBLE
            feedsNoDataView?.visibility = View.VISIBLE
            //display null
        }
    }

    private fun makeFeedsUiList(feeds: List<EventsModel>): ArrayList<EventsModelUi> {
        var feedsUi: EventsModelUi


        val FeedsUiList: ArrayList<EventsModelUi> = ArrayList()

        for (i in 0 until feeds.size) {
            val d = feeds[i]

            feedsUi = EventsModelUi(

                    id = d.id,
                    name = d.name,
                    venue_id = d.venueId,
                    sport_id = d.sportId,
                    team1 = d.team1,
                    team2 = d.team2,
                    point_value = d.pointValue,
                    minutes_to_redeem = d.minutesToRedeem,
                    start_time = d.startTime ,
                    venue = d.venue,
                    sport = d.sport!!,
                    pick_id = d.pickId ?: 0,
                    ticket_url = d.ticketUrl,
                    url = d.url,
                    hero_image = d.heroImage,
                    selected = false

            )
            FeedsUiList.add(feedsUi)


        }

        return FeedsUiList
    }


    private fun setupRecyclerview(response: List<EventsModel>) {

        uiListEvent = makeFeedsUiList(response)

        recyclerView?.apply {
            //set a linear layout manager
            layoutManager = LinearLayoutManager(context)
            adapter = CreatePostEventAdapter(uiListEvent!!)
        }




    }




//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        viewModel = ViewModelProviders.of(this).get(FeedsCreatPostViewModel::class.java)
//
//
//
//        // TODO: Use the ViewModel
//    }

}

