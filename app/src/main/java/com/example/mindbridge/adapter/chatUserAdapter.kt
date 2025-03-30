package com.example.mindbridge.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mindbridge.R
import com.example.mindbridge.databinding.ChatItemBinding

class chatUserAdapter(
    private val userList: MutableList<String>,
    private val userInfos: MutableList<String>,
    private val chatImage: MutableList<String>,
    private val onMessageClick: (name: String, uid: String, image: String) -> Unit // Updated callback
) : RecyclerView.Adapter<chatUserAdapter.ChatUserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatUserViewHolder {
        val binding = ChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatUserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatUserViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = userList.size

    inner class ChatUserViewHolder(private val binding: ChatItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                profileNameText.text = userList[position]
                profileInfoText.text = userInfos[position]

                Glide.with(binding.root.context)
                    .load(chatImage[position])
                    .placeholder(R.drawable.profile)
                    .error(R.drawable.profile)
                    .into(userImage)

                callChatButton.text = "Call/Chat"

                // Set up the message button to trigger the callback
                messageButton.setOnClickListener {
                    onMessageClick(userInfos[position], userList[position],  chatImage[position]) // Pass name, uid, and image
                }
            }
        }
    }

    fun updateData(newUserNames: List<String>, newUserInfos: List<String>, newUserImages: List<String>) {
        userList.clear()
        userInfos.clear()
        chatImage.clear()
        userList.addAll(newUserNames)
        userInfos.addAll(newUserInfos)
        chatImage.addAll(newUserImages)
        notifyDataSetChanged()
    }
}
