package com.jedmahonisgroup.gamepoint

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log


class ConnectivityReceiver(listener: NetworkSchedulerService) : BroadcastReceiver(){
    private var mConnectivityReceiverListener: ConnectivityReceiverListener = listener
    private var TAG = ConnectivityReceiver::class.simpleName

    fun ConnectivityReceiver
                () {}

    override fun onReceive(context: Context?, intent: Intent?) {
        mConnectivityReceiverListener.onNetworkConnectionChanged(isConnected(context!!))
        Log.i(TAG, "received broadcast")
    }

    private fun isConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
        Log.i(TAG, "is connected received broadcast")

    }

    interface ConnectivityReceiverListener {
        fun onNetworkConnectionChanged(isConnected: Boolean)
    }

}