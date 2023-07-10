package com.jedmahonisgroup.gamepoint.ui.points

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProviders
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.injection.ViewModelFactory
import com.jedmahonisgroup.gamepoint.ui.events.EventViewModel


class PointDetailFragment : Fragment() {

    companion object {
        fun newInstance() = PointDetailFragment()
    }

    private lateinit var viewModel: PointDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, ViewModelFactory(this.activity as AppCompatActivity)).get(
            PointDetailViewModel::class.java)



    }

    private var mBtn1: Button? = null
    private var mBtn2: Button? = null
    private var mBtn3: Button? = null
    private var txtPoint: TextView? = null
    private var txtPointValue: TextView? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getUserPointSuccess.observe(viewLifecycleOwner) {
            txtPoint?.text = it
        }
        viewModel.getUserPointFailed.observe(viewLifecycleOwner) {
            txtPoint?.text = it
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val mainView = inflater.inflate(R.layout.fragment_point_detail, container, false)

        val mToolbar = mainView.findViewById<Toolbar>(R.id.toolbar)
        mToolbar!!.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()

        }

        txtPoint = mainView.findViewById(R.id.txtTokenAmount)
        txtPointValue = mainView.findViewById(R.id.txtTokenValue)
        mBtn1 = mainView.findViewById(R.id.btnAction1)
        mBtn2 = mainView.findViewById(R.id.btnAction2)
        mBtn3 = mainView.findViewById(R.id.btnAction3)

        mBtn1?.setOnClickListener {
            //TODO implement method
        }

        mBtn2?.setOnClickListener {
            //TODO implement method
        }

        mBtn3?.setOnClickListener {
            //TODO implement method
        }

            return mainView


    }
}