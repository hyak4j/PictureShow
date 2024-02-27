package com.hyak4j.pictureshow

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class PictureViewHolder(val pictureView: View): RecyclerView.ViewHolder(pictureView) {
    private val imageView: ImageView = this.pictureView.findViewById(R.id.image)

    fun setImageView(bitmap: Bitmap) {
        imageView.setImageBitmap(bitmap)
    }
}