package com.tm470.cookhub.navdrawerfragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.tm470.cookhub.CurrentUser
import com.tm470.cookhub.R
import com.tm470.cookhub.ViewRecipeFragment
import com.tm470.cookhub.models.CookHubUser
import com.tm470.cookhub.models.Recipe
import com.xwray.groupie.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_public_recipes.*
import kotlinx.android.synthetic.main.recipe_row.view.*

class PublicRecipesFragment : Fragment() {

    var adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_public_recipes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().toolbar.title = "Public Recipes"

        adapter.clear()

        recyclerViewPublicRecipes.layoutManager = LinearLayoutManager(context)
        recyclerViewPublicRecipes.adapter = adapter

        displayRecipes()
    }

    private fun displayRecipes() {
        val ref = FirebaseDatabase.getInstance().getReference("/users/")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val usersList: MutableList<CookHubUser> = mutableListOf()
                snapshot.children.forEach {
                    usersList.add(it.getValue(CookHubUser::class.java)!!)
                }
                usersList.forEach {
                    val recipesRef = FirebaseDatabase.getInstance().getReference("/users/${it.uid}/recipes")
                    recipesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(
                            snapshot: DataSnapshot
                        ) {
                            snapshot.children.forEach {
                                adapter.apply {
                                    val recipe = it.getValue(Recipe::class.java)!!
                                    this.add(ExpandableGroup(NewExpandableHeaderItem(recipe)).apply {
                                        recipe.ingredients!!.forEach { it2 ->
                                            add(RecipesFragment.ChildItem(it2))
                                        }
                                    })
                                }
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
    }

    inner class NewExpandableHeaderItem(val recipe: Recipe): com.xwray.groupie.kotlinandroidextensions.Item(),
        ExpandableItem {

        private lateinit var expandableGroup: ExpandableGroup

        override fun bind(
            viewHolder: com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder,
            position: Int
        ) {
            viewHolder.root.recipeTitle.text = recipe.name
            viewHolder.root.recipeAuthor.text = recipe.author

            viewHolder.itemView.indicator.setOnClickListener {
                expandableGroup.onToggleExpanded()
                changeStuff(viewHolder)
            }

            viewHolder.itemView.setOnClickListener {
                CurrentUser.viewRecipe = recipe
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, ViewRecipeFragment(), "ViewRecipeFragment")
                    .addToBackStack("ViewRecipeFragment").commit()
            }
        }

        override fun getLayout() = R.layout.recipe_row

        private fun changeStuff(viewHolder: GroupieViewHolder) {
            viewHolder.root.indicator.apply {
                setImageResource(
                    if (expandableGroup.isExpanded) R.drawable.ic_baseline_arrow_drop_down_24
                    else R.drawable.ic_baseline_arrow_right_24
                )
            }
        }

        override fun setExpandableGroup(onToggleListener: ExpandableGroup) {
            this.expandableGroup = onToggleListener
        }
    }
}