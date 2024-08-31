package com.example.chat_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chat_app.databinding.ChatScreenBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ChatScreenBinding
    private lateinit var userAdapter: UserAdapter
    private lateinit var userList: MutableList<UserProfile>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.chat_screen)

        userList = mutableListOf()
        userAdapter = UserAdapter(userList) { user ->
            val intent = Intent(this, MessageActivity::class.java).apply {
                putExtra("name", user.name)
                putExtra("uid", user.userId)
            }
            startActivity(intent)
        }

        binding.recylerchat.layoutManager = LinearLayoutManager(this)
        binding.recylerchat.adapter = userAdapter

        loadUsers()
    }

    fun toChats(view:View){
        userAdapter.filter("")
    }


    fun filter(view: View) {
        val query = binding.searchv.text.toString()
        userAdapter.filter(query)
    }

    private fun loadUsers() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId == null) {
            // Handle the case where user is not logged in
            Log.e("ChatActivity", "No current user logged in.")
            return
        }

        val userStatusRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId)

        userStatusRef.child("online").setValue(true)

        userStatusRef.child("online").onDisconnect().setValue(false)

        val database = FirebaseDatabase.getInstance().getReference("users")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(UserProfile::class.java)
                    if (user != null) {
                        Log.d("ChatActivity", "User found: ${user.name}")
                        if (user.userId != currentUserId) {
                            userList.add(user)
                        }
                    } else {
                        Log.d("ChatActivity", "User data is null")
                    }
                }
                if (userList.isEmpty()) {
                    Log.d("ChatActivity", "No users found")
                }
                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChatActivity, "Failed to load users", Toast.LENGTH_SHORT).show()
                Log.e("ChatActivity", "Database error: ${error.message}")
            }
        })
    }

}
