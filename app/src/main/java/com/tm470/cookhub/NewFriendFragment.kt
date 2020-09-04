package com.tm470.cookhub

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.auth.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.tm470.cookhub.models.CookhubUser
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_friends.*
import kotlinx.android.synthetic.main.fragment_new_friend.*
import kotlinx.android.synthetic.main.friend_row.view.*

@SuppressLint("RestrictedApi")
class NewFriendFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_friend, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().toolbar.title = "New Friend"

        displayUsers()
    }

    private fun displayUsers() {
        val adapter = GroupAdapter<GroupieViewHolder>()
        FirebaseDatabase.getInstance().getReference("/users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    adapter.clear()
                    snapshot.children.forEach {
                        val user = it.getValue(CookhubUser::class.java)
                        if (!CurrentUser.friends!!.contains(user!!.uid) && FirebaseAuth.getInstance().uid != user.uid) {
                            adapter.add(NewFriendItem(user))
                        }
                    }

                    adapter.setOnItemClickListener { item, view ->
                        val newFriendItem = item as NewFriendItem
                        val friendRef = FirebaseDatabase.getInstance()
                            .getReference("/users/${CurrentUser.user!!.uid}/friends")
                        friendRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                CurrentUser.friends!!.add(newFriendItem.friend.uid!!)
                                friendRef.setValue(CurrentUser.friends)
                                    .addOnSuccessListener {
                                        requireActivity().supportFragmentManager.beginTransaction()
                                            .remove(
                                                requireActivity().supportFragmentManager.findFragmentByTag(
                                                    "NewFriendFragment"
                                                )!!
                                            )
                                        requireActivity().supportFragmentManager.popBackStack()
                                    }
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        recyclerViewNewFriend.adapter = adapter
        recyclerViewNewFriend.layoutManager = LinearLayoutManager(context)
    }

    class NewFriendItem(val friend: CookhubUser): Item<GroupieViewHolder>() {
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.textViewFriendRow.text = friend.username
            Picasso.get().load(friend.profileImageUrl).into(viewHolder.itemView.imageViewFriendRow)
        }

        override fun getLayout() = R.layout.friend_row
    }
}