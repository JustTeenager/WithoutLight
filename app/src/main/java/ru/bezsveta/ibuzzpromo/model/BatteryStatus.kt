package ru.bezsveta.ibuzzpromo.model

import com.google.gson.annotations.SerializedName

data class BatteryStatus(@SerializedName("charge_status") val isCharging: Int,
                         @SerializedName("device_serial") val code:String?)
