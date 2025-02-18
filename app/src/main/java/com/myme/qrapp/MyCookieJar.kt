package com.myme.qrapp

import android.util.Log
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class MyCookieJar : CookieJar {

    private val cookieStore = HashMap<String, List<Cookie>>()

    // 싱글턴 객체
    companion object {
        val INSTANCE = MyCookieJar()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        Log.d("cookie1","${cookies}")
        val domain = url.host
        cookieStore[domain] = cookies
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val domain = url.host
        Log.d("cookie2","${cookieStore[domain]} ${url}")
        return cookieStore[domain] ?: emptyList()
    }
}
