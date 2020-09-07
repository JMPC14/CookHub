package com.tm470.cookhub.models

class Quantity(var amount: Double, var type: String) {

    constructor(): this(0.0, "g")
}