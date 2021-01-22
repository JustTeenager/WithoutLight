package ru.bezsveta.ibuzzpromo

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.Service
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import ru.bezsveta.ibuzzpromo.databinding.DialogInternetConnectionBinding

class InternetConnectionDialog: DialogFragment() {


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding:DialogInternetConnectionBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_internet_connection, null, false)
        binding.checkNetwork.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.setBackgroundColor(resources.getColor(R.color.green_dark))
                MotionEvent.ACTION_UP ->{
                    v.setBackgroundColor(resources.getColor(R.color.green_light))
                    if (isNetworkConnect()) {
                        this.dialog?.dismiss()
                        sendResult()
                    }
                    else Toast.makeText(context, R.string.no_network, Toast.LENGTH_SHORT).show()
                }
            }
            return@setOnTouchListener true
        }
        val dialog:AlertDialog = AlertDialog.Builder(context).setView(binding.root).create()
        return dialog
    }

    private fun sendResult(){
        targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, Intent())
    }

    private fun isNetworkConnect(): Boolean {
        val cm = context?.getSystemService(Service.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netinfo = cm.activeNetworkInfo
        return netinfo != null && netinfo.isConnected
    }
}