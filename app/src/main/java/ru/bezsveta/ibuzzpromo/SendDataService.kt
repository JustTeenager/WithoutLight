package ru.bezsveta.ibuzzpromo

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.bezsveta.ibuzzpromo.retrofit.BaseRetrofit
import ru.bezsveta.ibuzzpromo.retrofit.BatteryStatus
import java.util.*

class SendDataService : Service() {

    companion object {
        const val channelId = "channel_battery"
        const val notifyId = 228 // some number
        const val CODE_TO_SEND_BATTERY_DATA=0
        lateinit var receiver:BroadcastReceiver
    }

    val timer = Timer()
    var code:String?="no_code"
    var lightStatus:Int?=-1
    var provider:BatteryStatusProvider?=null
    lateinit var thread:SendThread

    override fun onCreate() {
        super.onCreate()
        Log.d("provider_null", (provider==null).toString())
        sendStartNotification()
        thread=SendThread("sendThread")
    }

    override fun onBind(intent: Intent?): IBinder?  {
        code=provider?.getCode()
        return ServiceProvider(this)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!thread.isAlive) {
            thread.start()
        }
        return START_REDELIVER_INTENT
    }

    fun launchTimer(provider: BatteryStatusProvider) {
        this.provider=provider
        code = provider.getCode()
        setLightStatusReceiver()
    }

    private fun sendStartNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createChannel()
        val notification = buildNotification()
        this.startForeground(notifyId, notification)
    }

    private fun buildNotification(): Notification {
        val builder = NotificationCompat.Builder(baseContext, channelId)

        builder.setSmallIcon(R.mipmap.ic_launcher_circle)
                .setContentTitle(this.resources.getString(R.string.app_name))
                .setShowWhen(true)
                .setOngoing(true)
                .setProgress(100, 0, true)
                .priority = NotificationCompat.PRIORITY_MAX

        return builder.build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val name = "Service"
        val channel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH)
        channel.description = "notification_channel_description" //for user
        manager.createNotificationChannel(channel)
    }

    private fun setLightStatusReceiver(){
        receiver=object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("serviceStatus",lightStatus.toString())
                lightStatus = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            }
        }
        registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    inner class SendThread(name: String?) : HandlerThread(name) {
        private lateinit var handler:Handler

        override fun onLooperPrepared() {
            super.onLooperPrepared()
            handler= Handler(looper){
                when(it.what){
                    CODE_TO_SEND_BATTERY_DATA->sendBatteryData()
                }
                return@Handler false
            }
            startTimer()
        }

        private fun startTimer(){
            handler.sendEmptyMessage(CODE_TO_SEND_BATTERY_DATA)
        }

        private fun sendBatteryData(){
            val doAsynchronousTask = object : TimerTask() {
                override fun run() {
                    //TODO Поставить присутствие эл-ва при 100% зарядки
                    Log.d("tut_lightstatus",lightStatus.toString())
                    Log.d("tut_chatging_status",(BatteryManager.BATTERY_STATUS_CHARGING==lightStatus).toString())
                    Log.d("tut_code",code.toString())
                    BaseRetrofit.sendBatteryData(BatteryStatus(if (lightStatus==BatteryManager.BATTERY_STATUS_CHARGING) 1 else 0,code)).enqueue(object:Callback<Void>{
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) Log.d("tut_response_suc",response.message())
                            Log.d("tut_response","isHere")
                            Log.d("tut_method",call.request().method())
                            Log.d("tut_body",call.request().body().toString())
                            Log.d("tut_url",call.request().url().toString())
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            t.printStackTrace()
                        }
                    })
                }
            }
            timer.schedule(doAsynchronousTask, 2000, 10000)
        }
    }

    interface BatteryStatusProvider{
        fun getCode():String?
    }

    class ServiceProvider(private val service:SendDataService) : Binder(){
        fun getService():SendDataService{
            return service
        }
    }
}