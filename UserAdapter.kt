package com.example.chat_app

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chat_app.databinding.ChatBinding
import com.example.chat_app.databinding.ChatScreenBinding
import kotlinx.coroutines.newSingleThreadContext

class UserAdapter(
    private val userList: List<UserProfile>,
    private val onItemClick: (UserProfile) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var filteredList: List<UserProfile> = userList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = filteredList[position]
        holder.bind(user)


        holder.itemView.setOnClickListener {
            onItemClick(user)
        }
    }

    override fun getItemCount(): Int = filteredList.size

    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            userList
        } else {
            userList.filter {
                it.name?.contains(query, ignoreCase = true) ?: false
            }
        }
        notifyDataSetChanged()
    }

    class UserViewHolder(private val binding: ChatBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserProfile) {

            binding.personName.text = user.name


            if (user.profileImageUri != null && user.profileImageUri.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(user.profileImageUri)
                    .placeholder(R.drawable.vector)
                    .error(R.drawable.vector)
                    .into(binding.dp)
            } else {
                binding.dp.setImageResource(R.drawable.vector)
            }


            if (user.online == true) {
                binding.lastscenetv.text = "Online"
                binding.lastscenetv.setTextColor(ContextCompat.getColor(binding.root.context, R.color.dark_blue)) // Set color for online
            } else {
                binding.lastscenetv.text = "Offline"
                binding.lastscenetv.setTextColor(ContextCompat.getColor(binding.root.context, R.color.black)) // Set color for offline
            }
        }
    }
}
