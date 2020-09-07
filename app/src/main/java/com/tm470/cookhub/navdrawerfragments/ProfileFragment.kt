package com.tm470.cookhub.navdrawerfragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import com.tm470.cookhub.CurrentUser
import com.tm470.cookhub.R
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().toolbar.title = "Profile"

        loadProfile()
    }

    private fun loadProfile() {
        textViewUsername.text = CurrentUser.user!!.username
        textViewEmailAddress.text = CurrentUser.user!!.email
        textViewRecipes.text = CurrentUser.recipes!!.size.toString()
        textViewIngredients.text = CurrentUser.ingredients!!.size.toString()

        Picasso.get().load(CurrentUser.user!!.profileImageUrl).into(imageViewProfile)
    }
}