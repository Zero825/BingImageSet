package com.image.bingimage


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi


class SplashActivity:Activity() {
    private var textView:TextView?=null
    var connectivityManager:ConnectivityManager?=null
    var networkCallbackImpl=NetworkCallbackImpl()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

         connectivityManager=getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager!!.registerDefaultNetworkCallback(networkCallbackImpl)
        }
        textView=findViewById<TextView>(R.id.activity_splash_textView)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(!isNetworkConnected(this)) textView!!.visibility = View.VISIBLE
        }

    }

    fun openb(){
        Thread.sleep(1000)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun isNetworkConnected(context: Context?): Boolean {
        if (context != null) {
            val mConnectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val mNetworkInfo = mConnectivityManager.activeNetwork
            if (mNetworkInfo != null) {
                return true
            }
        }
        return false
    }


    inner class NetworkCallbackImpl:ConnectivityManager.NetworkCallback(){
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            println("okokokokokokok")
            openb()
            connectivityManager!!.unregisterNetworkCallback(networkCallbackImpl)

        }

    }
}

