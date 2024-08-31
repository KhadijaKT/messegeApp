package com.example.chat_app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.chat_app.databinding.ProfileScreenBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class ProfileActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_READ_EXTERNAL_STORAGE = 101
    }

    private lateinit var binding: ProfileScreenBinding
    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ProfileScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.profileimg.setOnClickListener {
            checkGalleryPermission()
        }

        binding.savebt.setOnClickListener {
            saveUserProfile()
        }

        loadImage() // Load the image from shared preferences if available
    }

    fun goBack_profile(view: View) {
        val goBackIntent = Intent(this, LoginActivity::class.java)
        startActivity(goBackIntent)
        finish()
    }

    private fun send_home() {
        val loginIntent = Intent(this, ChatActivity::class.java)
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

    private fun checkGalleryPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_CODE_READ_EXTERNAL_STORAGE)
        } else {
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun saveImageUri(uri: Uri?) {
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sharedPref.edit().putString("profile_image_uri", uri.toString()).apply()
    }

    private fun loadImage() {
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val uriString = sharedPref.getString("profile_image_uri", null)
        uriString?.let {
            try {
                val uri = Uri.parse(it)
                binding.profileimg.setImageURI(uri)
            } catch (e: SecurityException) {
                Toast.makeText(this, "Unable to load image due to security restrictions", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            binding.profileimg.setImageURI(imageUri)
            saveImageUri(imageUri)
        }
    }

    private fun saveUserProfile() {
        val name = binding.firstnameet.text.toString().trim()
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val imageUriString = sharedPref.getString("profile_image_uri", null)

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUriString == null) {
            Toast.makeText(this, "Please select a profile image", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userProfile = UserProfile(userId, name, imageUriString)
        val database = FirebaseDatabase.getInstance().getReference("users")

        database.child(userId).setValue(userProfile)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile saved successfully", Toast.LENGTH_SHORT).show()
                send_home()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save profile", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
