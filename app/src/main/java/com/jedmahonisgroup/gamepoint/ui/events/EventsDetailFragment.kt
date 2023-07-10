package com.jedmahonisgroup.gamepoint.ui.events


import android.annotation.SuppressLint
import android.app.Activity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.*
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import android.os.Bundle
import android.provider.Settings
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.Gson
import com.jedmahonisgroup.gamepoint.GamePointResultListener
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.adapters.DayAdapter
import com.jedmahonisgroup.gamepoint.injection.ViewModelFactory
import com.jedmahonisgroup.gamepoint.model.CheckinsResponseModel
import com.jedmahonisgroup.gamepoint.model.events.EventsModel
import com.jedmahonisgroup.gamepoint.model.events.PreviousCheckedInEvent
import com.jedmahonisgroup.gamepoint.ui.BaseFragment
import com.jedmahonisgroup.gamepoint.ui.settings.SettingsActivity
import com.jedmahonisgroup.gamepoint.utils.Constants
import com.jedmahonisgroup.gamepoint.utils.Constants.PERCENT_KEY
import com.jedmahonisgroup.gamepoint.utils.Constants.POINTS_KEY
import com.jedmahonisgroup.gamepoint.utils.Constants.POINTS_THIS_CHECK_IN
import com.jedmahonisgroup.gamepoint.utils.Constants.PREVIOUS_MINUETS_KEY
import com.jedmahonisgroup.gamepoint.utils.Constants.TOTAL_POINTS_KEY
import com.jedmahonisgroup.gamepoint.utils.SharedPreferencesGamePointSharedPrefsRepo
import com.jedmahonisgroup.gamepoint.utils.StringFormatter
import com.jedmahonisgroup.gamepoint.utils.StringFormatter.convertTimestampToDate
import com.jedmahonisgroup.gamepoint.utils.StringFormatter.getDay
import com.jedmahonisgroup.gamepoint.utils.StringFormatter.getDayOfYear
import com.jedmahonisgroup.gamepoint.utils.StringFormatter.getMonth
import com.jedmahonisgroup.gamepoint.utils.gamePointSharedPrefsRepo
import com.squareup.picasso.Picasso
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.above_18_alert.view.*
import kotlinx.android.synthetic.main.fragment_event_detail.*
import kotlinx.android.synthetic.main.fragment_events.*
import org.json.JSONObject


/**
 * A simple [Fragment] subclass.
 *
 */
class EventsDetailFragment : BaseFragment(){


    private lateinit var callback: EventsDetailFragmentCallback
    private lateinit var eventsModel: EventsModel

    companion object {
        fun newInstance(eventsModel: EventsModel, callback: EventsDetailFragmentCallback) : EventsDetailFragment{
            val fragment = EventsDetailFragment()
            fragment.eventsModel = eventsModel
            fragment.callback = callback
            return fragment
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                requireActivity().supportFragmentManager.popBackStack()
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mainView = inflater.inflate(R.layout.fragment_event_detail, null)
        Picasso.get()
            .load(eventsModel.heroImage).into(mainView.findViewById<ImageView>(R.id.imgHero))

        return mainView
    }


    interface EventsDetailFragmentCallback{
        fun onCheckInBtnPressed()
        fun onGetTicketBtnPressed()
    }
}
