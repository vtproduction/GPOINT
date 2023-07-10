package com.jedmahonisgroup.gamepoint.ui.leaderboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.adapters.leaderboard.GameShowLeaderBoardAdapter
import com.jedmahonisgroup.gamepoint.injection.ViewModelFactory
import com.jedmahonisgroup.gamepoint.model.State
import com.jedmahonisgroup.gamepoint.ui.BaseFragment
import com.jedmahonisgroup.gamepoint.utils.LogUtil
import com.jedmahonisgroup.gamepoint.utils.SharedPreferencesGamePointSharedPrefsRepo
import com.jedmahonisgroup.gamepoint.utils.gamePointSharedPrefsRepo
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_gameshow_leaderboard.*
import kotlinx.android.synthetic.main.fragment_pick_em.*

/**
 * Created by nienle on 05,March,2023
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
class GameShowLeaderBoardFragment : BaseFragment() {

    private var TAG: String = GameShowLeaderBoardFragment::class.java.simpleName



    private lateinit var viewModel: GameShowLeaderBoardViewModel


    private val disposables = CompositeDisposable()

    private var token: String? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        val rootView = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_gameshow_leaderboard, null)
        super.onCreateView(inflater, container, savedInstanceState)
        viewModel = ViewModelProviders.of(this, ViewModelFactory(this.activity as AppCompatActivity)).get(GameShowLeaderBoardViewModel::class.java)
        token = requireArguments().getString("token")
        Log.e(TAG + "Token", token.toString())



        /*if (!isAfterAugustFirst()){
            setUpAugustFirstUi(rootView)
        }else{
            setUpUi(rootView)
        }

        pointsViewModel.getPointsFromDb()*/
        setUpUi(rootView)
        viewModel.getLeaderBoardData()
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LogUtil.d("GameShowLeaderBoardFragment > onViewCreated > 76: $view")
    }





    private lateinit var swipeContainer: SwipeRefreshLayout
    private lateinit var contentContainer: LinearLayout
    private lateinit var loadingContainer: LinearLayout
    private lateinit var notFoundContainer: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var backButton: ImageButton

    private fun setUpUi(rootView: View?){
        LogUtil.d("GameShowLeaderBoardFragment > setUpUi > 85: $rootView")
        if (rootView == null) return

        swipeContainer = rootView.findViewById(R.id.swipeContainer)
        contentContainer = rootView.findViewById(R.id.containerContent)
        loadingContainer = rootView.findViewById(R.id.containerLoading)
        notFoundContainer = rootView.findViewById(R.id.containerNotFound)
        recyclerView = rootView.findViewById(R.id.contentRV)
        backButton = rootView.findViewById(R.id.back_arrow)

        backButton.setOnClickListener { requireActivity().onBackPressed() }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        swipeContainer.setOnRefreshListener {
            swipeContainer.isRefreshing = false
            viewModel.getLeaderBoardData()
        }



        viewModel.gameShowLeaderBoardData.observe(viewLifecycleOwner) { state ->

            val set = AnimationUtils.loadAnimation(requireContext(), R.anim.loading_anim)

            val loadingImage = loadingContainer.findViewById<ImageView>(R.id.imgLoading)
            Log.d(TAG, "setUpUi: ${state.state}")
            when (state.state) {

                State.LOADING -> {
                    loadingContainer.visibility = View.VISIBLE
                    notFoundContainer.visibility = View.GONE
                    contentContainer.visibility = View.GONE
                    loadingImage.startAnimation(set)
                }
                State.FAILED -> {
                    loadingImage.clearAnimation()
                    loadingContainer.visibility = View.GONE
                    notFoundContainer.visibility = View.VISIBLE
                    contentContainer.visibility = View.GONE
                    notFoundContainer.findViewById<TextView>(R.id.noDataFoundTxt).text = "An Error Occur"
                    notFoundContainer.findViewById<TextView>(R.id.noDataFoundSubTxt).text = state.error?.localizedMessage
                }
                State.SUCCESS -> {

                    val data = state.data?.data
                    LogUtil.d("GameShowLeaderBoardFragment > setUpUi > 115: $data")
                    loadingImage.clearAnimation()
                    loadingContainer.visibility = View.GONE
                    notFoundContainer.visibility = View.GONE
                    contentContainer.visibility = View.VISIBLE

                    contentContainer.findViewById<TextView>(R.id.txtDate).text = data!!.title
                    val adapter = data.users?.let { GameShowLeaderBoardAdapter(it) }
                    recyclerView.adapter = adapter
                }
            }
        }

    }

}