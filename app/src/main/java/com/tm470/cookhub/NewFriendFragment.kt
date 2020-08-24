package com.tm470.cookhub

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.tm470.cookhub.models.User
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.friend_row.*

class NewFriendFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_friend, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val usersRef = FirebaseDatabase.getInstance().getReference("/users")
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()
                snapshot.children.forEach {
                    val user = it.getValue(User::class.java)
                    if (user != null) {
                        val uid = user.uid
                        if (user.uid != FirebaseAuth.getInstance().uid && !CurrentUser.friends!!.contains(uid)) {
                            adapter.add(NewFriendItem(user))
                        }
                    }
                }
            }
        })
    }

    inner class NewFriendItem(private val user: User) : Item<GroupieViewHolder>() {
        override fun getLayout(): Int {
            return R.layout.friend_row
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            textViewFriendRow.text = user.username
            if (user.profileImage != null && user.profileImage!!.isNotEmpty()) {
                Picasso.get().load(user.profileImage).into(imageViewFriendRow)
            }
        }
    }
}