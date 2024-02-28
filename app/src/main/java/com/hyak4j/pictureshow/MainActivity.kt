package com.hyak4j.pictureshow

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
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
    private lateinit var mRecyclerView: RecyclerView

    private val handler = Handler(Looper.getMainLooper())
    private var picturesFromAPI: ArrayList<PictureData> = ArrayList()
    private val newIndices: ArrayList<Int> = ArrayList()
    private val cachedThreadPoolExecutor = Executors.newCachedThreadPool()

    private lateinit var layoutManager: LayoutManager
    private lateinit var adapter: PictureAdapter
    private val recyclerViewBottomImageContainer = intArrayOf(0, 0, 0)
    private var page = 1 // 目前頁數
    private val per_page = 15 // 每頁張數

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mRecyclerView = binding.recyclerview
        mBtnSearch = binding.btnSearch
        mProgressBar = binding.progressbar
        mProgressBar.visibility = View.INVISIBLE

        Thread {
            handler.post {
                // 禁止使用者按搜尋
                mBtnSearch.isEnabled = false
                mProgressBar.visibility = View.VISIBLE
            }
            loadDataFromAPI("https://api.pexels.com/v1/curated?page=1&per_page=$per_page")
            loadImageFromAPI()
            handler.post {
                layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
                adapter = PictureAdapter(this, picturesFromAPI)
                mRecyclerView.layoutManager = layoutManager
                mRecyclerView.adapter = adapter
                mProgressBar.visibility = View.INVISIBLE
                mBtnSearch.isEnabled = true
            }
        }.start()

        mRecyclerView.addOnScrollListener(ScrollListener(this))
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

    inner class ScrollListener(val context: Context) : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            // 確認滑到最下面
            val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager
            val lastVisibleItemPosition =
                layoutManager.findLastVisibleItemPositions(recyclerViewBottomImageContainer)
            val itemCount = layoutManager.itemCount

            if (lastVisibleItemPosition[0] == itemCount - 1 ||
                lastVisibleItemPosition[1] == itemCount - 1 ||
                lastVisibleItemPosition[2] == itemCount - 1
            ) {
                Thread {
                    handler.post {
                        mBtnSearch.isEnabled = false
                        mProgressBar.visibility = View.VISIBLE
                        Toast.makeText(context, R.string.download_new_image, Toast.LENGTH_LONG)
                            .show()
                    }
                    page += 1

                    loadDataFromAPI("https://api.pexels.com/v1/curated?page=$page&per_page=$per_page")

                    loadImageFromAPI()
                    handler.post {
                        mBtnSearch.isEnabled = true
                        mProgressBar.visibility = View.INVISIBLE
                        adapter.notifyDataSetChanged()
                    }
                }.start()
            }
        }
    }
}