package com.myme.qrapp

import android.app.Application
import android.os.Build
import android.os.StrictMode
import com.google.android.datatransport.BuildConfig

class Mywarehome : Application(){
    override fun onCreate() {
        super.onCreate()

        // 디버그 모드에서만 StrictMode 설정
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .permitNetwork() // 네트워크 사용 허용
                    .build()
            )
        }
    }
}