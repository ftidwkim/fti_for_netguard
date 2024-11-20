package com.franklinwireless.android.jexkids.restservice

import com.franklinwireless.android.jexkids.auth.model.UserInfo
import com.franklinwireless.android.jexkids.home.model.AppConfiguration
import com.franklinwireless.android.jexkids.home.model.ChildDetails
import com.franklinwireless.android.jexkids.home.model.UsageDetails
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface RestAPI {
    @Headers("Content-Type: application/json")
    @POST("sign_up_code_validation")
    fun addUser(@Body userData: UserInfo): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("get_child_details")
    fun childDetails(@Header("authorizationToken") auth: String, @Body body: ChildDetails): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("app_usage_reports")
    fun usageDetails(@Header("authorizationToken") auth: String, @Body body: UsageDetails): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("kids_app_configuration_control")
    fun appConfiguration(@Header("authorizationToken") auth: String, @Body body: AppConfiguration): Call<ResponseBody>
}