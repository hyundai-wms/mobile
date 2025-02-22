package com.myme.qrapp

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    val qrCodeLiveData = MutableLiveData<String>("")

    val isInbound = MutableLiveData<Boolean>()

    val planId = MutableLiveData<String>("")

    fun setQrCodeValue(qrCodeValue: String) {
        qrCodeLiveData.value = qrCodeValue
        Log.d("chk","${qrCodeLiveData.value} ${qrCodeValue}")
    }

    fun setIsInbound(checkInbound : Boolean){
        isInbound.value = checkInbound
    }

    fun setPlanId(id : String){
        planId.value = id
    }
}
