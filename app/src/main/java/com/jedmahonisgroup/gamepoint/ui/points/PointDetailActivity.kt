package com.jedmahonisgroup.gamepoint.ui.points

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.jedmahonisgroup.gamepoint.R

class PointDetailActivity : AppCompatActivity() {

    private var mToolbar: Toolbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_point_detail)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, PointDetailFragment.newInstance())
                .commitNow()
        }


    }
}