package com.tm470.cookhub

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.tm470.cookhub.models.ChatMessage
import com.tm470.cookhub.models.CookHubUser
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.chat_message_from.view.*
import kotlinx.android.synthetic.main.chat_message_from_image.view.*
import kotlinx.android.synthetic.main.chat_message_to.view.*
import kotlinx.android.synthetic.main.chat_message_to_image.view.*
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

        requireActivity().toolbar.title = CurrentUser.currentChatUser!!.username

        recyclerViewChat.layoutManager = LinearLayoutManager(context)
        recyclerViewChat.adapter = adapter

        buttonSendMessage.setOnClickListener {
            if (imageAttachedLayout.visibility == View.VISIBLE) {
                uploadImage()
            }
            if (recipeAttachedLayout.visibility == View.VISIBLE) {
//                uploadRecipe()
            }
            else if (imageAttachedLayout.visibility == View.INVISIBLE && recipeAttachedLayout.visibility == View.INVISIBLE) {
                sendMessage()
            }
            recyclerViewChat.scrollToPosition(adapter.itemCount)
        }

        buttonAttachPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            startActivityForResult(intent, 0)
        }

        editTextEnterMessage.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                buttonSendMessage.isEnabled = editTextEnterMessage.text.isNotEmpty() || photoAttachmentUri != null
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                buttonSendMessage.isEnabled = editTextEnterMessage.text.isNotEmpty() || photoAttachmentUri != null
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                buttonSendMessage.isEnabled = editTextEnterMessage.text.isNotEmpty() || photoAttachmentUri != null
            }
        })

        listenForMessages()
    }

    private var photoAttachmentUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            photoAttachmentUri = data.data
            Picasso.get().load(photoAttachmentUri).into(imageAttachedImageView)
            imageAttachedLayout.visibility = View.VISIBLE
            buttonSendMessage.isEnabled = true
        }
    }

    private fun uploadImage() {
        if (photoAttachmentUri == null) { return }
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(photoAttachmentUri!!).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener {
                CurrentUser.attachedImage = it.toString()
                sendMessage()
            }
        }
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

        chatMessage = when {
            CurrentUser.attachedImage != null -> {
                ChatMessage(
                    ref.key!!,
                    messageText,
                    fromId,
                    toId,
                    timestamp,
                    time,
                    CurrentUser.attachedImage!!
                )
            }
            CurrentUser.attachedRecipe != null -> {
                ChatMessage(
                    ref.key!!,
                    messageText,
                    fromId,
                    toId,
                    timestamp,
                    time,
                    CurrentUser.attachedRecipe!!
                )
            }
            else -> {
                ChatMessage(
                    ref.key!!,
                    messageText,
                    fromId,
                    toId,
                    timestamp,
                    time
                )
            }
        }

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

        CurrentUser.attachedImage = null
        CurrentUser.attachedRecipe = null
        imageAttachedLayout.visibility = View.INVISIBLE
        recipeAttachedLayout.visibility = View.INVISIBLE
        buttonSendMessage.isEnabled = false
    }

    private fun listenForMessages() {
        val newRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("/conversations/${CurrentUser.cid}")
        newRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(
                snapshot: DataSnapshot,
                previousChildName: String?
            ) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                if (chatMessage != null) {

                    val user: CookHubUser?

                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        user = CurrentUser.user
                    } else {
                        user = CurrentUser.currentChatUser
                    }

                    if (chatMessage.imageUrl != null) {
                        adapter.add(ChatItemImage(snapshot.key!!, chatMessage, user!!))
                    }
                    else if (chatMessage.recipe != null) {
                        // Add recipe item
                    } else {
                        adapter.add(ChatItem(snapshot.key!!, chatMessage, user!!))
                    }
                }

                recyclerViewChat.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onChildChanged(
                snapshot: DataSnapshot,
                previousChildName: String?
            ) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
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
        val user: CookHubUser
    ) : Item<GroupieViewHolder>() {

        override fun getLayout(): Int {
            return if (user.uid == CurrentUser.user!!.uid) {
                R.layout.chat_message_from
            } else {
                R.layout.chat_message_to
            }
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            if (layout == R.layout.chat_message_from) {
                Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.imageMessageFrom)

                viewHolder.itemView.textMessageFrom.text = chatMessage.text
                viewHolder.itemView.timestampMessageFrom.text = chatMessage.timestamp

            } else if (layout == R.layout.chat_message_to) {
                viewHolder.itemView.textMessageTo.text = chatMessage.text
                viewHolder.itemView.timestampMessageTo.text = chatMessage.timestamp

                Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.imageMessageTo)
            }
        }
    }

    inner class ChatItemImage(val id: String, val chatMessage: ChatMessage, val user: CookHubUser) : Item<GroupieViewHolder>() {

        override fun getLayout(): Int {
            return if (user.uid == CurrentUser.user!!.uid) {
                R.layout.chat_message_from_image
            } else {
                R.layout.chat_message_to_image
            }
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            if (layout == R.layout.chat_message_from_image) {
                if (chatMessage.text.isNotEmpty()) {
                    viewHolder.itemView.textMessageFromImage.text = chatMessage.text
                } else {
                    viewHolder.itemView.textMessageFromImage.height = 0
                }

                Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.imageFromImage)

                viewHolder.itemView.timestampMessageFromImage.text = chatMessage.timestamp
                Picasso.get().load(chatMessage.imageUrl).transform(RoundedCornersTransformation(20, 20))
                    .into(viewHolder.itemView.imageMessageFromImage)
            } else if (layout == R.layout.chat_message_to_image) {
                if (chatMessage.text.isNotEmpty()) {
                    viewHolder.itemView.textMessageToImage.text = chatMessage.text
                } else {
                    viewHolder.itemView.textMessageToImage.height = 0
                }

                Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.imageToImage)

                viewHolder.itemView.timestampMessageToImage.text = chatMessage.timestamp
                Picasso.get().load(chatMessage.imageUrl).transform(RoundedCornersTransformation(20, 20))
                    .into(viewHolder.itemView.imageMessageToImage)
            }
        }
    }
}