package com.example.mindbridge

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.mindbridge.Fragment.SideMenuFragment
import com.example.mindbridge.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.fragmentContainerView4)
        val bottomNav = binding.bottomNavigationView

        binding.menuButton.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            // Play the Lottie animation once
            binding.menuButton.progress = 0f
            binding.menuButton.playAnimation()

            // Open the SideMenuFragment
            val sideMenu = SideMenuFragment()
            sideMenu.show(supportFragmentManager, "SideMenu")
        }

        bottomNav.setupWithNavController(navController)

        // Haptic + Bounce Animation on item selection
        bottomNav.setOnItemSelectedListener { item ->
            val view = bottomNav.findViewById<View>(item.itemId)
            view?.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)

            // Apply bounce animation to icon view inside BottomNavigationView
            val icon = view.findViewById<View>(com.google.android.material.R.id.icon)
            icon?.startAnimation(bounceAnimation())

            // Navigate using default behavior
            navController.navigate(item.itemId)
            true
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun bounceAnimation(): Animation {
        val anim = ScaleAnimation(
            1f, 1.3f, 1f, 1.3f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        anim.duration = 150
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = 1
        return anim
    }
}
