package com.example.mindbridge

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit

data class User(val fullname: String, val email: String, val mobile: String)

class Register : AppCompatActivity() {

    private lateinit var fullnameET: EditText
    private lateinit var emailET: EditText
    private lateinit var mobileET: EditText
    private lateinit var passwordET: EditText
    private lateinit var repasswordET: EditText
    private lateinit var signUpBtn: Button
    private lateinit var passwordIcon: ImageView
    private lateinit var repasswordIcon: ImageView

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        fullnameET = findViewById(R.id.fullnameET)
        emailET = findViewById(R.id.emailET)
        mobileET = findViewById(R.id.mobileET)
        passwordET = findViewById(R.id.passwordET)
        repasswordET = findViewById(R.id.repasswordET)
        signUpBtn = findViewById(R.id.signUpBtn)
        passwordIcon = findViewById(R.id.passwordIcon)
        repasswordIcon = findViewById(R.id.repasswordIcon)

        passwordIcon.setOnClickListener {
            togglePasswordVisibility(passwordET)
        }

        repasswordIcon.setOnClickListener {
            togglePasswordVisibility(repasswordET)
        }

        auth = FirebaseAuth.getInstance()

        signUpBtn.setOnClickListener {
            registerUser()
        }

        val signInBtn: TextView = findViewById(R.id.signInBtn)
        signInBtn.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }

    private fun registerUser() {
        val fullname = fullnameET.text.toString().trim()
        val email = emailET.text.toString().trim()
        val mobile = mobileET.text.toString().trim()
        val password = passwordET.text.toString().trim()
        val repassword = repasswordET.text.toString().trim()

        if (fullname.isEmpty() || email.isEmpty() || mobile.isEmpty() || password.isEmpty() || repassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != repassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    if (signInMethods != null && signInMethods.isNotEmpty()) {
                        Toast.makeText(this, "User already exists. Please log in.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, Login::class.java)
                        startActivity(intent)
                    } else {
                        // User does not exist, proceed to register
                        createUser(email, password, fullname, mobile)
                    }
                } else {
                    Toast.makeText(this, "Failed to check if user exists", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun createUser(email: String, password: String, fullname: String, mobile: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    database = FirebaseDatabase.getInstance().getReference("Users")

                    userId?.let {
                        val user = User(fullname, email, mobile)
                        database.child(it).setValue(user).addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                // Navigate to OTPActivity directly after successful registration
                                navigateToOtpActivity(mobile) // Pass mobile to OTPActivity
                            } else {
                                Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun togglePasswordVisibility(editText: EditText) {
        if (editText.inputType == 129) {
            editText.inputType = 1
        } else {
            editText.inputType = 129
        }
        editText.setSelection(editText.text.length)
    }

    private fun navigateToOtpActivity(phoneNumber: String) {
        val intent = Intent(this, OTPActivity::class.java).apply {
            putExtra("phoneNumber", phoneNumber) // Pass the phone number to OTPActivity
            putExtra("Email", emailET.text.toString())
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        finish() // Finish the Register activity
    }
}
