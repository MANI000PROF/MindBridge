package com.example.mindbridge

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.mindbridge.databinding.ActivitySetupProfileBinding
import com.example.mindbridge.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class SetupProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private var selectedImage: Uri? = null
    private lateinit var dialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize binding
        binding = ActivitySetupProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ProgressDialog
        dialog = ProgressDialog(this)
        dialog.setMessage("Updating Profile...")
        dialog.setCancelable(false)

        // Initialize Firebase services
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        // Hide action bar
        supportActionBar?.hide()

        // Load existing user data
        loadUserData()

        // Set up profile image click listener
        binding.profileImage.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 45)
        }

        // Set up button click listener
        binding.setUpbtn.setOnClickListener {
            updateProfile()
        }
    }

    private fun loadUserData() {
        val uid = auth.uid
        if (uid != null) {
            database.reference.child("users").child(uid).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    user?.let {
                        binding.profileBox.setText(it.name)
                        binding.profileBoxAge.setText(it.age)
                        binding.profileBoxQualification.setText(it.qualification)
                        binding.profileBoxInterestedIn.setText(it.interestedIn)
                        // Load the profile image using Glide or similar library
                        Glide.with(this)
                            .load(it.profileImage)
                            .placeholder(R.drawable.profile) // Default placeholder
                            .into(binding.profileImage)
                    }
                }
            }
        }
    }

    private fun updateProfile() {
        val name: String = binding.profileBox.text.toString()
        val age: String = binding.profileBoxAge.text.toString()
        val qualification: String = binding.profileBoxQualification.text.toString()
        val interestedIn: String = binding.profileBoxInterestedIn.text.toString()

        if (name.isEmpty()) {
            binding.profileBox.error = "Please type a name"
            return
        }

        if (age.isEmpty() || qualification.isEmpty() || interestedIn.isEmpty()) {
            showToast("Please fill all fields")
            return
        }

        dialog.show()

        if (selectedImage != null) {
            // Upload profile image
            val reference = storage.reference.child("Profiles").child(auth.uid!!)
            reference.putFile(selectedImage!!).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    reference.downloadUrl.addOnCompleteListener { uriTask ->
                        if (uriTask.isSuccessful) {
                            val imageUrl = uriTask.result.toString()
                            updateUserInDatabase(name, imageUrl, age, qualification, interestedIn)
                        } else {
                            dialog.dismiss()
                            showToast("Failed to retrieve image URL")
                        }
                    }
                } else {
                    dialog.dismiss()
                    showToast("Image upload failed")
                }
            }
        } else {
            updateUserInDatabase(name, "No Image", age, qualification, interestedIn)
        }
    }

    private fun updateUserInDatabase(name: String, imageUrl: String, age: String, qualification: String, interestedIn: String) {
        val uid = auth.uid
        val user = User(uid, name, auth.currentUser?.phoneNumber, imageUrl, age, qualification, interestedIn)

        database.reference.child("users").child(uid!!).setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                dialog.dismiss()
                showToast("Profile updated successfully")
                navigateToMainActivity()
            } else {
                dialog.dismiss()
                showToast("Failed to update profile")
            }
        }
    }

    // Function to navigate to ChatUsersActivity
    private fun navigateToMainActivity() {
        val intent = Intent(this@SetupProfileActivity, ChatUsersActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Show a toast message
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @Deprecated("This method has been deprecated. Use ActivityResult API instead.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null && data.data != null) {
            selectedImage = data.data
            binding.profileImage.setImageURI(data.data)
        }
    }
}


