package com.tm470.cookhub.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class CookHubUser(var uid: String?, var username: String?, var email: String?, var profileImageUrl: String?) : Parcelable {

    constructor() : this("", "", "", "")
}