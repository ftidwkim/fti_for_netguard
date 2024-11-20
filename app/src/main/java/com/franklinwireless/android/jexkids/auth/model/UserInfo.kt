package com.franklinwireless.android.jexkids.auth.model

import com.google.gson.annotations.SerializedName

data class UserInfo (
    @SerializedName("device_id") val deviceId: String?,
    @SerializedName("unique_code") val unique_code: String?,
    @SerializedName("user_name") val user_name: String?
)