package com.image.bingimage


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.snackbar.Snackbar
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class ImagePageFragment : Fragment() {


    private var position=0
    private lateinit var image:ImageView
    private lateinit var text:TextView
    private lateinit var text1:TextView


    fun newInstance(position:Int): ImagePageFragment {
        val imagePageFragment=ImagePageFragment()
        val bundle=Bundle()
        bundle.putInt("position",position)
        imagePageFragment.arguments=bundle
        return imagePageFragment
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //环境变化时不重新创建实例
         // setRetainInstance(true)
        val view = inflater.inflate(R.layout.image_recycler, container, false)
        image=view.findViewById(R.id.image_recycler_image)
        text=view.findViewById(R.id.image_recycler_textview)
        text1=view.findViewById(R.id.image_recycler_textView1)
        position=arguments!!.getInt("position")
        registerForContextMenu(image)
        sentHttp()


        return view
    }


    private fun sentHttp(){
        Thread {
            try {
                val url =
                    URL("https://cn.bing.com/HPImageArchive.aspx?idx=$position&n=1&format=js&mkt=zh-CN")
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
                    .getDefaultSharedPreferences(image.context)
                    .getString("image_resolution","1080x1920")
                val imageurl =
                    "https://cn.bing.com" + jsontemp.getString("url")
                        .replace("1920x1080", p1!!)
                val copyright = jsontemp.getString("copyright")
                image.post {
                    Glide.with(image)
                        .load(imageurl)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .centerCrop()
                        .into(image)
                }
                text.post {
                    text.text=copyright
                }
                text1.post {
                    text1.visibility=View.GONE
                }
            }catch (e:Exception){
                Snackbar.make(image, "网络连接异常", Snackbar.LENGTH_LONG).show()
            }
        }.start()
    }



}
