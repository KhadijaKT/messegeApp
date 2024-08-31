package com.example.chat_app

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chat_app.databinding.MsgFriendBinding
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter(private val context: Context, private val messageList: ArrayList<Message>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(val binding: MsgFriendBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = MsgFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val currentMsg = messageList[position]

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentMsg.senderId == currentUserId) {
           holder.binding.sendet.text=currentMsg.message
            holder.binding.sendet.visibility=View.VISIBLE
            holder.binding.receiveet.visibility=View.GONE
        } else {
            holder.binding.receiveet.text = currentMsg.message
            holder.binding.receiveet.visibility = View.VISIBLE
            holder.binding.sendet.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }
}
