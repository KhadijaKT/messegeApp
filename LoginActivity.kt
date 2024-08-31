package com.example.chat_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.chat_app.databinding.PhoneValidationBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class LoginActivity:AppCompatActivity() {
    private lateinit var binding:PhoneValidationBinding
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser?=null
    private var phoneNum:String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=PhoneValidationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth=FirebaseAuth.getInstance()
        currentUser=auth.currentUser

    }

    fun goBack_login(view: View){
        val goBackIntent=Intent(this,MainActivityKotlin::class.java)
        startActivity(goBackIntent)
        finish()
    }
    fun send_home(){
        val loginIntent= Intent(this,ProfileActivity::class.java)
        startActivity(loginIntent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        if(currentUser!=null){
            send_home()
            finish()
        }
    }

    fun generate_OTP(view: View){
        phoneNum=binding.codeet.text.toString()+binding.numet.text.toString()
        if(phoneNum.isNotEmpty()){
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNum) // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this) // Activity (for callback binding)
                .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // Handle different cases of verification failure
            when (e) {
                is FirebaseAuthInvalidCredentialsException -> {
                    // Invalid request
                }
                is FirebaseTooManyRequestsException -> {
                    // The SMS quota for the project has been exceeded
                }
                is FirebaseAuthMissingActivityForRecaptchaException -> {
                    // reCAPTCHA verification attempted with null Activity
                }
            }
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            val otpIntent = Intent(this@LoginActivity, OTPactivity::class.java)
            otpIntent.putExtra("otpcr", verificationId)
            startActivity(otpIntent)
            finish()
        }
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
}