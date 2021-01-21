package ru.bezsveta.ibuzzpromo.retrofit

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface StatusLightAPI {

    @POST("sendData/")
    @FormUrlEncoded
    fun sendData(@Field("charge_status") charge_status:Int,@Field("device_serial") code:String? ): Call<Void>
}