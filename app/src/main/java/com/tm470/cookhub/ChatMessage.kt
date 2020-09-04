package com.tm470.cookhub

class ChatMessage(
    val id: String,
    val text: String,
    val fromId: String,
    val toId: String,
    val timestamp: String,
    val time: Long
) {
    constructor(): this("", "", "", "", "", -1)
}