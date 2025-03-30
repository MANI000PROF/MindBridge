package com.example.mindbridge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mindbridge.adapter.ViewAdapter
import com.example.mindbridge.databinding.FragmentViewBottomSheetBinding
import com.example.mindbridge.model.Mentor
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.*

class ViewBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentViewBottomSheetBinding
    private lateinit var viewAdapter: ViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentViewBottomSheetBinding.inflate(inflater, container, false)

        setupRecyclerView()
        fetchMentorsFromFirebase()  // Fetch mentors from Firebase

        return binding.root
    }

    private fun setupRecyclerView() {
        viewAdapter = ViewAdapter(ArrayList(), ArrayList(), ArrayList())
        binding.viewRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.viewRecyclerView.adapter = viewAdapter
    }

    private fun fetchMentorsFromFirebase() {
        val database = FirebaseDatabase.getInstance().getReference("mentors")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userNames = mutableListOf<String>()
                val userInfos = mutableListOf<String>()
                val userImages = mutableListOf<String>() // Change to MutableList<String>

                for (snapshot1 in snapshot.children) {
                    val mentor = snapshot1.getValue(Mentor::class.java)
                    if (mentor != null) {
                        userNames.add(mentor.name ?: "Unknown")
                        userInfos.add(mentor.qualification ?: "No Info")
                        userImages.add(mentor.profileImage ?: "") // Store as String
                    }
                }

                // Update the adapter with real data
                viewAdapter.updateData(userNames, userInfos, userImages)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error if needed
            }
        })
    }
}
