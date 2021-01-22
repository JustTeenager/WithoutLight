package ru.bezsveta.ibuzzpromo

import android.annotation.SuppressLint
import android.app.Activity.BIND_IMPORTANT
import android.app.Activity.RESULT_OK
import android.app.Service
import android.content.*
import android.content.Context.BIND_AUTO_CREATE
import android.net.ConnectivityManager
import android.os.BatteryManager
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.style.URLSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import ru.bezsveta.ibuzzpromo.databinding.FragmentMainBinding


class MainFragment:SendDataService.BatteryStatusProvider, Fragment(){
    private lateinit var binding: FragmentMainBinding


    companion object {
        private const val NO_INTERNET_REQUEST_CODE = 1
        private val links = arrayOf(
                "https://bez-sveta.ru/register", "https://bez-sveta.ru/voprosy-i-otvety.html",
                "https://bez-sveta.ru/prilozhenie.html", "https://bez-sveta.ru/kontakty.html"
        )
    }

    private lateinit var callback: Callback
    private lateinit var dataService: SendDataService
    private val dialog = InternetConnectionDialog()

    private val conn:ServiceConnection = object:ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.e("tut_conn","connected")
            dataService=(service as SendDataService.ServiceProvider).getService()
            dataService.launchTimer(this@MainFragment)
        }

        override fun onServiceDisconnected(name: ComponentName?) {}
    }



    lateinit var receiver: BroadcastReceiver

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as Callback
        context.bindService(Intent(context,SendDataService::class.java),conn, BIND_IMPORTANT)
        context.startService(Intent(context,SendDataService::class.java))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
        createConstantCode()
        setLightStatus()
        setUpLink(binding.linkText, links[0])
        setUpLink(binding.faqText, links[1])
        setUpLink(binding.aboutText, links[2])
        setUpLink(binding.contactsText, links[3])
        binding.copyText.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.setBackgroundColor(resources.getColor(R.color.green_dark))
                MotionEvent.ACTION_UP ->{
                    v.setBackgroundColor(resources.getColor(R.color.green_light))
                    copyCode()
                }
            }
            return@setOnTouchListener true
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (isNetworkConnect()){
            dismissDialog()
        }
        else {
            showDialog()
        }
    }


    override fun onPause() {
        super.onPause()
        dismissDialog()
    }

    private fun isNetworkConnect(): Boolean {
        val cm = context?.getSystemService(Service.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netinfo = cm.activeNetworkInfo
        return netinfo != null && netinfo.isConnected
    }

    @SuppressLint("HardwareIds")
    private fun createConstantCode(){
        binding.code=Settings.Secure.getString(
                activity?.contentResolver,
                Settings.Secure.ANDROID_ID
        )
    }

    private fun setLightStatus(){
        receiver=object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                binding.setLightStatus(status == BatteryManager.BATTERY_STATUS_CHARGING || status== BatteryManager.BATTERY_STATUS_FULL)
            }
        }
        context!!.registerReceiver(receiver,IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    private fun setUpLink(textView: TextView, link: String){
        val ss = SpannableString(textView.text)
        ss.setSpan(
                URLSpan(java.lang.String.valueOf(textView.text)), 0, textView.text.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = ss
        textView.setOnClickListener{
                callback.changeFragmentFromMainToWebView(link)
            }
    }

    private fun copyCode(){
        val clipboard: ClipboardManager? = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText("", binding.codeText.text.toString())
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, R.string.text_copy, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        context?.unregisterReceiver(receiver)
        context?.unbindService(conn)
    }

    override fun getCode(): String? =binding.code

    override fun showDialog() {
        activity?.runOnUiThread {
            if (!dialog.isAdded) {
                try {
                    dialog.isCancelable = false
                    dialog.setTargetFragment(this, NO_INTERNET_REQUEST_CODE)
                    fragmentManager?.let { dialog.show(it, null) }
                }catch (e:Exception) {e.printStackTrace()}
            }
        }
    }

    override fun dismissDialog() {
        activity?.runOnUiThread {
            try {
                dialog.dismiss()
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode==RESULT_OK){
            if(requestCode == NO_INTERNET_REQUEST_CODE){
                //activity?.let { dataService.changeNotification(it.getString(R.string.app_name)) }
            }
        }
    }

    interface Callback{
        fun changeFragmentFromMainToWebView(link: String)
    }
}