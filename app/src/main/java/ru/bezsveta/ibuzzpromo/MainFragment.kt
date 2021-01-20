package ru.bezsveta.ibuzzpromo

import android.annotation.SuppressLint
import android.content.*
import android.os.BatteryManager
import android.os.Bundle
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.style.URLSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import ru.bezsveta.ibuzzpromo.databinding.FragmentMainBinding


class MainFragment: Fragment(){
    private lateinit var binding: FragmentMainBinding
    private val links = arrayOf(
            "https://bez-sveta.ru/register", "https://bez-sveta.ru/voprosy-i-otvety.html",
            "https://bez-sveta.ru/prilozhenie.html", "https://bez-sveta.ru/kontakty.html"
    )

    private lateinit var callback: Callback

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as Callback
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
        createConstantCode()
        setUpLink(binding.linkText, links[0])
        setUpLink(binding.faqText, links[1])
        setUpLink(binding.aboutText, links[2])
        setUpLink(binding.contactsText, links[3])
        setLightStatus()
        binding.copyText.setOnClickListener{ copyCode() }
        return binding.root
    }


    @SuppressLint("HardwareIds")
    private fun createConstantCode(){
        binding.code=Settings.Secure.getString(
                activity?.contentResolver,
                Settings.Secure.ANDROID_ID
        )
    }

    private fun setLightStatus(){
            val batteryStatus = context!!.registerReceiver(
                    null,
                    IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
        val string = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        binding.setLightStatus(string == BatteryManager.BATTERY_STATUS_CHARGING)
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


    interface Callback{
        fun changeFragmentFromMainToWebView(link: String)
    }
}