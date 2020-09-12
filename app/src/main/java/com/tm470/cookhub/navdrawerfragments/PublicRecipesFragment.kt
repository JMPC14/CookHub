package com.tm470.cookhub.navdrawerfragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.tm470.cookhub.CurrentUser
import com.tm470.cookhub.R
import com.tm470.cookhub.ViewRecipeFragment
import com.tm470.cookhub.models.CookHubUser
import com.tm470.cookhub.models.Ingredient
import com.tm470.cookhub.models.Quantity
import com.tm470.cookhub.models.Recipe
import com.xwray.groupie.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_public_recipes.*
import kotlinx.android.synthetic.main.recipe_row.view.*
import kotlinx.android.synthetic.main.recipe_row_child.view.*

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

        imageViewInfo.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
            val newDialog = dialog.create()
            val dialogView = layoutInflater.inflate(R.layout.layout_dialog, null)
            val buttonInfo = dialogView.findViewById<Button>(R.id.buttonInfo)
            newDialog.setView(dialogView)
            buttonInfo.setOnClickListener {
                newDialog.cancel()
            }
            newDialog.setCancelable(false)
            newDialog.show()
        }

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
                                    if (recipe.public!!) {
                                        this.add(ExpandableGroup(NewExpandableHeaderItem(recipe)).apply {
                                            recipe.ingredients!!.forEach { it2 ->
                                                add(PublicChildItem(it2))
                                            }
                                        })
                                    }
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

    open class PublicChildItem(private val ingredient: Ingredient) : Item<GroupieViewHolder>() {

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.root.ingredientTitle.text = ingredient.name
            viewHolder.root.ingredientQuantity.text = ingredient.quantity!!.amount.toString()
            viewHolder.root.ingredientQuantityType.text = ingredient.quantity!!.type

            CurrentUser.ingredients!!.forEach {
                if (it.name.equals(ingredient.name, true)) {
                    if (compareUnits(it.quantity!!, ingredient.quantity!!)) {
                        viewHolder.root.imageViewCheckMarkGreen.visibility = View.VISIBLE
                        viewHolder.root.imageViewCheckMarkOrange.visibility = View.GONE
                    } else {
                        viewHolder.root.imageViewCheckMarkOrange.visibility = View.VISIBLE
                        viewHolder.root.imageViewCheckMarkGreen.visibility = View.GONE
                    }
                } else {
                    viewHolder.root.imageViewCheckMarkOrange.visibility = View.GONE
                    viewHolder.root.imageViewCheckMarkGreen.visibility = View.GONE
                }
            }
        }

        private fun compareUnits(first: Quantity, second: Quantity): Boolean {
            when (first.type) {
                "g" -> {
                    when (second.type) {
                        "kg" -> {
                            return (first.amount * 0.001 > second.amount)
                        }

                        "oz" -> {
                            return (first.amount * 0.035 > second.amount)
                        }

                        "lbs" -> {
                            return (first.amount * 0.0022 > second.amount)
                        }
                    }
                    return first.amount > second.amount
                }

                "kg" -> {
                    when (second.type) {
                        "g" -> {
                            return (first.amount * 10000 > second.amount)
                        }

                        "oz" -> {
                            return (first.amount * 35.374 > second.amount)
                        }

                        "lbs" -> {
                            return (first.amount * 2.2204 > second.amount)
                        }
                    }
                    return first.amount > second.amount
                }

                "ml" -> {
                    when (second.type) {
                        "L" -> {
                            return (first.amount * 1000 > second.amount)
                        }

                        "fl oz" -> {
                            return (first.amount * 0.0338 > second.amount)
                        }
                    }
                    return first.amount > second.amount
                }

                "L" -> {
                    when (second.type) {
                        "ml" -> {
                            return (first.amount * 1000 > second.amount)
                        }

                        "fl oz" -> {
                            return (first.amount * 33.814 > second.amount)
                        }
                    }
                    return first.amount > second.amount
                }

                "oz" -> {
                    when (second.type) {
                        "g" -> {
                            return (first.amount * 28.3495 > second.amount)
                        }

                        "kg" -> {
                            return (first.amount * 0.02834 > second.amount)
                        }

                        "lbs" -> {
                            return (first.amount * 0.0625 > second.amount)
                        }
                    }
                    return first.amount > second.amount
                }

                "lbs" -> {
                    when (second.type) {
                        "g" -> {
                            return (first.amount * 453.592 > second.amount)
                        }

                        "kg" -> {
                            return (first.amount * 0.4535 > second.amount)
                        }

                        "oz" -> {
                            return (first.amount * 16 > second.amount)
                        }
                    }
                    return first.amount > second.amount
                }

                "fl oz" -> {
                    when (second.type) {
                        "ml" -> {
                            return (first.amount * 29.5735 > second.amount)
                        }

                        "L" -> {
                            return (first.amount * 0.0295735 > second.amount)
                        }
                    }
                    return first.amount > second.amount
                }
            }
            return false
        }

        override fun getLayout() = R.layout.recipe_row_child
    }
}