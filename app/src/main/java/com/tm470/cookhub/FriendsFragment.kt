package com.tm470.cookhub

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.PointerIcon
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.tm470.cookhub.models.CookhubUser
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.fragment_friends.*
import kotlinx.android.synthetic.main.friend_row.view.*

@SuppressLint("RestrictedApi")
class FriendsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_friends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        displayFriends()

        fabFriends.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, NewFriendFragment(), "NewFriendFragment")
                .addToBackStack("NewFriendFragment").commit()
        }
    }

    fun displayFriends() {
        val adapter = GroupAdapter<GroupieViewHolder>()
        val list: MutableList<CookhubUser>? = mutableListOf()
        CurrentUser.friends!!.forEach { it ->
            FirebaseDatabase.getInstance().getReference("/users/$it")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        adapter.clear()
                        list!!.add(snapshot.getValue(CookhubUser::class.java)!!)
                        list.sortBy { it.username }
                        list.forEach { adapter.add(FriendItem(it)) }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }
        recyclerViewFriends.adapter = adapter
        recyclerViewFriends.layoutManager = LinearLayoutManager(context)

        recyclerViewFriends.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
    }

    inner class FriendItem(private val friend: CookhubUser): Item<GroupieViewHolder>() {
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.textViewFriendRow.text = friend.username
            if (friend.profileImageUrl!!.isNotEmpty()) {
                Picasso.get().load(friend.profileImageUrl)
                    .into(viewHolder.itemView.imageViewFriendRow)
            }

            viewHolder.itemView.setOnLongClickListener {
                val pop = PopupMenu(it.context, it)
                pop.inflate(R.menu.friend_list_menu)
                pop.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.remove_friend -> {
                            CurrentUser.friends?.remove(friend.uid)
                            FirebaseDatabase.getInstance()
                                .getReference("/users/${CurrentUser.user?.uid}/friends")
                                .setValue(CurrentUser.friends)
                            displayFriends()
                        }
                    }
                    true
                }
                pop.show()
                true
            }
        }

        override fun getLayout() = R.layout.friend_row
    }
}