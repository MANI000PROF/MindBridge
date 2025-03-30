package com.example.mindbridge

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.mindbridge.adapter.MessageAdapter
import com.example.mindbridge.databinding.ActivityChatBinding
import com.example.mindbridge.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.HashMap
import java.util.Hashtable
import java.util.Locale
import java.util.logging.Handler

class chatActivity : AppCompatActivity() {

    var binding: ActivityChatBinding? = null
    var adapter: MessageAdapter? = null
    var message: ArrayList<Message>? = null
    var senderRoom: String? = null
    var receiverRoom: String? = null
    var database: FirebaseDatabase? = null
    var storage: FirebaseStorage? = null
    var dialog: ProgressDialog? = null
    var selectedImage: Uri? = null
    var senderUid: String? = null
    var receiverUid: String? = null
    val handler = android.os.Handler()
    private val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var imageUri: Uri
    private val CAMERA_PERMISSION_CODE = 100
    private val STORAGE_PERMISSION_CODE = 101
    private var replyingToMessage: Message? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setSupportActionBar(binding!!.toolbar)

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        dialog = ProgressDialog(this@chatActivity)
        dialog!!.setMessage("Uploading Image...")
        dialog!!.setCancelable(false)

        message = ArrayList()

        val rootView = findViewById<RelativeLayout>(R.id.main)
        val linear2 = findViewById<LinearLayout>(R.id.linear2)

        // Listen for layout changes to detect the keyboard
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootView.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            if (keypadHeight > screenHeight * 0.15) {
                // Keyboard is open, move the linear2 layout above the keyboard
                linear2.translationY = -keypadHeight.toFloat()
            } else {
                // Keyboard is closed, reset linear2 to its original position
                linear2.translationY = 0f
            }
        }

        val cameraIcon: ImageView = findViewById(R.id.camera)
        cameraIcon.setOnClickListener {
            checkPermissions()
        }

        val name = intent.getStringExtra("name")
        val profile = intent.getStringExtra("image")
        binding!!.name.text = name
        Glide.with(this@chatActivity).load(profile)
            .placeholder(R.drawable.profile)
            .into(binding!!.profile1)

        binding!!.imageView.setOnClickListener { finish() }

        senderUid = FirebaseAuth.getInstance().uid
        receiverUid = intent.getStringExtra("uid")
        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid

        // Set up the adapter once
        adapter = MessageAdapter(this@chatActivity, message!!, senderRoom!!, receiverRoom!!)
        binding!!.recyclerView.layoutManager = LinearLayoutManager(this@chatActivity)
        binding!!.recyclerView.adapter = adapter

        adapter!!.setOnMessageLongClickListener { msg ->
            val messageId = msg.messageId
            val senderId = msg.senderId // Get the sender's ID from the message object
            val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid // Get the current user's ID

            val dialogBuilder = AlertDialog.Builder(this@chatActivity)
                .setTitle("Delete Message")

            // Check if the current user is the sender of the message
            if (senderId == currentUserId) {
                // The message was sent by the current user, so show both options
                dialogBuilder.setItems(arrayOf("Delete for Yourself", "Delete for Everyone")) { _, which ->
                    when (which) {
                        0 -> { // "Delete for Yourself"
                            if (messageId != null) {
                                // Delete the message only from the sender's room (current user)
                                database!!.reference.child("chats").child(senderRoom!!)
                                    .child("messages").child(messageId).removeValue()
                            }
                        }
                        1 -> { // "Delete for Everyone"
                            if (messageId != null) {
                                // Delete the message from both sender's and receiver's chat
                                database!!.reference.child("chats").child(senderRoom!!)
                                    .child("messages").child(messageId).removeValue()
                                database!!.reference.child("chats").child(receiverRoom!!)
                                    .child("messages").child(messageId).removeValue()
                            }
                        }
                    }
                }
            } else {
                // The message was sent by the other person, so show only "Delete for Yourself"
                dialogBuilder.setItems(arrayOf("Delete for Yourself")) { _, _ ->
                    if (messageId != null) {
                        // Delete the message only from the current user's chat
                        database!!.reference.child("chats").child(senderRoom!!)
                            .child("messages").child(messageId).removeValue()
                    }
                }
            }

            dialogBuilder.setNegativeButton("Cancel", null).create().show()
        }

        // Listen for presence status
        database!!.reference.child("presence").child(receiverUid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val status = snapshot.getValue(String::class.java)
                        if (status == null || status == "Offline" || status == "offline") {
                            binding!!.status.visibility = View.GONE // Hide status when offline
                        } else {
                            // Show online or other custom statuses
                            binding!!.status.text = status
                            binding!!.status.visibility = View.VISIBLE
                        }
                    } else {
                        // If no status is found, treat it as offline
                        binding!!.status.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle the error (you can log or show a message)
                }
            })


        // Fetch messages
        database!!.reference.child("chats").child(senderRoom!!)
            .child("messages")  // Ensure using correct path
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("ChatActivity", "onDataChange triggered")
                    message!!.clear()
                    for (snapshot1 in snapshot.children) {
                        val msg: Message? = snapshot1.getValue(Message::class.java)
                        msg?.messageId = snapshot1.key
                        msg?.let { message!!.add(it) }
                    }
                    Log.d("ChatActivity", "Messages size: ${message!!.size}")
                    adapter!!.notifyDataSetChanged()
                    if (message!!.isNotEmpty()) {
                        binding!!.recyclerView.scrollToPosition(message!!.size - 1)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatActivity", "Error: ${error.message}")
                }
            })

        // Sending a message
        binding!!.send.setOnClickListener {
            val messageTxt: String = binding!!.messageBox.text.toString().trim() // Trim to check for empty messages
            if (messageTxt.isNotEmpty()) { // Only proceed if the message is not empty
                sendMessage(messageTxt, replyingToMessage) // Call your new function
            } else {
                // Optionally, show a message or toast saying the message cannot be empty
            }
        }

        // Attachment handling
        binding!!.attachment.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 25)
        }

        // Typing status logic
        binding!!.messageBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                database!!.reference.child("presence").child(senderUid!!).setValue("typing...")
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedTyping, 1000)
            }

            var userStoppedTyping = Runnable {
                database!!.reference.child("presence").child(senderUid!!).setValue("online")
            }
        })

        supportActionBar!!.setDisplayShowTitleEnabled(false)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun sendMessage(messageTxt: String, replyingToMessage: Message?) {
        val date = Date()
        val message = if (replyingToMessage != null) {
            Message(messageTxt, senderUid, date.time, replyingToMessage.messageId).apply {
                // Reset after sending
                this@chatActivity.replyingToMessage = null
            }
        } else {
            Message(messageTxt, senderUid, date.time)
        }

        // Clear the message box
        binding!!.messageBox.setText("")

        val randomKey = database!!.reference.push().key
        val lastMsgObj = HashMap<String, Any>().apply {
            put("lastMsg", message.message!!)
            put("lastMsgTime", date.time)
        }

        // Update both sender and receiver rooms
        database!!.reference.child("chats").child(senderRoom!!).updateChildren(lastMsgObj)
        database!!.reference.child("chats").child(receiverRoom!!).updateChildren(lastMsgObj)

        // Save the message
        database!!.reference.child("chats").child(senderRoom!!)
            .child("messages").child(randomKey!!).setValue(message)
            .addOnSuccessListener {
                database!!.reference.child("chats").child(receiverRoom!!)
                    .child("messages").child(randomKey).setValue(message)
            }
    }


    fun setReplyMessage(message: Message) {
        // Fetch the username based on senderId
        val databaseReference = FirebaseDatabase.getInstance().getReference("users").child(message.senderId!!)

        databaseReference.get().addOnSuccessListener { dataSnapshot ->
            val username = dataSnapshot.child("name").value.toString() // Assuming "name" field exists in the user's profile

            val replyText = "Replying to: $username: ${message.message}"

            // Set the reply message in your reply layout
            binding!!.replyLayout.visibility = View.VISIBLE
            binding!!.replyText.text = replyText // Display the predefined reply text

            // Add functionality to send the reply
            binding!!.send.setOnClickListener {
                sendReply(message)
            }
        }.addOnFailureListener {
            // Handle the failure of fetching the username
            binding!!.replyText.text = "Replying to: ${message.senderId}: ${message.message}"
        }
    }


    private fun sendReply(originalMessage: Message) {
        // Set the send button click listener
        binding!!.send.setOnClickListener {
            val messageTxt: String = binding!!.messageBox.text.toString().trim()  // Get the message input and trim it
            if (messageTxt.isNotEmpty()) {  // Ensure the message is not empty
                // Call your new function to send the message, and include the reply to the original message
                sendMessage(messageTxt, originalMessage)

                // Only clear the reply and hide the reply layout AFTER the message has been successfully sent
                clearReply()
                binding!!.replyLayout.visibility = View.GONE // Hide the reply layout after sending
            } else {
                Log.d("ChatActivity", "Message cannot be empty") // Optionally log or show a toast
            }
        }
    }

    private fun clearReply() {
        replyingToMessage = null
        binding!!.messageBox.setText("") // Clear the input box
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_chat, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_all_messages -> {
                showConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

//    private fun checkPermissions() {
//        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
//        } else if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
//        } else {
//            openCamera()
//        }
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            CAMERA_PERMISSION_CODE -> {
//                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    openCamera()
//                } else {
//                    // Handle permission denied case
//                }
//            }
//            STORAGE_PERMISSION_CODE -> {
//                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    openCamera()
//                } else {
//                    // Handle permission denied case
//                }
//            }
//        }
//    }
//
//
//    private fun openCamera() {
//        try {
//            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            if (cameraIntent.resolveActivity(packageManager) != null) {
//                // Create a file to store the photo
//                val photoFile = createImageFile()
//                photoFile?.let {
//                    imageUri = FileProvider.getUriForFile(
//                        this,
//                        "com.example.mindbridge.fileprovider",
//                        it
//                    )
//                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
//                    startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
//                }
//            } else {
//                Log.e("CameraError", "No camera app available")
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Log.e("CameraError", "Error launching camera: ${e.localizedMessage}")
//        }
//    }

    private fun checkPermissions() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
        } else {
            openCamera()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_CODE, STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()  // Call openCamera for both permission requests
                } else {
                    // Handle permission denied case
                }
            }
        }
    }

    private fun openCamera() {
        try {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (cameraIntent.resolveActivity(packageManager) != null) {
                // Create a new file for the photo each time
                val photoFile = createImageFile()
                photoFile?.let {
                    imageUri = FileProvider.getUriForFile(
                        this,
                        "com.example.mindbridge.fileprovider",
                        it
                    )
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                    startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
                }
            } else {
                Log.e("CameraError", "No camera app available")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("CameraError", "Error launching camera: ${e.localizedMessage}")
        }
    }


    private fun createImageFile(): File? {
        return try {
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        } catch (ex: IOException) {
            ex.printStackTrace()
            Log.e("CameraError", "Error creating image file: ${ex.localizedMessage}")
            null
        }
    }


    private fun showConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Clear All Messages")
            .setMessage("Are you sure you want to clear all messages? This action cannot be undone.")
            .setPositiveButton("Yes") { _, _ -> clearAllMessages() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun clearAllMessages() {
        // Remove messages from the sender's room
        database!!.reference.child("chats").child(senderRoom!!).child("messages").removeValue()
        // Remove messages from the receiver's room
        database!!.reference.child("chats").child(receiverRoom!!).child("messages").removeValue()

        // Clear the local message list and notify the adapter
        message!!.clear()
        adapter!!.notifyDataSetChanged()
    }

    // Clean up handler when activity is destroyed
    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    // Sending image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Handle camera capture
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            selectedImage = imageUri // Use the captured image URI
            uploadImage(selectedImage!!)
        }

        // Handle image selection from gallery
        if (requestCode == 25 && resultCode == RESULT_OK && data != null) {
            selectedImage = data.data // Get the URI of the selected image
            uploadImage(selectedImage!!)
        }
    }

    private fun uploadImage(imageUri: Uri) {
        dialog!!.show()
        val calendar = Calendar.getInstance()
        val reference = storage!!.reference.child("chats").child(calendar.timeInMillis.toString())

        reference.putFile(imageUri)
            .addOnCompleteListener { task ->
                dialog!!.dismiss()
                if (task.isSuccessful) {
                    reference.downloadUrl.addOnSuccessListener { uri ->
                        val filePath = uri.toString()
                        val messageTxt: String = binding!!.messageBox.text.toString()
                        val date = Date()
                        val message = Message(messageTxt, senderUid, date.time)
                        message.message = "photo"
                        message.imageUrl = filePath
                        binding!!.messageBox.setText("")

                        val randomKey = database!!.reference.push().key
                        val lastMsgObj = HashMap<String, Any>()
                        lastMsgObj["lastMsg"] = message.message!!
                        lastMsgObj["lastMsgTime"] = date.time

                        database!!.reference.child("chats").child(senderRoom!!)
                            .updateChildren(lastMsgObj)
                        database!!.reference.child("chats").child(receiverRoom!!)
                            .updateChildren(lastMsgObj)

                        database!!.reference.child("chats").child(senderRoom!!)
                            .child("messages").child(randomKey!!).setValue(message)
                            .addOnSuccessListener {
                                database!!.reference.child("chats").child(receiverRoom!!)
                                    .child("messages").child(randomKey).setValue(message)
                            }
                    }
                }
            }
    }


    override fun onStart() {
        super.onStart()
        val currentId = FirebaseAuth.getInstance().uid
        if (currentId != null) {
            // Update the status when connected
            val userStatusRef = database!!.reference.child("presence").child(currentId)

            // Use Firebase onDisconnect to ensure that the user is marked "offline" if the app crashes or disconnects
            userStatusRef.onDisconnect().setValue("Offline")

            // Mark the user as "Online" when the activity starts
            userStatusRef.setValue("Online")
        }
    }

    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        if (currentId != null) {
            // Mark the user as "Online" when the activity is resumed
            database!!.reference.child("presence").child(currentId).setValue("Online")
        }
    }

    override fun onPause() {
        super.onPause()
        // It's optional to update the status here. Let `onStop()` handle setting the user as "Offline."
    }

    override fun onStop() {
        super.onStop()
        val currentId = FirebaseAuth.getInstance().uid
        if (currentId != null) {
            // Mark the user as "Offline" when the activity is stopped (no longer visible)
            database!!.reference.child("presence").child(currentId).setValue("Offline")
        }
    }

}


