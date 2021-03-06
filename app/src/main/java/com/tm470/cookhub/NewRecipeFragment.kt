package com.tm470.cookhub

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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
            val ingredientList = mutableListOf<Ingredient>()
            for (i in 0 until adapter.itemCount) {
                val item = recyclerViewNewRecipeIngredients.findViewHolderForAdapterPosition(i)!!.itemView
                val ingredientName = item.editTextNewIngredientName.text.toString()
                val ingredientQuantity = item.editTextNewIngredientQuantity.text.toString().toDouble()
                val quantityType = item.spinnerIngredientQuantity.selectedItem.toString()

                ingredientList.add(Ingredient(ingredientName, Quantity(ingredientQuantity, quantityType)))
            }


            val ref = FirebaseDatabase.getInstance().getReference("/users/${CurrentUser.user!!.uid}/recipes").push()
            val recipe = Recipe(title, ingredientList, instructions, ref.key, switchRecipePublic.isChecked, CurrentUser.user!!.username!!)
            ref.setValue(recipe).addOnSuccessListener {
                requireActivity().supportFragmentManager.beginTransaction().remove(this)
                requireActivity().supportFragmentManager.popBackStack()

                hideKeyboardFrom(requireContext(), this.requireView())
            }
        }

        recyclerViewNewRecipeIngredients.layoutManager = LinearLayoutManager(context)
        recyclerViewNewRecipeIngredients.adapter = adapter

        adapter.add(NewIngredientItem())
    }

    inner class NewIngredientItem: Item<GroupieViewHolder>() {

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            val array = listOf("g", "kg", "ml", "L", "oz", "lbs", "fl oz")
            val spinnerAdapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_dropdown_item, array)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            viewHolder.itemView.spinnerIngredientQuantity.adapter = spinnerAdapter

            viewHolder.itemView.imageViewDeleteIngredient.setOnClickListener {
                adapter.remove(this)
            }
        }

        override fun getLayout() = R.layout.new_recipe_ingredient_item
    }
}