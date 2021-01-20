package ru.bezsveta.ibuzzpromo.retrofit

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface StatusLightAPI {

    @POST("/sendData")
    fun sendData(@Body batteryStatus: BatteryStatus): Call<String>
}