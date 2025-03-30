package com.example.mindbridge.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mindbridge.R
import com.example.mindbridge.databinding.TrendMentorBinding
import com.example.mindbridge.model.Mentor
import com.example.mindbridge.viewProfile

class TrendMentorAdapter(private val mentors: List<Mentor>) :
    RecyclerView.Adapter<TrendMentorAdapter.TrendMentorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrendMentorViewHolder {
        val binding = TrendMentorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TrendMentorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrendMentorViewHolder, position: Int) {
        holder.bind(mentors[position])
    }

    override fun getItemCount(): Int = mentors.size

    inner class TrendMentorViewHolder(private val binding: TrendMentorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(mentor: Mentor) {
            binding.trendMentorName.text = mentor.name ?: "Unknown Mentor"
            binding.rank.text = mentor.rank ?: "N/A"

            // Use Glide to load profile image
            Glide.with(binding.root.context)
                .load(mentor.profileImage)
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .into(binding.imageView3)

            // Set up View Profile button click
            binding.viewProfileButton1.setOnClickListener {
                // Pass mentor data to ViewProfileActivity
                val intent = Intent(binding.root.context, viewProfile::class.java)
                intent.putExtra("MENTOR_NAME", mentor.name)
                intent.putExtra("MENTOR_RANK", mentor.rank)
                intent.putExtra("MENTOR_PROFILE_IMAGE", mentor.profileImage)
                binding.root.context.startActivity(intent)
            }
        }
    }
}

