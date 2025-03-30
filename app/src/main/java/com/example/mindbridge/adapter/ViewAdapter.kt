package com.example.mindbridge.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mindbridge.R
import com.example.mindbridge.databinding.AnalyticsMentorBinding
import com.example.mindbridge.viewProfile

class ViewAdapter(
    private val viewItems: MutableList<String>,
    private val positionList: MutableList<String>,
    private val itemImage: MutableList<String>
) : RecyclerView.Adapter<ViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AnalyticsMentorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = viewItems.size

    inner class ViewHolder(private val binding: AnalyticsMentorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                trendMentorName1.text = viewItems[position]
                rank1.text = positionList[position]

                // Use Glide to load the image from URL
                Glide.with(binding.root.context)
                    .load(itemImage[position]) // Load image using URL
                    .placeholder(R.drawable.profile) // Default placeholder image
                    .error(R.drawable.profile) // Error image if loading fails
                    .into(userImage1)

                // Set up View Profile button click
                viewProfileButton.setOnClickListener {
                    // Pass mentor data to ViewProfileActivity
                    val context = binding.root.context
                    val intent = Intent(context, viewProfile::class.java)
                    intent.putExtra("MENTOR_NAME", viewItems[position])
                    intent.putExtra("MENTOR_RANK", positionList[position])
                    intent.putExtra("MENTOR_PROFILE_IMAGE", itemImage[position])
                    context.startActivity(intent)
                }
            }
        }
    }

    fun updateData(newViewItems: List<String>, newPositionList: List<String>, newItemImages: List<String>) {
        viewItems.clear()
        positionList.clear()
        itemImage.clear()
        viewItems.addAll(newViewItems)
        positionList.addAll(newPositionList)
        itemImage.addAll(newItemImages)
        notifyDataSetChanged()
    }
}

