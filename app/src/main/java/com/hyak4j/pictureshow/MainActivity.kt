package com.hyak4j.pictureshow

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hyak4j.pictureshow.databinding.ActivityMainBinding
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    // Pexels API KEY
    private val API_KEY = "PBemzrFY7A7tFbwS1QsPBmQghMMrJkxe1WkwUHBW7vEUbJamKZvLmSKA"

    private lateinit var binding: ActivityMainBinding
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mBtnSearch: Button

    private val handler = Handler(Looper.getMainLooper())
    private var picturesFromAPI: ArrayList<PictureData> = ArrayList()
    private val newIndices: ArrayList<Int> = ArrayList()
    private val cachedThreadPoolExecutor = Executors.newCachedThreadPool()

    private lateinit var layoutManager: LayoutManager
    private lateinit var adapter: PictureAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mBtnSearch = binding.btnSearch
        mProgressBar = binding.progressbar
        mProgressBar.visibility = View.INVISIBLE

        Thread {
            handler.post {
                // 禁止使用者按搜尋
                mBtnSearch.isEnabled = false
                mProgressBar.visibility = View.VISIBLE
            }
            loadDataFromAPI("https://api.pexels.com/v1/curated?page=1&per_page=15")
            loadImageFromAPI()
            handler.post {
                layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
                adapter = PictureAdapter(this, picturesFromAPI)
                binding.recyclerview.layoutManager = layoutManager
                binding.recyclerview.adapter = adapter
                mProgressBar.visibility = View.INVISIBLE
                mBtnSearch.isEnabled = true
            }
        }.start()
    }

    private fun loadDataFromAPI(url: String) {
        val urlObject = URL(url)
        try {
            val connection: HttpURLConnection = urlObject.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", API_KEY)

            val inputStreamReader = InputStreamReader(connection.inputStream, "UTF-8")
            val bufferedReader = BufferedReader(inputStreamReader)
            val result = bufferedReader.readLine()
            val response = JSONObject(result)
            val photos = response.getJSONArray("photos")
            for (i in 0 until photos.length()) {
                val photo = photos.getJSONObject(i)
                picturesFromAPI.add(
                    PictureData(
                        photo.getString("id"),
                        photo.getString("photographer"),
                        photo.getJSONObject("src").getString("medium"),
                        null
                    )
                )
                newIndices.add(picturesFromAPI.size - 1)
            }
            inputStreamReader.close()
            bufferedReader.close()
            connection.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadImageFromAPI() {
        val latch = CountDownLatch(newIndices.size)
        for (i in newIndices) {
            // 從 medium路徑下載圖片
            cachedThreadPoolExecutor.execute {
                try {
                    val inputStream: InputStream = URL(picturesFromAPI[i].medium).openStream()
                    picturesFromAPI[i].realImage = BitmapFactory.decodeStream(inputStream)
                    latch.countDown()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        try {
            // 確保每個realImage都處理完後再往下做
            latch.await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        newIndices.clear()
    }
}