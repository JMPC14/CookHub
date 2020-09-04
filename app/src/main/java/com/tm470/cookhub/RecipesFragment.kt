package com.tm470.cookhub

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.tm470.cookhub.models.Ingredient
import com.xwray.groupie.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_new_recipe.*
import kotlinx.android.synthetic.main.fragment_recipes.*
import kotlinx.android.synthetic.main.recipe_row.view.*
import kotlinx.android.synthetic.main.recipe_row_child.view.*

@RequiresApi(Build.VERSION_CODES.O)
class RecipesFragment : Fragment() {

    val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_recipes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().toolbar.title = "Recipes"

        adapter.clear()

        fabRecipes.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, NewRecipeFragment(), "NewRecipeFragment")
                .addToBackStack("NewRecipeFragment").commit()
        }

        recyclerViewRecipes.layoutManager = LinearLayoutManager(context)
        recyclerViewRecipes.adapter = adapter

        displayRecipes()
    }

    private fun displayRecipes() {
        CurrentUser.recipes.forEach {
            adapter.apply {
                this.add(ExpandableGroup(ExpandableHeaderItem(it.name!!)).apply {
                    it.ingredients!!.forEach {
                        add(ChildItem(it))
                    }
                })
            }
        }
    }

    class ExpandableHeaderItem(val title: String): com.xwray.groupie.kotlinandroidextensions.Item(), ExpandableItem {

        private lateinit var expandableGroup: ExpandableGroup

        override fun bind(
            viewHolder: com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder,
            position: Int
        ) {
            viewHolder.root.recipeTitle.text = title
            viewHolder.itemView.indicator.setOnClickListener {
                expandableGroup.onToggleExpanded()
                changeStuff(viewHolder)
            }

            viewHolder.itemView.setOnClickListener {

            }
        }

        override fun getLayout() = R.layout.recipe_row

        private fun changeStuff(viewHolder: GroupieViewHolder) {
            viewHolder.root.indicator.apply {
                setImageResource(
                    if (expandableGroup.isExpanded) R.drawable.ic_baseline_arrow_drop_down_24
                    else R.drawable.ic_baseline_arrow_right_24)
            }
        }

        override fun setExpandableGroup(onToggleListener: ExpandableGroup) {
            this.expandableGroup = onToggleListener
        }
    }

    open class ChildItem(private val ingredient: Ingredient) : Item<GroupieViewHolder>() {

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.root.ingredientTitle.text = ingredient.name
            viewHolder.root.ingredientQuantity.text = ingredient.quantity!!.amount.toString()
            viewHolder.root.ingredientQuantityType.text = ingredient.quantity!!.type
        }

        override fun getLayout() = R.layout.recipe_row_child
    }
}