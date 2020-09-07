package com.tm470.cookhub

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.tm470.cookhub.models.CookHubUser
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_new_conversation.*
import kotlinx.android.synthetic.main.friend_row.view.*
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class NewConversationFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_conversation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().toolbar.title = "New Conversation"

        displayFriends()
    }

    private fun displayFriends() {
        val adapter = GroupAdapter<GroupieViewHolder>()
        val list: MutableList<CookHubUser>? = mutableListOf()
        CurrentUser.friends!!.forEach { it ->
            FirebaseDatabase.getInstance().getReference("/users/$it")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        adapter.clear()
                        list!!.add(snapshot.getValue(CookHubUser::class.java)!!)
                        list.sortBy { it.username }
                        list.forEach { adapter.add(NewConversationItem(it)) }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }
        recyclerNewConversation.adapter = adapter
        recyclerNewConversation.layoutManager = LinearLayoutManager(context)

        recyclerNewConversation.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
    }

    inner class NewConversationItem(private val user: CookHubUser) : Item<GroupieViewHolder>() {
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.textViewFriendRow.text = user.username
            Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.imageViewFriendRow)

            viewHolder.itemView.setOnClickListener {

                // Check if current cid is available
                val ref = FirebaseDatabase.getInstance().getReference("/user-messages/${CurrentUser.user!!.uid}/${user.uid}")
                val otherRef = FirebaseDatabase.getInstance().getReference("/user-messages/${user.uid}/${CurrentUser.user!!.uid}")
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.forEach {
                            if (it.key == "cid") {
                                // Key exists from current user, open conversation
                                CurrentUser.cid = it.value.toString()
                                CurrentUser.currentChatUser = user
                                requireActivity().supportFragmentManager.beginTransaction()
                                    .replace(
                                        R.id.nav_host_fragment,
                                        ChatFragment(),
                                        "ChatFragment"
                                    ).addToBackStack("ChatFragment").commit()
                                return
                            }
                        }

                        val cid = UUID.randomUUID().toString()
                        CurrentUser.currentChatUser = user
                        CurrentUser.cid = cid
                        ref.child("cid").setValue(cid)
                        otherRef.child("cid").setValue(cid)

                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(
                                R.id.nav_host_fragment,
                                ChatFragment(),
                                "ChatFragment"
                            ).addToBackStack("ChatFragment").commit()
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            }
        }

        override fun getLayout() = R.layout.friend_row
    }
}