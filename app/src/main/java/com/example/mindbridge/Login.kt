package com.example.mindbridge

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class Login : AppCompatActivity() {

    private lateinit var emailET: EditText
    private lateinit var passwordET: EditText
    private lateinit var loginBtn: Button
    private lateinit var signInWithGoogle: RelativeLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var passwordToggleBtn: ImageView
    private var isPasswordVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailET = findViewById(R.id.usernameET)
        passwordET = findViewById(R.id.passwordET)
        loginBtn = findViewById(R.id.signInBtn)
        signInWithGoogle = findViewById(R.id.signInWithGoogle)
        passwordToggleBtn = findViewById(R.id.eyeIcon)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Check if a user is already logged in
        if (auth.currentUser != null) {
            // If a user is already logged in, navigate to MainActivity
            val intent = Intent(this, MainPage::class.java)
            startActivity(intent)
            finish() // Close the login activity
        }

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Use your actual web client ID from Firebase console
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        loginBtn.setOnClickListener {
            loginUser()
        }

        passwordToggleBtn.setOnClickListener {
            togglePasswordVisibility()
        }

        signInWithGoogle.setOnClickListener {
            signInWithGoogle()
        }

        // Set up Forgot Password button click listener
        val forgotPasswordBtn: TextView = findViewById(R.id.forgotPasswordBtn)
        forgotPasswordBtn.setOnClickListener {
            showForgotPasswordDialog()
        }

        // Set up Sign Up button click listener
        val signUpBtn: TextView = findViewById(R.id.signUpBtn)
        signUpBtn.setOnClickListener {
            val intent = Intent(this, Register::class.java) // Navigate to Register Activity
            startActivity(intent)
        }


    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide password
            passwordET.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else {
            // Show password
            passwordET.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }
        // Move cursor to the end of the text
        passwordET.setSelection(passwordET.text.length)
        // Toggle the flag
        isPasswordVisible = !isPasswordVisible
    }

    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_forgot_password, null)
        builder.setView(dialogView)

        val emailEditText: EditText = dialogView.findViewById(R.id.emailEditText)

        builder.setTitle("Reset Password")
            .setMessage("Enter your email address to receive a password reset link.")
            .setPositiveButton("Send") { dialog, which ->
                val email = emailEditText.text.toString().trim()
                sendPasswordResetEmail(email)
            }
            .setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

        builder.show()
    }

    private fun sendPasswordResetEmail(email: String) {
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset email sent.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun loginUser() {
        val email = emailET.text.toString().trim()
        val password = passwordET.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Sign in the user
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login successful, redirect to Main page
                    val intent = Intent(this, MainPage::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Handle login failure
                    Toast.makeText(
                        this,
                        "Login failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            // Handle sign-in error
            Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential: AuthCredential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in success, update UI with the signed-in user's information
                    val user: FirebaseUser? = auth.currentUser
                    // Navigate to the Main Page or handle the logged-in user
                    Toast.makeText(this, "Sign in successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainPage::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Handle sign-in failure
                    Toast.makeText(
                        this,
                        "Authentication Failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}