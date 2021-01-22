package ru.bezsveta.ibuzzpromo

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import ru.bezsveta.ibuzzpromo.databinding.FragmentWithWebViewBinding


class WebViewFragment() : Fragment() {
    private lateinit var binding:FragmentWithWebViewBinding

    companion object {
        private const val CODE_LINK = "code_link"
        fun newInstance(link: String): WebViewFragment {
            val args = Bundle()
            args.putString(CODE_LINK, link)
            val fragment = WebViewFragment()
            fragment.arguments = args
            return fragment
        }
    }



    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_with_web_view,
                container,
                false
        )
        binding.webView.loadUrl(arguments?.getString(CODE_LINK).toString())
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.webViewClient = WebViewClient()
        return binding.root
    }


    fun getWebView():WebView{
        return binding.webView
    }

}