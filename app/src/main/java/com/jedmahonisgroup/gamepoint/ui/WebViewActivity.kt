package com.jedmahonisgroup.gamepoint.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.webkit.WebView
import android.webkit.WebViewClient
import com.jedmahonisgroup.gamepoint.R

class WebViewActivity : AppCompatActivity() {
    var mywebview: WebView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        val type : Int = intent.getIntExtra("urlType", 0)
        mywebview = findViewById(R.id.web_view)
        mywebview!!.webViewClient = WebViewClient()
        mywebview!!.settings.javaScriptEnabled = true
        var urlString = "https://www.gamepoint.fans/privacy"
        if (type == 1) {
            urlString = "https://www.gamepoint.fans/terms"
        } else if (type == 2) {
            urlString = "https://www.gamepoint.fans/contest-terms"
        }
        mywebview!!.loadUrl(urlString)
    }
}