package com.tm470.cookhub.models

class User(var uid: String?, var username: String?, var email: String?, var profileImage: String?) {

    constructor(): this(null, null, null, null)

    constructor(uid: String?, username: String?, email: String?) : this(uid, username, email, null)
}