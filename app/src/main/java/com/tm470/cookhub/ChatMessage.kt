package com.tm470.cookhub

import com.tm470.cookhub.models.Recipe

class ChatMessage(
    val id: String,
    val text: String,
    val fromId: String,
    val toId: String,
    val timestamp: String,
    val time: Long
) {
    constructor(): this("", "", "", "", "", -1)

    var imageUrl: String? = null
    var recipe: Recipe? = null


    /** Constructor for image messages **/
    constructor(id: String, text: String, fromId: String, toId: String, timestamp: String, time: Long, imageUrl: String) : this(id, text, fromId, toId, timestamp, time) {
        this.imageUrl = imageUrl
    }


    /** Constructor for recipe messages **/
    constructor(id: String, text: String, fromId: String, toId: String, timestamp: String, time: Long, recipe: Recipe) : this(id, text, fromId, toId, timestamp, time) {
        this.recipe = recipe
    }
}