package com.hyak4j.pictureshow

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class PictureAdapter(val context: Context, val pictures: ArrayList<PictureData>): RecyclerView.Adapter<PictureViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PictureViewHolder {
        val pictureView = LayoutInflater.from(context).inflate(R.layout.picture_layout, parent, false)
        return PictureViewHolder(pictureView)
    }

    override fun getItemCount(): Int {
       return pictures.size
    }

    override fun onBindViewHolder(holder: PictureViewHolder, position: Int) {
        holder.setImageView(pictures[position].realImage!!)
    }
}