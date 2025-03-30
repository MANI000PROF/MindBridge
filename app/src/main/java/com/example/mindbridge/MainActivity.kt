package com.example.mindbridge

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mindbridge.adapter.UserAdapter
import com.example.mindbridge.databinding.ActivityMainBinding
import com.example.mindbridge.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null
    private var database: FirebaseDatabase? = null
    private var users: ArrayList<User> = ArrayList()
    private lateinit var userAdapter: UserAdapter
    private var dialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding!!.root)
        setSupportActionBar(binding!!.toolbar)

        dialog = ProgressDialog(this@MainActivity)
        dialog!!.setMessage("Uploading Image...")
        dialog!!.setCancelable(false)

        database = FirebaseDatabase.getInstance()

        // Initializing the UserAdapter
        userAdapter = UserAdapter(this@MainActivity, users, object : UserAdapter.OnItemClickListener {
            override fun onItemClick(user: User) {
                // Start ChatActivity
                Log.d("UserAdapter", "Clicked on: ${user.name}")
                val intent = Intent(this@MainActivity, chatActivity::class.java).apply {
                    putExtra("name", user.name)
                    putExtra("image", user.profileImage)
                    putExtra("uid", user.uid)
                }
                startActivity(intent) // Open ChatActivity
            }

            override fun onItemLongPress(user: User) {
                showDeleteDialog(user) // Show delete dialog on long press
            }
        })


        binding!!.mRec.adapter = userAdapter // Set adapter
        binding!!.mRec.layoutManager = GridLayoutManager(this@MainActivity, 2)

        // Set initial visibility of the toolbar
        binding!!.toolbar.visibility = View.GONE // Make the toolbar hidden initially

        binding!!.userProfileImage.setOnClickListener {
            val intent = Intent(this, SetupProfileActivity::class.java)
            startActivity(intent)
        }


        // Add a listener to detect when the user scrolls
        binding!!.mRec.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Show the toolbar when scrolling up
                if (dy < 0 && binding!!.toolbar.visibility == View.GONE) {
                    binding!!.toolbar.visibility = View.VISIBLE
                }

                // Hide the toolbar when scrolling down
                if (dy > 0 && binding!!.toolbar.visibility == View.VISIBLE) {
                    binding!!.toolbar.visibility = View.GONE
                }
            }
        })

        // Listener for the users data
        database!!.reference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                users.clear()
                for (snapshot1 in snapshot.children) {
                    val user = snapshot1.getValue(User::class.java)
                    if (user != null && user.uid != FirebaseAuth.getInstance().uid) {
                        users.add(user)
                    }
                }
                Log.d("MainActivity", "Users count: ${users.size}")
                userAdapter.notifyDataSetChanged() // Refresh the adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Database error: ${error.message}")
            }
        })

        // Set padding to account for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }
    private fun loadCurrentUserDetails() {
        val currentUserId = FirebaseAuth.getInstance().uid
        if (currentUserId != null) {
            database!!.reference.child("users").child(currentUserId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentUser = snapshot.getValue(User::class.java)
                    currentUser?.let {
                        binding!!.userName.text = it.name // Set username
                        Glide.with(this@MainActivity)
                            .load(it.profileImage)
                            .placeholder(R.drawable.profile) // Default placeholder image
                            .error(R.drawable.profile) // Error image if loading fails
                            .into(binding!!.userProfileImage) // Load profile image
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MainActivity", "Error loading user details: ${error.message}")
                }
            })
        }
    }

    private fun populateMentorsFromUsers() {
        database!!.reference.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val mentorsRef = database!!.reference.child("mentors")
                mentorsRef.removeValue() // Clear old mentors data if you want to reset

                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null && user.uid != FirebaseAuth.getInstance().uid) {
                        // Create a Mentor object with additional fields like rank or expertise
                        val mentor = hashMapOf<String, Any?>(
                            "name" to user.name,
                            "profileImage" to user.profileImage,
                            "uid" to user.uid,
                            "qualification" to user.qualification,
                            "rank" to determineMentorRank(user) // Add rank based on your logic
                        )

                        // Add each mentor to the "mentors" node
                        mentorsRef.child(user.uid!!).setValue(mentor)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Error populating mentors: ${error.message}")
            }
        })
    }

    private fun determineMentorRank(user: User): String {
        // Logic to determine rank based on user data
        // Example: Rank can be set based on expertise or user ID
        // You can customize this function based on your requirement
        return "1" // Replace this with dynamic ranking logic
    }

    override fun onStart() {
        super.onStart()
        loadCurrentUserDetails() // Load user details when the activity starts
        populateMentorsFromUsers() // Populate mentors whenever MainActivity starts
    }

    private fun showDeleteDialog(user: User) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete User")
        builder.setMessage("Are you sure you want to delete ${user.name}?")
        builder.setPositiveButton("Yes") { dialog: DialogInterface, which: Int ->
            deleteUser(user) // Call the delete function
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog: DialogInterface, which: Int ->
            dialog.dismiss() // Dismiss the dialog
        }
        builder.show()
    }

    private fun deleteUser(user: User) {
        // Assuming users are stored under "users" in Firebase
        user.uid?.let {
            database!!.reference.child("users").child(it).removeValue()
                .addOnSuccessListener {
                    Log.d("MainActivity", "${user.name} deleted successfully")
                    users.remove(user) // Remove from local list
                    userAdapter.notifyDataSetChanged() // Notify adapter
                }
                .addOnFailureListener { error ->
                    Log.e("MainActivity", "Failed to delete user: ${error.message}")
                }
        }
    }

    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        if (currentId != null) {
            // User is logged in
            database!!.reference.child("presence").child(currentId).setValue("Online")
        } else {
            // User is logged out, redirect to OTPActivity
            val intent = Intent(this@MainActivity, OTPActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish() // Finish the MainActivity
        }
    }

    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        currentId?.let {
            database!!.reference.child("presence").child(it).setValue("Offline")
        }
    }

    // Inflate the menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // Handle menu item clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                showLogoutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout?")
        builder.setPositiveButton("Yes") { dialog: DialogInterface, which: Int ->
            FirebaseAuth.getInstance().signOut() // Sign out from Firebase
            val intent = Intent(this@MainActivity, Login::class.java) // Redirect to OTPActivity
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear the activity stack
            startActivity(intent)
            dialog.dismiss() // Dismiss the dialog
        }
        builder.setNegativeButton("No") { dialog: DialogInterface, which: Int ->
            dialog.dismiss() // Dismiss the dialog
        }
        builder.show()
    }

}
