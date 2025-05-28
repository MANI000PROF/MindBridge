package com.example.mindbridge

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mindbridge.databinding.ActivityOtpactivityBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit

class OTPActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpactivityBinding
    private var verificationId: String? = null
    private lateinit var auth: FirebaseAuth
    private var dialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialog = ProgressDialog(this@OTPActivity).apply {
            setMessage("Sending Message...")
            setCancelable(false)
            show()
        }

        auth = FirebaseAuth.getInstance()
        supportActionBar?.hide()

        val phoneNumber = intent.getStringExtra("phoneNumber")
        val email = intent.getStringExtra("Email")
        binding.otpEmail.text = email
        binding.otpMobile.text = phoneNumber

        if (phoneNumber.isNullOrEmpty()) {
            Toast.makeText(this, "Phone number is invalid", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            binding.phoneLabel.text = "Verify $phoneNumber"
        }

        // PhoneAuthOptions for OTP verification
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber!!) // Phone number to send the OTP to
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout for OTP
            .setActivity(this@OTPActivity) // Current activity context
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    dialog!!.dismiss()
                    Log.e("OTPActivity", "Verification Failed: ", e)
                    Toast.makeText(this@OTPActivity, "Verification Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onCodeSent(verifyId: String, forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
                    verificationId = verifyId
                    dialog!!.dismiss()
                    Log.d("OTPActivity", "Verification ID: $verificationId")
                    binding.otpdigit1.requestFocus()  // Move focus to OTP input fields
                }
            }).build()

        PhoneAuthProvider.verifyPhoneNumber(options)

        setupOtpFields()

        binding.continueBtn.setOnClickListener {
            val otp = getOtpFromFields()
            if (otp.length == 6 && verificationId != null) {
                val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
                signInWithCredential(credential)
            } else {
                Toast.makeText(this@OTPActivity, "Invalid OTP", Toast.LENGTH_SHORT).show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val currentUser = auth.currentUser
                val userId = currentUser?.uid

                if (userId != null) {
                    val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)
                    userRef.get().addOnCompleteListener { userTask ->
                        if (userTask.isSuccessful) {
                            // Navigate to Main Activity after successful sign-in
                            Toast.makeText(this, "Verification successful!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish() // Close OTP Activity
                        } else {
                            Toast.makeText(this, "Failed to get user data", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "User ID is null", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Verification failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getOtpFromFields(): String {
        return binding.otpdigit1.text.toString() +
                binding.otpdigit2.text.toString() +
                binding.otpdigit3.text.toString() +
                binding.otpdigit4.text.toString() +
                binding.otpdigit5.text.toString() +
                binding.otpdigit6.text.toString()
    }

    private fun setupOtpFields() {
        val otpFields = listOf(binding.otpdigit1, binding.otpdigit2, binding.otpdigit3, binding.otpdigit4, binding.otpdigit5, binding.otpdigit6)

        for (i in otpFields.indices) {
            otpFields[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1 && i < otpFields.size - 1) {
                        otpFields[i + 1].requestFocus()
                    } else if (s.isNullOrEmpty() && i > 0) {
                        otpFields[i - 1].requestFocus()
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }
}
