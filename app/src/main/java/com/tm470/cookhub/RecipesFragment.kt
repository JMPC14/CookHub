package com.tm470.cookhub

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_new_recipe.*
import kotlinx.android.synthetic.main.fragment_recipes.*
import kotlinx.android.synthetic.main.recipe_row.view.*
import kotlinx.android.synthetic.main.recipe_row_child.view.*

class RecipesFragment : Fragment() {

    val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_recipes, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().toolbar.title = "Recipes"

        fabRecipes.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, NewRecipeFragment(), "NewRecipeFragment")
                .addToBackStack("NewRecipeFragment").commit()
        }

        val childList = listOf(
            ChildItem("First Album"),
            ChildItem("Second Album"),
            ChildItem("Third Album"),
            ChildItem("Fourth Album")

        )

        val parentList = listOf(
            ExpandableHeaderItem("Travis Scott"),
            ExpandableHeaderItem("Migos"),
            ExpandableHeaderItem("Post Malone"),
            ExpandableHeaderItem("Drake")

        )

        adapter.apply {
            for (i in parentList){
                this.add(ExpandableGroup(i).apply {
                    for (j  in childList) {
                        add(j)
                    }
                })

            }
        }

        recyclerViewRecipes.layoutManager = LinearLayoutManager(context)
        recyclerViewRecipes.adapter = adapter
    }

    class ExpandableHeaderItem(val title: String): com.xwray.groupie.kotlinandroidextensions.Item(), ExpandableItem {

        private lateinit var expandableGroup: ExpandableGroup

        override fun bind(
            viewHolder: com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder,
            position: Int
        ) {

            viewHolder.root.recipeTitle.text = title
            viewHolder.itemView.setOnClickListener {
                expandableGroup.onToggleExpanded()
                changeStuff(viewHolder)
            }

        }

        override fun getLayout(): Int {
            return  R.layout.recipe_row
        }

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

    open class ChildItem(private val title: String) : Item<GroupieViewHolder>() {

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.root.ingredientTitle.text = title

        }
        override fun getLayout(): Int {
            return R.layout.recipe_row_child
        }

    }
}