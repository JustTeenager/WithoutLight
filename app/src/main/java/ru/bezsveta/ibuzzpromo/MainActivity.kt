package ru.bezsveta.ibuzzpromo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("tut", Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID))
    }
}