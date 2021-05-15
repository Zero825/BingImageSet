package com.image.bingimage

import android.Manifest
import android.app.WallpaperColors
import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class MainActivity : AppCompatActivity() {


    private  var image: Bitmap?=null
    private var name:String?=null
    private var exitTime:Long=0
    private var permissonsStorage = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private val requestPermissionCode = 1
    private var nscolor:Int = 0
    private lateinit var mPager: ViewPager2
    private lateinit var calendar1:Calendar
    private lateinit var handler: Handler


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById<Toolbar>(R.id.activity_main_toolbar))
        nscolor=ContextCompat.getColor(this,R.color.colorPrimary)
        mPager = findViewById(R.id.pager)
        mPager.registerOnPageChangeCallback(PageCallback())
        mPager.offscreenPageLimit=1
        updataView()
        println("act被创建啦")
        handler = Handler{
           when (it.what){
               1->{
                   val window = this.window
                   window.statusBarColor = nscolor
                   window.navigationBarColor = nscolor
                   val tb1 = findViewById<Toolbar>(R.id.activity_main_toolbar)
                   tb1.setBackgroundColor(nscolor)
               }
           }
            false
        }




        val intent = Intent(this@MainActivity, RepetitionService::class.java)
        println("启动服务")
        startService(intent)



    }

    inner class PageCallback: ViewPager2.OnPageChangeCallback() {

        override fun onPageSelected(position: Int) {

            getImageBitmapAndColor()
        }
    }

    override fun onResume() {
        super.onResume()
        println("我回来啦")
        val calendar2= Calendar.getInstance()
        if (calendar2.get(Calendar.MONTH)>calendar1.get(Calendar.MONTH)||
            (calendar2.get(Calendar.MONTH)==calendar1.get(Calendar.MONTH)&&
                calendar2.get(Calendar.DAY_OF_MONTH)>calendar1.get(Calendar.DAY_OF_MONTH))){
            updataView()
        }
    }

    fun updataView(){
        val pagerAdapter = ImagePagerAdapter(this,8)
        mPager.adapter=pagerAdapter
        calendar1= Calendar.getInstance()
        println("更新视图")
    }


    private fun downloadWallpaper(){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissonsStorage, requestPermissionCode)
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(mPager, "没有读写权限", Snackbar.LENGTH_LONG).show()
        }else {
            Thread{
                try {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        val p1 = PreferenceManager
                            .getDefaultSharedPreferences(this)
                            .getString("imae_download_path", "/storage/emulated/0/Download")
                        val path = p1 + "/$name.png"
                        if (bitmapToFile(
                                image!!,
                                path,
                                Bitmap.CompressFormat.PNG
                            )
                        ) {
                            runOnUiThread {
                                Snackbar.make(mPager, "图片已下载到目标路径", Snackbar.LENGTH_LONG).show()
                            }

                        } else {
                            runOnUiThread {
                                Snackbar.make(mPager, "图片下载失败", Snackbar.LENGTH_LONG).show()
                            }
                        }
                    }else{

                        val resolver = contentResolver
                        val imageCollection = MediaStore.Images.Media.getContentUri(
                            MediaStore.VOLUME_EXTERNAL_PRIMARY)

                        val newImageDetails = ContentValues().apply {
                            put(MediaStore.Images.Media.DISPLAY_NAME, "$name")
                            put(MediaStore.Images.Media.IS_PENDING,1)
                        }
                        val newImageUri = resolver
                            .insert(imageCollection, newImageDetails)
                        if (newImageUri != null) {
                            image!!.compress(Bitmap.CompressFormat.PNG, 100,resolver.openOutputStream(newImageUri))
                            newImageDetails.clear()
                            newImageDetails.put(MediaStore.Images.Media.IS_PENDING, 0)
                            resolver.update(newImageUri,newImageDetails,null,null)
                            runOnUiThread {
                                Snackbar.make(mPager, "图片已下载", Snackbar.LENGTH_LONG).show()
                            }
                        }else{
                            runOnUiThread {
                                Snackbar.make(mPager, "图片下载失败", Snackbar.LENGTH_LONG).show()
                            }
                        }
                    }
                }catch (e:java.lang.Exception){
                    e.printStackTrace()
                    runOnUiThread {
                        Snackbar.make(mPager, "图片下载失败", Snackbar.LENGTH_LONG).show()
                    }
                }
            }.start()

        }
    }

    private fun bitmapToFile(bitmap:Bitmap, filePath:String, format: Bitmap.CompressFormat):Boolean {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                file.createNewFile()
                val out = FileOutputStream(file)
                bitmap.compress(format, 100, out)
                out.flush()
                out.close()
            }else{
                return false
            }
        }catch (e: IOException){
            e.printStackTrace()
            return false
        }
        return true
    }

    fun getImageBitmapAndColor(){
        val t=Thread {
            try {
                val url =
                    URL("https://cn.bing.com/HPImageArchive.aspx?idx=${mPager.currentItem}&n=1&format=js&mkt=zh-CN")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 50000
                connection.readTimeout = 50000
                val inp = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inp))
                val line: String = reader.readLine()
                val jsontemp =
                    JSONObject(JSONArray(JSONObject(line).getString("images"))[0].toString())
                val p1= PreferenceManager
                    .getDefaultSharedPreferences(this)
                    .getString("image_resolution","1080x1920")
                val imageurl =
                    "https://cn.bing.com" + jsontemp.getString("url")
                        .replace("1920x1080", p1!!)
                name=jsontemp.getString("fullstartdate")
                image = Glide.with(this)
                    .asBitmap()
                    .load(imageurl)
                    .submit()
                    .get()
            }catch (e:Exception){
                e.printStackTrace()
                runOnUiThread {
                    Snackbar.make(mPager, "网络连接异常", Snackbar.LENGTH_LONG).show()
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                val wpc= WallpaperColors.fromBitmap(image!!)
                nscolor=wpc.primaryColor.toArgb()
                handler.sendEmptyMessage(1)

            }
        }
        t.start()

    }


    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        val inflater: MenuInflater =this.menuInflater
        inflater.inflate(R.menu.bingimage_menu, menu)

    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.bingimage_menu1 -> {

                setWallpaper(1)
                true
            }
            R.id.bingimage_menu2 -> {
                setWallpaper(2)
                true
            }
            R.id.bingimage_menu3 -> {
                setWallpaper(3)
                true
            }
            R.id.bingimage_menu4 -> {

                downloadWallpaper()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {

            exitApp()
    }


    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.activity_main_menu1 -> {
            val intent=Intent(this,SettingsActivity::class.java)
            intent.putExtra("nscolor",nscolor)
            startActivity(intent)
            true
        }

        R.id.activity_main_menu2 -> {
            val aboutDialog=AboutDialog()
            aboutDialog.show(supportFragmentManager,"AboutDialog")
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        println("?????????????????????????????????????????????????????")

    }


    private fun exitApp(){
    if ((System.currentTimeMillis() - exitTime) > 2000)
    {
        Snackbar.make(mPager, "再按一次退出必应每图", Snackbar.LENGTH_SHORT).show()
        exitTime = System.currentTimeMillis()
    } else {
        finish()
        val intent = Intent(this@MainActivity, RepetitionService::class.java)
        println("启动服务")
        stopService(intent)
    }
}



    private fun setWallpaper(flag:Int){

        Thread {
            val wm = WallpaperManager.getInstance(this)
            var msg = String()
            if (flag==1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    wm.setBitmap(image, null, true, WallpaperManager.FLAG_LOCK)
                }
                msg="图片已设置为锁屏"
            }
            if (flag==2) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    wm.setBitmap(image, null, true, WallpaperManager.FLAG_SYSTEM)
                }
                msg="图片已设置为壁纸"
            }
            if (flag==3) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    wm.setBitmap(image, null, true, WallpaperManager.FLAG_LOCK)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    wm.setBitmap(image, null, true, WallpaperManager.FLAG_SYSTEM)
                }
                msg="图片已设置为壁纸和锁屏"
            }
            runOnUiThread {
                Snackbar.make(mPager, msg, Snackbar.LENGTH_LONG).show()
            }
        }.start();
    }








    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }


}



