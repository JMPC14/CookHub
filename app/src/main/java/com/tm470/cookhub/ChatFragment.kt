package com.tm470.cookhub

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.JsonObject
import com.squareup.picasso.Picasso
import com.tm470.cookhub.models.CookhubUser
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.chat_message_from.view.*
import kotlinx.android.synthetic.main.chat_message_to.view.*
import kotlinx.android.synthetic.main.fragment_chat.*
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class ChatFragment : Fragment() {

    val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewChat.layoutManager = LinearLayoutManager(context)
        recyclerViewChat.adapter = adapter

        buttonSendMessage.setOnClickListener {
            sendMessage()
            recyclerViewChat.scrollToPosition(adapter.itemCount)
        }

        listenForMessages()
    }

    private fun sendMessage() {
        val messageText = editTextEnterMessage.text.toString()
        val fromId = FirebaseAuth.getInstance().uid!!
        val toId = CurrentUser.currentChatUser!!.uid!!
        val ref =
            FirebaseDatabase.getInstance().getReference("/conversations/${CurrentUser.cid}").push()
        val chatMessage: ChatMessage?
        val time = System.currentTimeMillis() / 1000
        val month = LocalDateTime.now().month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        val date = LocalDateTime.now().dayOfMonth
        val hour = LocalDateTime.now().hour
        val minute = LocalDateTime.now().minute
        val newHour = if (hour < 10) {
            "0$hour"
        } else {
            hour.toString()
        }
        val newMinute = if (minute < 10) {
            "0$minute"
        } else {
            minute.toString()
        }
        val timestamp = "$date $month, $newHour:$newMinute"

        chatMessage = ChatMessage(
            ref.key!!,
            messageText,
            fromId,
            toId,
            timestamp,
            time
        )

        ref.setValue(chatMessage)
            .addOnSuccessListener {
                editTextEnterMessage.text.clear()
            }

        val latestMessageRef =
            FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef =
            FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)
    }

    private fun listenForMessages() {
        var newRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("/conversations/${CurrentUser.cid}")
        newRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(
                snapshot: DataSnapshot,
                previousChildName: String?
            ) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                if (chatMessage != null) {
                    var sequentialFrom = false
                    var sequentialTo = false

                    if (adapter.itemCount != 0) {
                        /** Checks most recent chat message to determine which user sent it and doesn't display
                        profile picture for that user if they sent the most recent message. **/
                        val test = adapter.getItem(adapter.itemCount - 1)
                        when (test.layout) {
                            R.layout.chat_message_from,
                            R.layout.chat_message_from_sequential,
                            R.layout.chat_message_from_image,
                            R.layout.chat_message_from_image_sequential,
                            R.layout.chat_message_from_file,
                            R.layout.chat_message_from_file_sequential -> {
                                sequentialFrom = true
                            }
                            R.layout.chat_message_to,
                            R.layout.chat_message_to_sequential,
                            R.layout.chat_message_to_image,
                            R.layout.chat_message_to_image_sequential,
                            R.layout.chat_message_to_file,
                            R.layout.chat_message_to_file_sequential -> {
                                sequentialTo = true
                            }
                        }
                    }

                    val user: CookhubUser?
                    val sequential: Boolean?

                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        user = CurrentUser.user
                        sequential = sequentialFrom
                    } else {
                        user = CurrentUser.currentChatUser
                        sequential = sequentialTo
                    }

                    adapter.add(ChatItem(snapshot.key!!, chatMessage, user!!, sequential))
                }
            }

            override fun onChildChanged(
                snapshot: DataSnapshot,
                previousChildName: String?
            ) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(
                snapshot: DataSnapshot,
                previousChildName: String?
            ) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    inner class ChatItem(
        val id: String,
        val chatMessage: ChatMessage,
        val user: CookhubUser,
        private val sequential: Boolean
    ) : Item<GroupieViewHolder>() {

        override fun getLayout(): Int {
            return if (user.uid == CurrentUser.user!!.uid) {
                R.layout.chat_message_from
            } else {
                R.layout.chat_message_to
            }
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            if (layout == R.layout.chat_message_from || layout == R.layout.chat_message_from_sequential) {
                Picasso.get().load(user.profileImageUrl)
                        .into(viewHolder.itemView.imageMessageFrom)

                viewHolder.itemView.textMessageFrom.text = chatMessage.text
                viewHolder.itemView.timestampMessageFrom.text = chatMessage.timestamp

            } else if (layout == R.layout.chat_message_to || layout == R.layout.chat_message_to_sequential) {
                viewHolder.itemView.textMessageTo.text = chatMessage.text
                viewHolder.itemView.timestampMessageTo.text = chatMessage.timestamp

                if (!sequential) {
                    Picasso.get().load(user.profileImageUrl)
                        .into(viewHolder.itemView.imageMessageTo)
                }
            }
        }
    }
}