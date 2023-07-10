package com.jedmahonisgroup.gamepoint.ui.pickEm

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.adapters.gameShow.GameShowAdapter
import com.jedmahonisgroup.gamepoint.adapters.gameShow.GameShowResultAdapter
import com.jedmahonisgroup.gamepoint.injection.ViewModelFactory
import com.jedmahonisgroup.gamepoint.model.State.*
import com.jedmahonisgroup.gamepoint.model.StateNotifier
import com.jedmahonisgroup.gamepoint.model.gameshow.GameShow
import com.jedmahonisgroup.gamepoint.model.gameshow.GameShowResult
import com.jedmahonisgroup.gamepoint.model.gameshow.JoinGameShowResponse
import com.jedmahonisgroup.gamepoint.model.gameshow.Question
import com.jedmahonisgroup.gamepoint.ui.BaseFragment
import com.jedmahonisgroup.gamepoint.ui.MainActivity
import com.jedmahonisgroup.gamepoint.utils.LogUtil
import com.jedmahonisgroup.gamepoint.utils.StringFormatter
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.squareup.picasso.Picasso
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import kotlinx.android.synthetic.main.dialog_gameshow_enterred.view.*
import kotlinx.android.synthetic.main.fragment_pick_em.*
import org.joda.time.DateTime
import org.joda.time.LocalDateTime
import org.joda.time.Period
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.PeriodFormatter
import org.joda.time.format.PeriodFormatterBuilder
import org.w3c.dom.Text
import java.net.URL

/**
 * Created by nienle on 05,January,2023
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
class PickEmFragment : BaseFragment() {

    companion object {
        fun newInstance(prefilledGameShow: GameShow? = null)  : PickEmFragment {
            val f = PickEmFragment()
            f.prefilledGameShow = prefilledGameShow
            return f
        }
    }

    private var TAG: String = PickEmFragment::class.java.simpleName
    //private var binding: ViewDataBinding? = null
    private lateinit var viewModel: PickEmViewModel
    private lateinit var youtubePlayer : YouTubePlayer
    private var wagerValue : Int = 0
    private var prefilledGameShow: GameShow? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        super.onCreateView(inflater, container, savedInstanceState)
        val rootView = inflater.inflate(R.layout.fragment_pick_em, null)

        //viewModel = ViewModelProvider(activity as AppCompatActivity)[PickEmViewModel::class.java]
        viewModel = ViewModelProviders.of(this, ViewModelFactory(this.activity as AppCompatActivity)).get(
            PickEmViewModel::class.java)


        viewModel.gameShowFromServer.observe(viewLifecycleOwner) {

            when(it.state){
                //LOADING -> showLoading()
                FAILED -> showError(it.error)
                SUCCESS -> handleData(it.data?.first, it.data?.second)
                else -> {}
            }

        }

        viewModel.gameShowListFromServer.observe(viewLifecycleOwner){
            when(it.state){
                LOADING -> {
                    showLoading()
                    containerList.visibility = View.GONE
                }
                FAILED -> {
                    hideLoading()
                    showError(it.error)
                }
                SUCCESS -> {
                    hideLoading()
                    it.data?.let { it1 -> handleGameShowList(it1) }
                }
            }
        }

        viewModel.joinGameResponseMessage.observe(viewLifecycleOwner) {
            /*if (!it.isNullOrEmpty()){
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }*/
            if (it.first) {
                showGameShowEndedAlert(viewModel.gameShowFromServer.value?.data?.second?.endDate ?: LocalDateTime.now())
            }else{
                Toast.makeText(context, it.second, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.joinGameShowResponse.observe(viewLifecycleOwner){
            if (it != null){
                setGameOn(it)
            }
        }

        viewModel.gameShowResult.observe(viewLifecycleOwner){
            handleGameShowResult(it)
        }



        return  rootView
    }



    private fun handleGameShowResult(gameShowResultStateNotifier: StateNotifier<GameShowResult?>?) {

        LogUtil.d("PickEmFragment > handleGameShowResult > 139: $gameShowResultStateNotifier")

        if (gameShowResultStateNotifier?.state == LOADING){
            showLoading()
            return
        }

        if (gameShowResultStateNotifier?.state == FAILED){
            hideLoading()
            showError(gameShowResultStateNotifier.error)
            return
        }

        //if (gameShowResultStateNotifier?.state != SUCCESS) return

        val result = gameShowResultStateNotifier?.data ?: return
        hideLoading()
        containerList.visibility = View.VISIBLE
        rvListGameShow.layoutManager = LinearLayoutManager(requireContext())
        val adapter = GameShowResultAdapter(result)

        rvListGameShow.adapter = adapter

    }

    private fun setGameOn(data: JoinGameShowResponse){
        try{
            LogUtil.d("PickEmFragment > setGameOn > 126: $data")
            if (data.player1 != null){
                txtName1.text = data.player1.name
                if (data.player1.avatar != null){
                    Picasso.get().load(data.player1.avatar).placeholder(R.drawable.icon_feather_meh).error(R.drawable.icon_feather_meh).into(imgAva1)
                }
            }else txtName1.text = ""
            if (data.player2 != null){
                txtName2.text = data.player2.name
                if (data.player2.avatar != null){
                    Picasso.get().load(data.player2.avatar).placeholder(R.drawable.icon_feather_meh).error(R.drawable.icon_feather_meh).into(imgAva2)
                }
            }else txtName2.text = ""
            if (data.wager != null){
                txtWagedPoint.text = data.wager.toString()
            }else txtWagedPoint.text = ""
        }catch(t: Throwable){
            LogUtil.e("PickEmFragment > setGameOn > 103: ${t.localizedMessage}")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //viewModel.getGameShow()

        lifecycle.addObserver(youtubePlayerView)
        back_arrow.setOnClickListener {
            LogUtil.d("PickEmFragment > handleGameShowResult > 162: ${requireActivity().localClassName}")
            requireActivity().onBackPressed()
        }
        back_arrow2.setOnClickListener {
            LogUtil.d("PickEmFragment > handleGameShowResult > 162: ${requireActivity().localClassName}")
            requireActivity().onBackPressed()
        }
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = false
            getData()
        }

        youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener(){
            override fun onReady(youTubePlayer: YouTubePlayer) {

               // youTubePlayer.cueVideo("J6eWe4b_Vpo",0f)
                this@PickEmFragment.youtubePlayer = youTubePlayer
                getData()

            }
        })
        //getData()
    }

    private fun getData() {
        val token = requireArguments().getString("token")

        try {
            if (token != null) {
                if (prefilledGameShow == null)
                    viewModel.getGameShowList(token)
                else
                    handleData(1, prefilledGameShow)

            }
        } catch (e: NullPointerException) {
            Log.e(TAG, "token was null inside $TAG, $e")
        }
    }

    private fun showLoading(){
        loadingContainer.visibility = View.VISIBLE
        not_found.visibility = View.GONE
        //swipeRefreshLayout.visibility = View.GONE
        val set = AnimationUtils.loadAnimation(requireContext(), R.anim.loading_anim)
        imgLoading.startAnimation(set)
    }

    private fun hideLoading(){
        loadingContainer.visibility = View.GONE
        not_found.visibility = View.GONE
        //swipeRefreshLayout.visibility = View.GONE
        //val set = AnimationUtils.loadAnimation(requireContext(), R.anim.loading_anim)
        //imgLoading.startAnimation(set)
        imgLoading.clearAnimation()
    }
    
    private fun showError(error: Throwable?) {
        loadingContainer.visibility = View.GONE
        not_found.visibility = View.VISIBLE
        not_found.findViewById<TextView>(R.id.txtNotFoundContent).text = error?.localizedMessage
        //swipeRefreshLayout.visibility = View.GONE
        imgLoading.clearAnimation()

    }

    private fun handleGameShowList(data: List<GameShow>){

        containerList.visibility = View.VISIBLE
        rvListGameShow.layoutManager = LinearLayoutManager(requireContext())
        val adapter = GameShowAdapter(data, ::handleGameShowClicked)

        rvListGameShow.adapter = adapter
    }
    
    private fun handleGameShowClicked(data: GameShow){
        LogUtil.d("PickEmFragment > handleGameShowClicked > 183: ")
        try{
            when(data.gameShowStatus){
                GameShow.Status.CLOSE -> showGameShowEndedAlert(data.endDate)
                GameShow.Status.OPEN ->  viewModel.goToGameShow(data, data.gameShowStatus)
                GameShow.Status.JOINED -> {
                    val token = requireArguments().getString("token")
                    Log.i(TAG, "getToken ===========> token: $token")
                    try {
                        if (token != null) {
                            viewModel.reJoinGameShowAction(token, data, 0)
                        }
                    } catch (e: NullPointerException) {
                        Log.e(TAG, "token was null inside $TAG, $e")
                    }
                }
                GameShow.Status.END -> {
                    val token = requireArguments().getString("token")
                    Log.i(TAG, "getToken ===========> token: $token")
                    try {
                        if (token != null) {
                            viewModel.getGameShowResult(token, data.id)
                        }
                    } catch (e: NullPointerException) {
                        Log.e(TAG, "token was null inside $TAG, $e")
                    }
                }
                else -> {

                }
            }
        }catch(t: Throwable){
            LogUtil.e("PickEmFragment > handleGameShowClicked > 187: ${t.localizedMessage}")
        }
    }

    private fun handleData(state: Int?, data: GameShow?){
        LogUtil.d("PickEmFragment > handleData > 168: $state")
        containerList.visibility = View.GONE
        swipeRefreshLayout.visibility = View.VISIBLE
        when(state){
            0 -> showGameShowContent(data)
            1 -> showContent(data)
        }
    }


    private fun showGameShowEndedAlert(endDate: LocalDateTime){
        val mDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_gameshow_enterred, null)
        //AlertDialogBuilder
        val text = String.format("Congratulations, you have entered the game show. The results will get released shortly after %s. " +
                "Check the main menu after this date to find out if you were a winner!",endDate.toString(DateTimeFormat.forPattern("E d MMM")))
        mDialogView.info.text =  text
        val mBuilder = this.let {
            AlertDialog.Builder(requireContext())
                .setView(mDialogView)
        }
        //show dialog
        val mAlertDialog = mBuilder?.show()
        mAlertDialog!!.setMessage(text)
        mAlertDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        //login button cick of custom layout
        mDialogView.close.setOnClickListener {
            Log.e(TAG, "mAlertDialog?.dismiss(): $it")
            mAlertDialog?.dismiss()
            (requireActivity() as MainActivity).backToHome()

        }
    }


    private fun answerCallback(position: Int, answerValue: List<Boolean>){

        viewModel.updateAnswers(position, answerValue)
    }

    private fun breakerCallback(value: Int) {
        viewModel.onSetBreakerValue(value)
    }

    private fun showGameShowContent(data: GameShow?) {

        if (data == null) return
        try{
            showGameInfo(data)

            txtGameTitle.text = data.name
            txtGameName.text = data.name

            txtDate2.text = data.startDate.toString(DateTimeFormat.forPattern("E d MMM"))
            txtTime2.text = data.endDate.toString(DateTimeFormat.forPattern("E d MMM"))
            txtGameDescription.text = data.description
            swipeRefreshLayout.visibility = View.GONE
            gameOnContainer.visibility = View.VISIBLE
            val adapter = ViewPagerAdapter(requireActivity(), data, ::answerCallback, ::breakerCallback)
            viewPager.isUserInputEnabled = false
            viewPager.adapter = adapter

            viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    when(position){
                        0 -> { // first one
                            btn1.visibility = View.GONE
                            btn2.visibility = View.VISIBLE
                            btn2.text = getString(R.string.next_question)
                        }
                        data.questions.size -> { // last one
                            btn1.visibility = View.VISIBLE
                            btn2.text = getString(R.string.submit_answers)
                        }
                        else -> {
                            btn1.visibility = View.VISIBLE
                            btn2.text = getString(R.string.next_question)
                        }
                    }
                }
            })

            btn1.setOnClickListener {
                viewPager.postDelayed(Runnable {
                    viewPager.setCurrentItem(viewPager.currentItem - 1, true)
                },10)
            }

            btn2.setOnClickListener {

                if (viewPager.currentItem == data.questions.size){
                    viewModel.onSubmitAnswers()
                }else{
                    viewPager.postDelayed(Runnable {
                        viewPager.setCurrentItem(viewPager.currentItem + 1, true)
                    },10)
                }
            }


        }catch(t: Throwable){
            LogUtil.e("PickEmFragment > showGameShowContent > 145: ${t.localizedMessage}")
        }
    }
    
    @SuppressLint("SetTextI18n")
    private fun showContent(data: GameShow?){

        if (data == null) return
        try{
            showGameInfo(data)

            txtGameTitle.text = data.name
            txtGameDescription.text = data.description

            swipeRefreshLayout.visibility = View.VISIBLE
            gameOnContainer.visibility = View.GONE

            btnJoinGame.setOnClickListener {
                val token = requireArguments().getString("token")
                Log.i(TAG, "getToken ===========> token: $token")
                try {
                    if (token != null) {
                        viewModel.reJoinGameShowAction(token, data, wagerValue)
                    }
                } catch (e: NullPointerException) {
                    Log.e(TAG, "token was null inside $TAG, $e")
                }
            }
        }catch(t: Throwable){
            LogUtil.e("PickEmFragment > showContent > 159: ${t.localizedMessage}")
        }
    }

    private fun showGameInfo(data: GameShow?){

        if (data == null) return
        try {
            loadingContainer.visibility = View.GONE
            not_found.visibility = View.GONE
            swipeRefreshLayout.visibility = View.VISIBLE
            imgLoading.clearAnimation()
            if (data.video.isNotEmpty()){
                val queries = StringFormatter.splitQuery(URL(data.video))
                //ogUtil.d("PickEmFragment > showContent > 104: ${queries.get("v")}")
                if (queries.containsKey("v")) {
                    youtubePlayer.cueVideo(queries["v"]!!, 0f)
                }
                youtubePlayerView.visibility = View.VISIBLE
                imgThumbnail.visibility = View.GONE
            }else if (data.photo.isNotEmpty()){

                youtubePlayerView.visibility = View.GONE
                imgThumbnail.visibility = View.VISIBLE
                Picasso.get().load(data.photo).into(imgThumbnail)
            }else {

                youtubePlayerView.visibility = View.GONE
                imgThumbnail.visibility = View.GONE
            }

            val gameTime = Period(data.endDate, data.startDate)

            txtDate.text = data.startDate.toString(DateTimeFormat.forPattern("E d MMM"))
            txtTime.text = data.endDate.toString(DateTimeFormat.forPattern("E d MMM"))


            if (data.wagerPools.size >= 2){
                wagerValue = data.wagerPools.first()
                seekBar.tickCount = data.wagerPools.size
                seekBar.min = data.wagerPools.first().toFloat()
                seekBar.max = data.wagerPools.last().toFloat()
                seekBar.customTickTexts(data.wagerPools.map { i -> i.toString() }.toTypedArray())
                //seekBar.customTickTexts(arrayOf("One","Two","Three"))

                seekBar.onSeekChangeListener = object : OnSeekChangeListener{
                    override fun onSeeking(seekParams: SeekParams?) {
                        val pos = seekParams?.thumbPosition ?: 0
                        wagerValue = data.wagerPools[pos]
                    }

                    override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?) = Unit
                    override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?)= Unit

                }
            }


        }catch(t: Throwable){
            LogUtil.e("PickEmFragment > showContent > 114: ${t.localizedMessage}")
        }
    }

}