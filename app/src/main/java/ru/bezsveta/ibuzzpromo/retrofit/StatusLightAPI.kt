package ru.bezsveta.ibuzzpromo.retrofit

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import ru.bezsveta.ibuzzpromo.model.BatteryStatus

interface StatusLightAPI {

    //@Headers("Content-Type: application/json")
    @POST("sendData/")
    @FormUrlEncoded
    fun sendData(@Field("charge_status") charge_status:Int,@Field("device_serial") code:String? ): Call<Void>
    //fun sendData(@Body batteryStatus:BatteryStatus): Call<Void>
}