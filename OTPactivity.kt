package com.example.chat_app

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.chat_app.databinding.CodeValidationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class OTPactivity : AppCompatActivity() {
    private lateinit var binding: CodeValidationBinding
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private var authId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CodeValidationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupOtpInputs()

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser
        authId = intent.getStringExtra("otpcr")!!
    }

    fun goBack(view: View){
        val goBackIntent=Intent(this,LoginActivity::class.java)
        startActivity(goBackIntent)
        finish()
    }

    private fun setupOtpInputs() {
        val editTexts = listOf(
            binding.codeedit1,
            binding.codeedit2,
            binding.codeedit3,
            binding.codeedit4,
            binding.codeedit5,
            binding.codeedit6
        )

        for (i in editTexts.indices) {
            editTexts[i].addTextChangedListener(GenericTextWatcher(editTexts, i))
        }
    }

    private class GenericTextWatcher(
        private val editTexts: List<EditText>,
        private val currentIndex: Int
    ) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s?.length == 1) {
                if (currentIndex < editTexts.size - 1) {
                    editTexts[currentIndex + 1].requestFocus()
                } else {
                    // All edit texts are filled, trigger the OTP verification
                    (editTexts[0].context as OTPactivity).verifyOtp()
                }
            }
        }

        override fun afterTextChanged(s: Editable?) {}
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    send_home()
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                }
            }
    }

    private fun send_home() {
        val loginIntent = Intent(this, ProfileActivity::class.java)
        startActivity(loginIntent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        if (currentUser != null) {
            send_home()
            finish()
        }
    }

    private fun verifyOtp() {
        val otp = binding.codeedit1.text.toString() + binding.codeedit2.text.toString() +
                binding.codeedit3.text.toString() + binding.codeedit4.text.toString() +
                binding.codeedit5.text.toString() + binding.codeedit6.text.toString()
        if (otp.isNotEmpty()) {
            val credential = PhoneAuthProvider.getCredential(authId, otp)
            signInWithPhoneAuthCredential(credential)
        }
    }
}
