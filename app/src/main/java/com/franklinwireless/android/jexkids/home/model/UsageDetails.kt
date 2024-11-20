package com.franklinwireless.android.jexkids.home.model

import com.google.gson.annotations.SerializedName

data class UsageDetails(
    @SerializedName("command") val command: String?,
    @SerializedName("unique_code") val unique_code: String?,
    @SerializedName("usage_date") val usagedate: String?

)
