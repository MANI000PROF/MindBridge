package com.example.mindbridge

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide

class viewProfile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_profile)

        // Apply WindowInsets for edge-to-edge UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Retrieve mentor data passed from the TrendMentorAdapter
        val name = intent.getStringExtra("MENTOR_NAME")
        val rank = intent.getStringExtra("MENTOR_RANK")
        val profileImageUrl = intent.getStringExtra("MENTOR_PROFILE_IMAGE")

        // Set up UI elements
        val nameTextView: TextView = findViewById(R.id.textView13)
        val rankTextView: TextView = findViewById(R.id.textView16)
        val profileImageView: ImageView = findViewById(R.id.profilePicImageView)

        // Populate the views with the received data
        nameTextView.text = name ?: "Unknown Mentor"
        rankTextView.text = rank ?: "N/A"

        // Load profile image using Glide
        Glide.with(this)
            .load(profileImageUrl)  // Check if profileImageUrl is not null
            .placeholder(R.drawable.profile)
            .into(profileImageView)
    }
}

