package ru.bezsveta.ibuzzpromo.retrofit

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import ru.bezsveta.ibuzzpromo.model.BatteryStatus

object BaseRetrofit {
    private const val BASE_URL = "https://bez-sveta.ru/"
    //private const val BASE_URL = "https://bez-sveta.ru/api/"
    private val gson = GsonBuilder().setLenient().create()
    private val retrofit: Retrofit by lazy {  Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory( GsonConverterFactory.create(gson)).build()}

    fun sendBatteryData(batteryStatus: BatteryStatus) = retrofit.create(StatusLightAPI::class.java).sendData(batteryStatus.isCharging,batteryStatus.code)
}