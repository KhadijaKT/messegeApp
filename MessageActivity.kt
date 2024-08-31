package com.example.chat_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.SupportActionModeWrapper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chat_app.databinding.MsgRecyclerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MessageActivity : AppCompatActivity() {

    private lateinit var binding: MsgRecyclerBinding
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var list: ArrayList<Message>
    private lateinit var myDBRef: DatabaseReference

    private var receiverRoom: String? = null
    private var senderRoom: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MsgRecyclerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name = intent.getStringExtra("name")
        val receiverId = intent.getStringExtra("uid")
        val senderId = FirebaseAuth.getInstance().currentUser?.uid

        myDBRef = FirebaseDatabase.getInstance().reference

        senderRoom = senderId + receiverId
        receiverRoom = receiverId + senderId

        supportActionBar?.title = name

        binding.personmsg.text=name

        list = ArrayList()
        messageAdapter = MessageAdapter(this, list)
        binding.recyclerMsg.layoutManager = LinearLayoutManager(this)
        binding.recyclerMsg.adapter = messageAdapter

        myDBRef.child("chats").child(senderRoom!!).child("messages").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                for (postSnapshot in snapshot.children) {
                    val message = postSnapshot.getValue(Message::class.java)
                    if (message != null) {
                        list.add(message)
                    }
                }
                messageAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })

        binding.sendicon.setOnClickListener {
            val message = binding.sendertv.text.toString()
            if (message.isNotEmpty()) {
                val messageObject = Message(message, senderId)

                // Push the message to both sender and receiver rooms
                myDBRef.child("chats").child(senderRoom!!).child("messages").push()
                    .setValue(messageObject).addOnSuccessListener {
                        myDBRef.child("chats").child(receiverRoom!!).child("messages").push()
                            .setValue(messageObject)
                    }
                // Clear the message box after sending
                binding.sendertv.text.clear()
            }
        }
    }
    fun goBack_chat(view: View){
        val goBackIntent= Intent(this,ChatActivity::class.java)
        startActivity(goBackIntent)
        finish()
    }
}
