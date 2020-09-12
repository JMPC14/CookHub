package com.tm470.cookhub.navdrawerfragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.FirebaseDatabase
import com.tm470.cookhub.CurrentUser
import com.tm470.cookhub.R
import com.tm470.cookhub.hideKeyboardFrom
import com.tm470.cookhub.models.Ingredient
import com.tm470.cookhub.models.Quantity
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_ingredients.*
import kotlinx.android.synthetic.main.new_recipe_ingredient_item.view.*

class IngredientsFragment : Fragment() {

    val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ingredients, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().toolbar.title = "My Ingredients"

        adapter.clear()

        buttonAddIngredient.setOnClickListener {
            adapter.add(IngredientItem())
        }

        buttonSaveIngredients.setOnClickListener {
            val ref = FirebaseDatabase.getInstance().getReference("/users/${CurrentUser.user!!.uid}/ingredients")
            val ingredientList = mutableListOf<Ingredient>()
            for (i in 0 until adapter.itemCount) {
                val item = recyclerViewIngredients.findViewHolderForAdapterPosition(i)!!.itemView
                val ingredientName = item.editTextNewIngredientName.text.toString()
                val ingredientQuantity = item.editTextNewIngredientQuantity.text.toString().toDouble()
                val quantityType = item.spinnerIngredientQuantity.selectedItem.toString()

                ingredientList.add(Ingredient(ingredientName, Quantity(ingredientQuantity, quantityType)))
            }

            ref.setValue(ingredientList).addOnSuccessListener {
                CurrentUser.ingredients = ingredientList

                Toast.makeText(requireContext(), "Ingredients saved.", Toast.LENGTH_LONG).show()

                hideKeyboardFrom(requireContext(), requireView())
            }
        }

        recyclerViewIngredients.layoutManager = LinearLayoutManager(context)
        recyclerViewIngredients.adapter = adapter

        displayIngredients()
    }

    private fun displayIngredients() {
        CurrentUser.ingredients!!.forEach {
            adapter.add(IngredientItem(it))
        }
    }

    inner class IngredientItem(val ingredient: Ingredient): Item<GroupieViewHolder>() {

        constructor(): this(Ingredient())

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.editTextNewIngredientName.setText(ingredient.name)
            viewHolder.itemView.editTextNewIngredientQuantity.setText(ingredient.quantity!!.amount.toString())

            val array = listOf("g", "kg", "ml", "L", "oz", "lbs", "fl oz")
            val spinnerAdapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_dropdown_item, array)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            viewHolder.itemView.spinnerIngredientQuantity.adapter = spinnerAdapter
            var a = 0
            array.forEach {
                if (ingredient.quantity!!.type == it) {
                    a = array.indexOf(it)
                }
            }
            viewHolder.itemView.spinnerIngredientQuantity.setSelection(a)

            viewHolder.itemView.imageViewDeleteIngredient.setOnClickListener {
                adapter.remove(this)
                buttonSaveIngredients.performClick()
            }
        }

        override fun getLayout() = R.layout.new_recipe_ingredient_item
    }
}