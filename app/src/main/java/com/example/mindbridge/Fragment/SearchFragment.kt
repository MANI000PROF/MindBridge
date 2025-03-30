package com.example.mindbridge.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mindbridge.R
import com.example.mindbridge.adapter.ViewAdapter
import com.example.mindbridge.databinding.FragmentSearchBinding
import com.example.mindbridge.model.Mentor
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var adapter: ViewAdapter
    private val mentorsList = mutableListOf<Mentor>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        setupRecyclerView()
        setupSearchView()
        fetchMentorsFromFirebase()
        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = ViewAdapter(ArrayList(), ArrayList(), ArrayList())
        binding.searchRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.searchRecyclerView.adapter = adapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterMentors(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterMentors(newText)
                return true
            }
        })
    }

    private fun filterMentors(query: String?) {
        val filteredMentors = mentorsList.filter {
            it.name?.contains(query ?: "", ignoreCase = true) == true
        }

        val filteredNames = filteredMentors.map { it.name ?: "Unknown" }
        val filteredInfos = filteredMentors.map { it.qualification ?: "No Info" }
        val filteredImages = filteredMentors.map { it.profileImage ?: "" }

        adapter.updateData(filteredNames, filteredInfos, filteredImages)
    }

    private fun fetchMentorsFromFirebase() {
        val database = FirebaseDatabase.getInstance().getReference("mentors")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mentorsList.clear() // Clear existing list before adding new data
                for (snapshot1 in snapshot.children) {
                    val mentor = snapshot1.getValue(Mentor::class.java)
                    mentor?.let { mentorsList.add(it) }
                }
                // Initial load
                filterMentors("")
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error if needed
            }
        })
    }
}
