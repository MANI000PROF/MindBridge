    package com.example.mindbridge

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mindbridge.adapter.NotificationAdapter
import com.example.mindbridge.databinding.FragmentNotificationBottomBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


    class notificationBottomFragment : BottomSheetDialogFragment() {
        private lateinit var binding: FragmentNotificationBottomBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNotificationBottomBinding.inflate(inflater, container, false)
        val notifications = listOf("Has joined recently in MindBridge", "is angry about the course", "has found the course interesting")
        val notificationImages = listOf(R.drawable.muichiro, R.drawable.sanemi, R.drawable.shinobu)
        val adapter = NotificationAdapter(
            ArrayList(notifications),
            ArrayList(notificationImages)
        )
        binding.notifcationRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.notifcationRecyclerView.adapter = adapter
        return binding.root
    }

    companion object {

    }
}