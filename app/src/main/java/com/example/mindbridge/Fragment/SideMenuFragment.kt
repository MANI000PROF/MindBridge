package com.example.mindbridge.Fragment

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.example.mindbridge.R

class SideMenuFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.SideMenuDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_side_menu, container, false)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            // Make it slide in from the left
            setGravity(Gravity.START)
            setLayout((resources.displayMetrics.widthPixels * 0.75).toInt(), ViewGroup.LayoutParams.MATCH_PARENT)
            setWindowAnimations(R.style.SideMenuAnimation)
        }
    }
}
