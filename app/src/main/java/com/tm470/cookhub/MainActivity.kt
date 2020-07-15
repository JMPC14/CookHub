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
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    private val adapter = GroupAdapter<GroupieViewHolder>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        recyclerViewRecipesList.layoutManager = layoutManager
        recyclerViewRecipesList.adapter = adapter

        fetchUser()
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
                    }
                })
            } else {
                fetchRecipesFile()
            }
        }
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