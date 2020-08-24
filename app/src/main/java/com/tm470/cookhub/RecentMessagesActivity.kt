package com.tm470.cookhub

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tm470.cookhub.launcher.LauncherActivity
import com.tm470.cookhub.models.User
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_recent_messages.*

class RecentMessagesActivity : AppCompatActivity() {


    private val adapter = GroupAdapter<GroupieViewHolder>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recent_messages)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        recyclerViewRecentMessages.layoutManager = layoutManager
        recyclerViewRecentMessages.adapter = adapter

        hideFragment(this, newConversationFragment)

        fetchUser()

        fabNewConversation.setOnClickListener {
            showFragment(this, newConversationFragment)
        }
    }

    private fun fetchUser() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, LauncherActivity::class.java)
            intent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TASK).or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
            FirebaseDatabase.getInstance().getReference("/online-users/$uid").setValue(true)
            if (CurrentUser.user == null) {
                val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
                ref.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        CurrentUser.user = snapshot.getValue(User::class.java)
                        fetchRecipesFile()
                        fetchContacts()
                    }
                })
            } else {
                fetchRecipesFile()
            }
        }
    }

    fun fetchContacts() {
        val friendsRef = FirebaseDatabase.getInstance().getReference("/users/${CurrentUser.user!!.uid}/friends")
        friendsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().uid}/friends").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        CurrentUser.friends = mutableListOf()
                        snapshot.children.forEach {
                            CurrentUser.friends?.add(it.value.toString())
                        }
                    }
                })
            }
        })
    }

    private fun fetchRecipesFile() {
        val file = baseContext.getFileStreamPath("${CurrentUser.user!!.uid}-recipes.txt")
        if (file.exists()) {
            /** Parse JSON with Klaxon **/
//            Klaxon().parse<RecipeContainer>(file)
        } else {
            try {
                val fileOutputStream = baseContext.openFileOutput(
                    "${CurrentUser.user!!.uid}-recipes.txt", Context.MODE_PRIVATE)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.optionsSignOut -> {
                FirebaseAuth.getInstance().signOut()
                FirebaseDatabase.getInstance().getReference("/online-users/${CurrentUser.user!!.uid}").setValue(false)
                CurrentUser.user = null
                val intent = Intent(this, LauncherActivity::class.java)
                intent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TASK) or (Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            R.id.item2 -> {
            }
            R.id.item3 -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }
}