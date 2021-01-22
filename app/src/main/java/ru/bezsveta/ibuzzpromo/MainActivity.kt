package ru.bezsveta.ibuzzpromo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), MainFragment.Callback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val fragment = MainFragment()
        supportFragmentManager.beginTransaction().add(R.id.container_fragment, fragment).commit()
    }

    override fun changeFragmentFromMainToWebView(link: String) {
        supportFragmentManager.beginTransaction().add(R.id.container_fragment,WebViewFragment.newInstance(link))
                .addToBackStack(null).commit()
    }
}