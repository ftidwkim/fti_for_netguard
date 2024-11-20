package com.franklinwireless.android.jexkids.home.model

import com.google.gson.annotations.SerializedName

data class AppConfiguration(
    @SerializedName("unique_id") val unique_code: String?,
    @SerializedName("command") val command: String?,
    @SerializedName("parental_control_id") val parentalid: String?,
    @SerializedName("deviceid") val deviceid: String?,
)
