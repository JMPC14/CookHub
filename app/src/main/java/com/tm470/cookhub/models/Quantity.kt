package com.tm470.cookhub.models

class Quantity(var amount: Int, var type: String) {

    constructor(): this(0, "g")
}