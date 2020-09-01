package com.tm470.cookhub

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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
import kotlinx.android.synthetic.main.activity_landing.*
import kotlinx.android.synthetic.main.content_main.*

class LandingActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var appBarConfiguration: AppBarConfiguration

    private val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_latest_messages, R.id.nav_recipes, R.id.nav_profile
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener(this)

        val fab: FloatingActionButton = findViewById(R.id.fab)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        recyclerViewLanding.layoutManager = layoutManager
        recyclerViewLanding.adapter = adapter

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
                        fetchFriends()
                    }
                })
            } else {
                fetchRecipesFile()
            }
        }
    }

    fun fetchFriends() {
        val friendsRef = FirebaseDatabase.getInstance().getReference("/users/${CurrentUser.user!!.uid}/friends")
        friendsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().uid}/friends").addListenerForSingleValueEvent(object :
                    ValueEventListener {
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.landing, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> {
                supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment, ProfileFragment()).commit()
            }

            R.id.nav_latest_messages -> {
                supportFragmentManager.fragments.forEach {
                    supportFragmentManager.beginTransaction().remove(it).commit()
                }
            }

            R.id.nav_contacts -> {
                supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment, ContactsFragment()).commit()
            }

            R.id.nav_recipes -> {
                supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment, RecipesFragment()).commit()
            }

            R.id.nav_ingredients -> {
                supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment, IngredientsFragment()).commit()
            }
        }
        drawer_layout.closeDrawers()
        return true
    }
}