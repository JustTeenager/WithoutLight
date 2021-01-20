package ru.bezsveta.ibuzzpromo

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.util.*

class SendDataService : Service() {
    companion object {
        const val channelId = "--your channel id--"
        const val notifyId = 395 // some number
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("Your service started")
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        sendStartNotification()
        doWhatYouNeed()// your code
    }

    private fun doWhatYouNeed() {
        val doAsynchronousTask: TimerTask
        val handler = Handler(baseContext.mainLooper)
        val timer = Timer()

        doAsynchronousTask = object : TimerTask() {
            override fun run() {
                handler.post(Runnable {
                    if (isOnline) { // check net connection
                        //what u want to do....
                    }
                })
            }
        }

        timer.schedule(doAsynchronousTask, 0, 10000) // execute in every 10 s

    }

    private fun sendStartNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createChannel()
        val notification = buildNotification()
        this.startForeground(notifyId, notification)
    }

    private fun buildNotification(): Notification {
        val builder = NotificationCompat.Builder(baseContext, channelId)

        builder.setSmallIcon(R.mipmap.ic_launcher)
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
}