package com.tm470.cookhub

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.forEach
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.FirebaseDatabase
import com.tm470.cookhub.models.Ingredient
import com.tm470.cookhub.models.Quantity
import com.tm470.cookhub.models.Recipe
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_new_recipe.*
import kotlinx.android.synthetic.main.new_recipe_ingredient_item.view.*


class NewRecipeFragment : Fragment() {

    private val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_recipe, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().toolbar.title = "New Recipe"

        buttonNewIngredient.setOnClickListener {
            adapter.add(NewIngredientItem())
        }

        buttonSaveRecipe.setOnClickListener {
            val title = editTextRecipeTitle.text.toString()
            val instructions = editTextRecipeInstructions.text.toString()
            for (i in 0 until adapter.itemCount) {
                val ingredient = adapter.getGroupAtAdapterPosition(i) as NewIngredientItem
                val a = ingredient.returnIngredient()
            }
        }

        recyclerViewNewRecipeIngredients.layoutManager = LinearLayoutManager(context)
        recyclerViewNewRecipeIngredients.adapter = adapter

        adapter.add(NewIngredientItem())
    }

    inner class NewIngredientItem: Item<GroupieViewHolder>() {

        var ingredientName = ""
        var ingredientQuantity = ""
        var quantityType = ""

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            ingredientName = viewHolder.itemView.editTextNewIngredientName.text.toString()
            ingredientQuantity = viewHolder.itemView.editTextNewIngredientQuantity.text.toString()
//            quantityType = viewHolder.itemView.spinnerIngredientQuantity.selectedItem.toString()

            viewHolder.itemView.setOnLongClickListener {
                val pop = PopupMenu(it.context, it)
                pop.inflate(R.menu.new_ingredient_menu)
                pop.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.remove_ingredient -> {
                            adapter.remove(this)
                        }
                    }
                    true
                }
                pop.show()
                true
            }
        }

        fun returnIngredient(): Ingredient {
            return Ingredient(ingredientName, Quantity(ingredientQuantity, quantityType))
        }

        override fun getLayout() = R.layout.new_recipe_ingredient_item
    }
}