package com.myme.qrapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okio.IOException
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: TextView
    private val client = OkHttpClient.Builder()
        .cookieJar(MyCookieJar.INSTANCE) // 쿠키 관리 활성화
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        // Find views
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)

        // Set login button click listener
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            loginUser(username, password)
//            // Validate inputs
//            if (username.isEmpty() || password.isEmpty()) {
//                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
//            } else {
//                loginUser(username, password)
//            }
        }
    }

    private fun loginUser(username: String, password: String) {
        val url = "https://api.mywareho.me/v1/auth/login"

        // JSON 객체 생성
        val jsonObject = JSONObject().apply {
            put("id", "jsj7878")
            put("password", "NSySYJ")
        }

        // JSON RequestBody 생성
        val body = RequestBody.create("application/json".toMediaType(), jsonObject.toString())

        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
//                        val cookies = response.headers("Set-Cookie") // Set-Cookie 헤더 값 가져오기
//
////                        var sessionId: String? = null
////
////                        for (cookie in cookies) {
////                            if (cookie.startsWith("JSESSIONID") || cookie.startsWith("SESSIONID")) {
////                                sessionId = cookie.split(";")[0] // "JSESSIONID=abcd1234; Path=/; HttpOnly" → "JSESSIONID=abcd1234"
////                                break
////                            }
////                        }
//                        response.body?.string()?.let { responseBody ->
//                            val jsonObject = JSONObject(responseBody)
//                            val userId = jsonObject.getInt("userId")
//                            val name = jsonObject.getString("name")
//                            val phoneNumber = jsonObject.getString("phoneNumber")
//                            val id = jsonObject.getString("id")
//                            val role = jsonObject.getString("role")
//                            Log.d("chk", "${name} ")
//                        }
                        Toast.makeText(applicationContext, "Login successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, SelectActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(applicationContext, "Login failed: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}

