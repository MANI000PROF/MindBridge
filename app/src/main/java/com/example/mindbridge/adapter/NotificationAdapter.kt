package com.example.mindbridge.adapter

import android.renderscript.ScriptGroup.Binding
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mindbridge.databinding.NotificationItemBinding

class NotificationAdapter(private var notifcation : ArrayList<String>, private var notificationImage : ArrayList<Int>) :RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = NotificationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int  = notifcation.size

    inner class NotificationViewHolder(private  val binding: NotificationItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(position: Int) {
            binding.apply {
                notificationTextView.text = notifcation[position]
                notificationImageView.setImageResource(notificationImage[position])
            }
        }

    }
}