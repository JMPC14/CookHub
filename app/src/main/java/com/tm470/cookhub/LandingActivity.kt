package com.tm470.cookhub

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.tm470.cookhub.launcher.LauncherActivity
import com.tm470.cookhub.models.CookhubUser
import com.tm470.cookhub.models.Ingredient
import com.tm470.cookhub.models.Recipe
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_landing.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.latest_message_row.view.*
import kotlinx.android.synthetic.main.nav_header_main.*

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("SetTextI18n")
class LandingActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var appBarConfiguration: AppBarConfiguration

    private val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onResume() {
        super.onResume()
        if (CurrentUser.user != null) {
            listenForLatestMessages()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_latest_messages, R.id.nav_recipes, R.id.nav_profile
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener(this)

        val layoutManager = LinearLayoutManager(this)
        recyclerViewLanding.layoutManager = layoutManager
        recyclerViewLanding.adapter = adapter
        recyclerViewLanding.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        toolbar.title = "Latest Messages"

        fetchUser()

        buttonNewConversation.setOnClickListener {
            supportFragmentManager.beginTransaction().replace(
                R.id.nav_host_fragment,
                NewConversationFragment(),
                "NewConversationFragment"
            ).addToBackStack("NewConversationFragment").commit()
        }
    }

    private val latestMessageMap = HashMap<String, ChatMessage>()

    private fun refreshRecyclerViewMessages() {
        adapter.clear()
        val sortedMap = latestMessageMap.toList().sortedByDescending { it.second.time }.toMap()
        sortedMap.values.forEach { adapter.add(LatestMessageRow(it)) }
    }

    private fun listenForLatestMessages() {
        val uid = CurrentUser.user!!.uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$uid")

        fun addMessage(snapshot: DataSnapshot) {
            val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return

            latestMessageMap[snapshot.key!!] = chatMessage
            refreshRecyclerViewMessages()
        }

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                addMessage(snapshot)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                addMessage(snapshot)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    inner class LatestMessageRow(private val chatMessage: ChatMessage) : Item<GroupieViewHolder>() {
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            val chatOtherUserId: String?
            if (chatMessage.fromId == CurrentUser.user!!.uid) {
                chatOtherUserId  = chatMessage.toId
                viewHolder.itemView.textLatestMessageRow.text = ("You: ${chatMessage.text}")
            } else {
                chatOtherUserId = chatMessage.fromId
                viewHolder.itemView.textLatestMessageRow.text = ("Them: ${chatMessage.text}")
            }

            val ref = FirebaseDatabase.getInstance().getReference("/users/$chatOtherUserId")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val chatOtherUser = snapshot.getValue(CookhubUser::class.java)
                    viewHolder.itemView.usernameLatestMessageRow.text = chatOtherUser!!.username
                    Picasso.get().load(chatOtherUser.profileImageUrl)
                        .into(viewHolder.itemView.userImageLatestMessageRow)

                    viewHolder.itemView.setOnClickListener {
                        val cidRef = FirebaseDatabase.getInstance()
                            .getReference("/user-messages/${CurrentUser.user!!.uid}/$chatOtherUserId")
                        cidRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                CurrentUser.cid = snapshot.child("cid").value.toString()
                                CurrentUser.currentChatUser = chatOtherUser
                                supportFragmentManager.beginTransaction()
                                    .replace(R.id.nav_host_fragment, ChatFragment(), "ChatFragment")
                                    .addToBackStack("ChatFragment").commit()
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }

        override fun getLayout() = R.layout.latest_message_row
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
                        CurrentUser.user = snapshot.getValue(CookhubUser::class.java)
                        textViewNavHeaderMain.text = CurrentUser.user!!.username
                        fetchRecipes()
                        fetchIngredients()
                        fetchFriends()
                        listenForLatestMessages()
                    }
                })
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

    private fun fetchRecipes() {
        val ref = FirebaseDatabase.getInstance().getReference("/users/${CurrentUser.user!!.uid}/recipes")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val recipe = snapshot.getValue(Recipe::class.java)!!
                CurrentUser.recipes.add(recipe)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun fetchIngredients() {
        val ref = FirebaseDatabase.getInstance().getReference("/users/${CurrentUser.user!!.uid}/ingredients")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val ingredient = snapshot.getValue(Ingredient::class.java)!!
                CurrentUser.ingredients.add(ingredient)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.landing, menu)
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
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, ProfileFragment(), "ProfileFragment")
                    .addToBackStack("ProfileFragment").commit()
            }

            R.id.nav_latest_messages -> {
                supportFragmentManager.fragments.forEach {
                    supportFragmentManager.beginTransaction().remove(it).commit()
                }
                toolbar.title = "Latest Messages"
                listenForLatestMessages()
            }

            R.id.nav_friends -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, FriendsFragment(), "FriendsFragment")
                    .addToBackStack("FriendsFragment").commit()
                toolbar.title = "Friends"
            }

            R.id.nav_recipes -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, RecipesFragment(), "RecipesFragment")
                    .addToBackStack("RecipesFragment").commit()
                toolbar.title = "Recipes"
            }

            R.id.nav_ingredients -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, IngredientsFragment(), "IngredientsFragment")
                    .addToBackStack("IngredientsFragment").commit()
                toolbar.title = "Pantry"
            }
        }
        drawer_layout.closeDrawers()
        return true
    }

    override fun onBackPressed() {
        if (supportFragmentManager.fragments.size > 1) {
            supportFragmentManager.popBackStack()
            supportFragmentManager.beginTransaction()
                .remove(supportFragmentManager.fragments[1])
                .commit()
            if (supportFragmentManager.backStackEntryCount > 1) {
                val fragment =
                    supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 2)
                when (fragment.name) {
                    "ProfileFragment" -> {
                        nav_view.setCheckedItem(R.id.nav_profile)
                    }

                    "FriendsFragment" -> {
                        nav_view.setCheckedItem(R.id.nav_friends)
                    }

                    "RecipesFragment" -> {
                        nav_view.setCheckedItem(R.id.nav_recipes)
                    }

                    "IngredientsFragment" -> {
                        nav_view.setCheckedItem(R.id.nav_ingredients)
                    }
                }
            } else {
                nav_view.setCheckedItem(R.id.nav_latest_messages)
                listenForLatestMessages()
                toolbar.title = "Latest Messages"
            }
        } else {
            super.onBackPressed()
        }
    }
}