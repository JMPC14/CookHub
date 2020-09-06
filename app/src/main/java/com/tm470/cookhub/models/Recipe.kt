package com.tm470.cookhub.models

class Recipe(var name: String?, var ingredients: List<Ingredient>?, var instructions: String?, var id: String?, var public: Boolean?, var author: String) {

    constructor(): this("", mutableListOf<Ingredient>(), "", "", false, "")
}