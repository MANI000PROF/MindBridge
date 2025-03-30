package com.example.mindbridge.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mindbridge.R
import com.example.mindbridge.SetupProfileActivity
import com.example.mindbridge.chatActivity
import com.example.mindbridge.databinding.ItemProfileBinding
import com.example.mindbridge.model.User

class UserAdapter(
    private val context: Context,
    private var userList: List<User>,
    private val itemClickListener: OnItemClickListener // Interface for click handling
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(val binding: ItemProfileBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            // Safely set the username
            binding.username.text = user.name ?: "Unknown User"

            // Load the profile image with error handling
            Glide.with(context)
                .load(user.profileImage)
                .placeholder(R.drawable.profile) // Default placeholder image
                .error(R.drawable.profile) // Error image if loading fails
                .into(binding.profileImg)

            // Set expertise text
            binding.expertise.text = user.qualification ?: "No expertise listed"

            // Handle item click
            itemView.setOnClickListener {
                val intent = Intent(context, chatActivity::class.java).apply {
                    putExtra("name", user.name)
                    putExtra("image", user.profileImage)
                    putExtra("uid", user.uid)
                }
                context.startActivity(intent) // Use context to start the activity
            }

            // Handle long item press
            itemView.setOnLongClickListener {
                itemClickListener.onItemLongPress(user) // Notify the listener with the long-pressed user
                true // Return true to indicate the event was handled
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ItemProfileBinding.inflate(inflater, parent, false)
        return UserViewHolder(binding)
    }

    override fun getItemCount(): Int = userList.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user) // Use the bind method to set the data
    }

    // Interface for item click handling
    interface OnItemClickListener {
        fun onItemClick(user: User)
        fun onItemLongPress(user: User) // New method for long press
    }
}

