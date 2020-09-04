package com.tm470.cookhub

import com.tm470.cookhub.models.CookhubUser

object CurrentUser {
    var user: CookhubUser? = null
    var friends: MutableList<String>? = null
    var currentChatUser: CookhubUser? = null
    var cid: String? = null
}