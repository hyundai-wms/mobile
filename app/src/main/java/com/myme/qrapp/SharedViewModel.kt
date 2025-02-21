package com.myme.qrapp

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    val qrCodeLiveData = MutableLiveData<String>("")

    val isInbound = MutableLiveData<Boolean>()

    fun setQrCodeValue(qrCodeValue: String) {
        qrCodeLiveData.value = qrCodeValue
        Log.d("chk","${qrCodeLiveData.value} ${qrCodeValue}")
    }

    fun setIsInbound(checkInbound : Boolean){
        isInbound.value = checkInbound
    }
}
