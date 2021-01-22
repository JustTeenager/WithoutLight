package ru.bezsveta.ibuzzpromo

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import ru.bezsveta.ibuzzpromo.model.BatteryStatus
import ru.bezsveta.ibuzzpromo.retrofit.InternetRequestsManager
import java.util.*


class SendDataService : Service() {

    companion object {
        const val channelId = "channel_battery"
        const val notifyId = 228 // some number
        const val CODE_TO_SEND_BATTERY_DATA=0
    }

    lateinit var receiver:BroadcastReceiver
    val requestsManager = InternetRequestsManager()
    var code:String?="no_code"
    var lightStatus:Int?=-1
    var provider:BatteryStatusProvider?=null

    lateinit var thread:SendThread

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) requestsManager.initClient()
        else requestsManager.initRetrofit()
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
        return START_STICKY
    }

    fun launchTimer(provider: BatteryStatusProvider) {
        this.provider=provider
        code = provider.getCode()
        setLightStatusReceiver()
    }

    private fun deleteProvider(){
        this.provider=null
    }

    private fun sendStartNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createChannel()
        val notification = buildNotification(this.resources.getString(R.string.connected))
        this.startForeground(notifyId, notification)
    }

    private fun buildNotification(title: String): Notification {
        val notificationIntent = Intent(baseContext, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(baseContext,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT)


        val builder = NotificationCompat.Builder(baseContext, channelId)

        builder.setSmallIcon(R.mipmap.ic_launcher_circle)
                .setContentTitle(title)
                .setShowWhen(true)
                .setOngoing(true)
                .setContentIntent(contentIntent)
                .setProgress(100, 0, true)
                .priority = NotificationCompat.PRIORITY_MAX

        return  builder.build()

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
                lightStatus = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            }
        }
        registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    private fun isNetworkConnect(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val netinfo = cm.activeNetworkInfo
        return netinfo != null && netinfo.isConnected
    }

    override fun onUnbind(intent: Intent?): Boolean {
        deleteProvider()
        return super.onUnbind(intent)
    }

    fun changeNotification(title: String){
        val notification = buildNotification(title)
        this.startForeground(notifyId, notification)
    }

    inner class SendThread(name: String?) : HandlerThread(name) {
        private lateinit var handler:Handler

        override fun onLooperPrepared() {
            super.onLooperPrepared()
            this.priority = Thread.MAX_PRIORITY
            handler= Handler(looper){
                when(it.what){
                    CODE_TO_SEND_BATTERY_DATA -> sendBatteryData()
                }
                return@Handler false
            }
            startTimer()
        }

        private fun startTimer(){

            handler.sendEmptyMessage(CODE_TO_SEND_BATTERY_DATA)
        }

        private fun sendBatteryData(){
            Thread.sleep(1000)
            var isDisconnected=false
                    if (isNetworkConnect()) {
                        Log.d("tut_sendData", isDisconnected.toString())
                        if (isDisconnected) this@SendDataService.startForeground(notifyId, buildNotification(getString(R.string.connected)))
                        isDisconnected = false
                        provider?.dismissDialog()

                        val batteryStatus=BatteryStatus(
                                if (lightStatus == BatteryManager.BATTERY_STATUS_CHARGING || lightStatus == BatteryManager.BATTERY_STATUS_FULL) 1 else 0,
                                code
                        )

                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP)
                        {
                            requestsManager.sendBatteryDataViaHTTPClient(batteryStatus)
                        }


                        //////////////////////////////////////////////////////////////////////////////////////

                        else{
                            try {
                                requestsManager.sendBatteryDataViaRetrofit(batteryStatus).execute()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    else{
                        Log.d("tut_no_internet", isDisconnected.toString())
                        provider?.showDialog()
                        if (!isDisconnected) {
                            changeNotification(getString(R.string.setting_network))
                            isDisconnected = true
                        }
            }
            Thread.sleep(60000)
            startTimer()
        }
    }

    interface BatteryStatusProvider{
        fun getCode():String?
        fun showDialog()
        fun dismissDialog()
    }

    class ServiceProvider(private val service: SendDataService) : Binder(){
        fun getService():SendDataService{
            return service
        }
    }
}