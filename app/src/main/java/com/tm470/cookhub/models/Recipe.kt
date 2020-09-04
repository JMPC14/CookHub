package com.tm470.cookhub.models

class Recipe(var name: String?, var ingredients: List<Ingredient>?, var instructions: String?, var id: String?) {

    constructor(): this("", mutableListOf<Ingredient>(), "", "")
}