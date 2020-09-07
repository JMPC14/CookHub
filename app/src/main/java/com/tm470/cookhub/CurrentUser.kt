package com.tm470.cookhub

import com.tm470.cookhub.models.CookHubUser
import com.tm470.cookhub.models.Ingredient
import com.tm470.cookhub.models.Recipe

object CurrentUser {
    var user: CookHubUser? = null
    var friends: MutableList<String>? = null
    var currentChatUser: CookHubUser? = null
    var cid: String? = null
    var recipes: MutableList<Recipe>? = mutableListOf()
    var ingredients: MutableList<Ingredient>? = mutableListOf()
    var attachedImage: String? = null
    var attachedRecipe: String? = null
    var editRecipe: Recipe? = null
    var viewRecipe: Recipe? = null
}