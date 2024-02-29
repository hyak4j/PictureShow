package com.hyak4j.pictureshow

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.hyak4j.pictureshow.databinding.ActivityPictureBinding

class PictureActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPictureBinding
    private lateinit var imageView: ImageView
    private lateinit var txtID: TextView
    private lateinit var txtAuthor: TextView
    private lateinit var txtURL: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPictureBinding.inflate(layoutInflater)
        setContentView(binding.root)
        imageView = binding.image
        txtID = binding.id
        txtAuthor = binding.photographer
        txtURL = binding.url

        txtID.text = "Photo ID: ${intent.getStringExtra("id")}"
        txtAuthor.text = "Author: ${intent.getStringExtra("author")}"
        txtURL.text = "URL: ${intent.getStringExtra("url")}"

        // ByteArray => Bitmap
        val byteArray = intent.getByteArrayExtra("bitmap")
        val image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)
        imageView.setImageBitmap(image)
    }
}