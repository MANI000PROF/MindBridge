package com.example.mindbridge.ui.theme

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.os.Bundle
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.mindbridge.R
import com.example.mindbridge.databinding.ActivityFullImageBinding

class FullImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFullImageBinding // Ensure you use the correct binding class
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var scale = 1f
    private var focusX = 0f
    private var focusY = 0f
    private val matrix = Matrix()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullImageBinding.inflate(layoutInflater) // Inflate the binding
        setContentView(binding.root) // Set the content view using the binding

        // Access views through binding
        val imageUrl = intent.getStringExtra("IMAGE_URL")
        if (imageUrl != null) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.ic_launcher_foreground) // Error handling
                .into(binding.fullImageView) // Assuming fullImageView is your ImageView's ID
        }

        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())

        // Set touch listener for zooming
        binding.fullImageView.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            true
        }

        // Set initial image scale
        resetImageScale()
    }

    private fun resetImageScale() {
        scale = 1f
        matrix.reset() // Reset the matrix
        binding.fullImageView.imageMatrix = matrix // Apply the matrix
        binding.fullImageView.scaleType = ImageView.ScaleType.MATRIX // Use matrix scale type
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scale *= detector.scaleFactor
            scale = Math.max(1f, Math.min(scale, 10.0f)) // Ensure scale is at least 1

            // Create a matrix for scaling
            matrix.setScale(scale, scale, focusX, focusY)
            binding.fullImageView.imageMatrix = matrix
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            focusX = detector.focusX
            focusY = detector.focusY
            return true
        }
    }
}
