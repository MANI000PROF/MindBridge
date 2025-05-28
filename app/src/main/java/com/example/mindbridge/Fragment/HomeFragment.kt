package com.example.mindbridge.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.mindbridge.R
import com.example.mindbridge.ViewBottomSheetFragment
import com.example.mindbridge.adapter.MentorSliderAdapter
import com.example.mindbridge.adapter.TrendMentorAdapter
import com.example.mindbridge.databinding.FragmentHomeBinding
import com.example.mindbridge.model.Mentor
import com.google.firebase.database.*

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var trendMentorAdapter: TrendMentorAdapter
    private val mentorList: MutableList<Mentor> = mutableListOf()
    private lateinit var mentorSliderAdapter: MentorSliderAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Bottom sheet button click event
        binding.viewAnalyticsBtn.setOnClickListener {
            val bottomSheetFragment = ViewBottomSheetFragment()
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupImageSlider()
        setupRecyclerView()
        // Initialize mentor slider recycler view horizontally
        binding.mentorSliderRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        mentorSliderAdapter = MentorSliderAdapter(mentorList)
        binding.mentorSliderRecycler.adapter = mentorSliderAdapter

        // Fetch mentors from Firebase
        fetchMentorsFromFirebase()
    }


    private fun setupImageSlider() {
        val imageList = ArrayList<SlideModel>()
        imageList.add(SlideModel(R.drawable.logo1, ScaleTypes.CENTER_CROP))
        imageList.add(SlideModel(R.drawable.banner1, ScaleTypes.CENTER_CROP))
        imageList.add(SlideModel(R.drawable.banner2, ScaleTypes.CENTER_CROP))

        binding.imageSlider.setImageList(imageList, ScaleTypes.FIT)
    }

    private fun setupRecyclerView() {
        trendMentorAdapter = TrendMentorAdapter(mentorList)
        binding.trendRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.trendRecyclerView.adapter = trendMentorAdapter
    }

    private fun fetchMentorsFromFirebase() {
        val database = FirebaseDatabase.getInstance().getReference("mentors")
        database.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                mentorList.clear()
                for (snapshot1 in snapshot.children) {
                    val mentor = snapshot1.getValue(Mentor::class.java)
                    if (mentor != null) {
                        mentorList.add(mentor)
                    }
                }
                trendMentorAdapter.notifyDataSetChanged()
                mentorSliderAdapter.notifyDataSetChanged() // Update horizontal slider too
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error if needed
            }
        })
    }

}
