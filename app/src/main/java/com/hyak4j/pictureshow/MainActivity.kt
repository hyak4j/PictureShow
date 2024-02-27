package com.hyak4j.pictureshow

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.hyak4j.pictureshow.databinding.ActivityMainBinding
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    // Pexels API KEY
    private val API_KEY = "PBemzrFY7A7tFbwS1QsPBmQghMMrJkxe1WkwUHBW7vEUbJamKZvLmSKA"

    private lateinit var binding: ActivityMainBinding
    private lateinit var mProgressBar: ProgressBar

    private val handler = Handler(Looper.getMainLooper())
    private var picturesFromAPI: ArrayList<PictureData> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mProgressBar = binding.progressbar
        mProgressBar.visibility = View.INVISIBLE

        Thread {
            handler.post {
                mProgressBar.visibility = View.VISIBLE
            }
            loadDataFromAPI("https://api.pexels.com/v1/curated?page=1&per_page=15")
            handler.post {
                mProgressBar.visibility = View.INVISIBLE
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
            }
            inputStreamReader.close()
            bufferedReader.close()
            connection.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}