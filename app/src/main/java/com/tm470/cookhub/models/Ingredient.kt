package com.tm470.cookhub.models

class Ingredient(var name: String?, var quantity: Quantity?) {

    constructor(): this("", Quantity())
}