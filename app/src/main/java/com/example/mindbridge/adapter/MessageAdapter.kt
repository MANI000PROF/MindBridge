package com.example.mindbridge.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mindbridge.R
import com.example.mindbridge.SwipeGestureDetector
import com.example.mindbridge.chatActivity
import com.example.mindbridge.databinding.ReceiveMsgBinding
import com.example.mindbridge.databinding.SendMsgBinding
import com.example.mindbridge.model.Message
import com.example.mindbridge.ui.theme.FullImageActivity// Import your FullImageActivity
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter(
    private val context: Context,
    private val messages: ArrayList<Message>,
    private val senderRoom: String,
    private val receiverRoom: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val ITEM_SENT = 1
        const val ITEM_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.senderId == FirebaseAuth.getInstance().uid) {
            ITEM_SENT
        } else {
            ITEM_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_SENT) {
            val view = LayoutInflater.from(context).inflate(R.layout.send_msg, parent, false)
            SentMsgHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.receive_msg, parent, false)
            ReceiveMsgHolder(view)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        holder.itemView.setOnLongClickListener {
            onMessageLongClickListener?.invoke(messages[holder.adapterPosition])
            true
        }

        // Initialize SwipeGestureDetector
        val swipeDetector = SwipeGestureDetector(holder.itemView.context) { direction ->
            when (direction) {
                "left" -> {
                    // Handle swipe left action
                    onSwipeReply(message) // Call your swipe reply method
                }
                "right" -> {
                    // Handle swipe right action (if needed)
                    onSwipeReply(message) // You can also implement different actions for right swipe
                }
            }
        }

        // Set the touch listener for swipe detection
        holder.itemView.setOnTouchListener { view, event ->
            swipeDetector.onTouchEvent(event) // Pass the touch event to the swipe detector
            false // Return false to allow other touch events to be processed
        }

        if (holder is SentMsgHolder) {
            holder.bind(message)
        } else if (holder is ReceiveMsgHolder) {
            holder.bind(message)
        }
    }

    fun onSwipeReply(message: Message) {
        // Here, handle the reply action
        (context as? chatActivity)?.setReplyMessage(message) // Or any other method to handle the reply
    }

    override fun getItemCount(): Int = messages.size

    inner class SentMsgHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding: SendMsgBinding = SendMsgBinding.bind(itemView)

        fun bind(message: Message) {
            if (message.message == "photo") {
                binding.image.visibility = View.VISIBLE
                binding.message.visibility = View.GONE
                Glide.with(context)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.ic_launcher_foreground) // Error handling
                    .into(binding.image)

                // Set onClickListener to open full image
                binding.image.setOnClickListener {
                    val intent = Intent(context, FullImageActivity::class.java)
                    intent.putExtra("IMAGE_URL", message.imageUrl) // Pass the image URL
                    context.startActivity(intent)
                }
            } else {
                binding.message.visibility = View.VISIBLE
                binding.image.visibility = View.GONE
                binding.message.text = message.message
            }
        }
    }

    inner class ReceiveMsgHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding: ReceiveMsgBinding = ReceiveMsgBinding.bind(itemView)

        fun bind(message: Message) {
            if (message.message == "photo") {
                binding.image.visibility = View.VISIBLE
                binding.message.visibility = View.GONE
                Glide.with(context)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.ic_launcher_foreground) // Error handling
                    .into(binding.image)

                // Set onClickListener to open full image
                binding.image.setOnClickListener {
                    val intent = Intent(context, FullImageActivity::class.java)
                    intent.putExtra("IMAGE_URL", message.imageUrl) // Pass the image URL
                    context.startActivity(intent)
                }
            } else {
                binding.message.visibility = View.VISIBLE
                binding.image.visibility = View.GONE
                binding.message.text = message.message
            }
        }
    }

    // In MessageAdapter.kt
    private var onMessageLongClickListener: ((Message) -> Unit)? = null

    fun setOnMessageLongClickListener(listener: (Message) -> Unit) {
        onMessageLongClickListener = listener
    }
}
