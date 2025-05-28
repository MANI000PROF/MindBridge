package com.example.mindbridge.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mindbridge.databinding.ItemMentorSliderBinding
import com.example.mindbridge.model.Mentor
import com.bumptech.glide.Glide

class MentorSliderAdapter(private val mentorList: List<Mentor>) :
    RecyclerView.Adapter<MentorSliderAdapter.MentorViewHolder>() {

    inner class MentorViewHolder(val binding: ItemMentorSliderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentorViewHolder {
        val binding =
            ItemMentorSliderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MentorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MentorViewHolder, position: Int) {
        val mentor = mentorList[position]

        // Load mentor photo circular with Glide or your preferred lib
        Glide.with(holder.binding.root.context)
            .load(mentor.profileImage)  // Adjust field name accordingly
            .circleCrop()
            .into(holder.binding.avatarImage)

        holder.binding.nameText.text = mentor.name

        // TODO: Add click listeners if needed (e.g., join or view mentor details)
    }

    override fun getItemCount(): Int = mentorList.size
}
