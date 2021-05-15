package com.image.bingimage

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class RepetitionService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private var connectivityManager: ConnectivityManager?=null
    private var networkCallbackImpl=NetworkCallbackImpl()
    var setState:Boolean=false
    private var conState:Boolean=true

    override fun onCreate() {
        super.onCreate()
        println("服务被创建了")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        println("执行指定命令")
        setAlarm()
        setState=false
        connectivityManager=getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager!!.registerDefaultNetworkCallback(networkCallbackImpl)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            conState=isNetworkConnected(this)
        }
        if(conState){
            setState=true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setWallpaper()
            }
        }



        return super.onStartCommand(intent, flags, startId)
    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun setWallpaper(){
        Thread {
            val url =
                URL("https://cn.bing.com/HPImageArchive.aspx?idx=0&n=1&format=js&mkt=zh-CN")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 50000
            connection.readTimeout = 50000
            val inp = connection.inputStream
            val reader = BufferedReader(InputStreamReader(inp))
            val line: String = reader.readLine()
            val jsontemp =
                JSONObject(JSONArray(JSONObject(line).getString("images"))[0].toString())
            val p= PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString("image_resolution","1080x1920")
            val imageurl =
                "https://cn.bing.com" + jsontemp.getString("url")
                    .replace("1920x1080", p!!)
            val image = Glide.with(this)
                .asBitmap()
                .load(imageurl)
                .submit()
                .get()

            val wm = WallpaperManager.getInstance(this)
            val p1=PreferenceManager
                .getDefaultSharedPreferences(this)
                .getBoolean("auto_set_wallpaper",false)
            val p2=PreferenceManager
                .getDefaultSharedPreferences(this)
                .getBoolean("set_lock_wallpaper",false)
            val p3=PreferenceManager
                .getDefaultSharedPreferences(this)
                .getBoolean("set_wallpaper",false)
            if(p1){
                if(p2&&p3) {
                    wm.setBitmap(image, null, true, WallpaperManager.FLAG_LOCK)
                    wm.setBitmap(image, null, true, WallpaperManager.FLAG_SYSTEM)
                    println("23232323232332323")
                }
                else {
                    if (p2) {
                        wm.setBitmap(image, null, true, WallpaperManager.FLAG_LOCK)
                        println("22222222222222222222222222")
                    }
                    if (p3) {
                        wm.setBitmap(image, null, true, WallpaperManager.FLAG_LOCK)
                        println("3333333333333333333333333333333")
                    }
                }
            }



        }.start()


    }

    private fun setAlarm(){
        val calendar1= Calendar.getInstance()
        calendar1.add(Calendar.DAY_OF_MONTH,1)
        calendar1.set(Calendar.HOUR_OF_DAY,0)
        calendar1.set(Calendar.MINUTE,0)
        calendar1.set(Calendar.SECOND,0)
        println(calendar1.get(Calendar.DAY_OF_MONTH))
        val intent1 = Intent(this, RepetitionService::class.java)
        val alarmManager=getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getService(this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.
            setExact(
                AlarmManager.RTC_WAKEUP,
                calendar1.timeInMillis + 10*1000,pendingIntent)
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

    override fun onDestroy() {
        super.onDestroy()
        println("我倒了呜呜呜")
    }


    inner class NetworkCallbackImpl:ConnectivityManager.NetworkCallback(){
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            println("网络可行了")
            if(!setState) {
                println("延迟更新开始")
                setState=true
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setWallpaper()
                }
            }

        }

    }
}