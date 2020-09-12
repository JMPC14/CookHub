package com.tm470.cookhub.models

class CookHubUser(var uid: String?, var username: String?, var email: String?, var profileImageUrl: String?) {

    constructor() : this("", "", "", "")
}