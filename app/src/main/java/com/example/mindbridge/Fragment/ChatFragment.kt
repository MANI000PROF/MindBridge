package com.example.mindbridge.Fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mindbridge.MainActivity
import com.example.mindbridge.R
import com.example.mindbridge.adapter.chatUserAdapter
import com.example.mindbridge.chatActivity
import com.example.mindbridge.databinding.FragmentChatBinding
import com.example.mindbridge.model.Mentor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatFragment : Fragment() {
    private lateinit var binding: FragmentChatBinding
    private lateinit var chatUserAdapter: chatUserAdapter
    private val mentorList: MutableList<Mentor> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)

        setupRecyclerView()
        fetchMentorsFromFirebase()

        binding.viewProfileBtn.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        chatUserAdapter = chatUserAdapter(
            userList = mutableListOf(),
            userInfos = mutableListOf(),
            chatImage = mutableListOf()
        ) { userId, userName, userImage ->
            openChatActivity(userId, userName, userImage) // Updated to pass three parameters
        }

        binding.chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.chatRecyclerView.adapter = chatUserAdapter
    }


    private fun fetchMentorsFromFirebase() {
        val database = FirebaseDatabase.getInstance().getReference("mentors")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mentorList.clear() // Clear the list to avoid duplication
                val userNames = mutableListOf<String>()
                val userInfos = mutableListOf<String>()
                val userImages = mutableListOf<String>()
                val mentorIds = mutableListOf<String>()

                for (snapshot1 in snapshot.children) {
                    val mentor = snapshot1.getValue(Mentor::class.java)
                    mentor?.let {
                        userNames.add(it.name ?: "Unknown")
                        userInfos.add(it.qualification ?: "No Info")
                        userImages.add(it.profileImage ?: "")
                        mentorIds.add(snapshot1.key!!) // Store mentor ID
                        mentorList.add(it) // Store mentor for future reference
                    }
                }

                // Now fetch last message timestamps to sort mentors
                fetchLastMessageTimestamps(userNames, userInfos, userImages, mentorIds)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatFragment", "Error fetching mentors: ${error.message}")
            }
        })
    }

    private fun fetchLastMessageTimestamps(
        userNames: MutableList<String>,
        userInfos: MutableList<String>,
        userImages: MutableList<String>,
        mentorIds: List<String>
    ) {
        val lastMessagesMap = mutableMapOf<String, Long>()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val chatsDatabase = FirebaseDatabase.getInstance().getReference("chats")
        chatsDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                lastMessagesMap.clear()

                for (mentorId in mentorIds) {
                    val chatId = if (currentUserId < mentorId) "$currentUserId$mentorId" else "$mentorId$currentUserId"
                    val chatSnapshot = snapshot.child(chatId)

                    // Get the timestamp of the last message
                    val lastMsgTime = chatSnapshot.child("lastMsgTime").getValue(Long::class.java) ?: 0L
                    lastMessagesMap[mentorId] = lastMsgTime
                }

                // Now sort mentors by last message timestamp
                sortMentorsByLastMessage(userNames, userInfos, userImages, lastMessagesMap, mentorIds)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatFragment", "Error fetching last message timestamps: ${error.message}")
            }
        })
    }

    private fun sortMentorsByLastMessage(
        userNames: MutableList<String>,
        userInfos: MutableList<String>,
        userImages: MutableList<String>,
        lastMessagesMap: Map<String, Long>,
        mentorIds: List<String>
    ) {
        val sortedData = mentorList.map { mentor ->
            val lastMessageTime = lastMessagesMap[mentor.id] ?: 0L
            Pair(mentor, lastMessageTime)
        }.sortedByDescending { it.second }

        // Prepare sorted data for the adapter
        val sortedUserNames = sortedData.map { it.first.name ?: "Unknown" }
        val sortedUserInfos = sortedData.map { it.first.qualification ?: "No Info" }
        val sortedUserImages = sortedData.map { it.first.profileImage ?: "" }

        // Update the adapter with sorted data
        chatUserAdapter.updateData(sortedUserNames, sortedUserInfos, sortedUserImages)
    }

    private fun openChatActivity(userId: String, userName: String, userImage: String) {
        val intent = Intent(requireContext(), chatActivity::class.java).apply {
            putExtra("uid", userId)
            putExtra("name", userName)
            putExtra("image", userImage)
        }
        startActivity(intent)
    }
}
