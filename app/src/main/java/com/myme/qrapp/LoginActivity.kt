package com.myme.qrapp

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
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
        StrictMode.enableDefaults()
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
                        val responseBody = response.body?.string() // 🔹 한 번만 호출

                        responseBody?.let { Log.d("chk", it) }  // ✅ 응답 로그 출력

                        val intent = Intent(this@LoginActivity, SelectActivity::class.java)

                        // 🔹 JSON 파싱 후 데이터를 `Intent`로 전달
                        responseBody?.let {
                            try {
                                val jsonObject = JSONObject(it)
                                val name = jsonObject.getString("name")  // 사용자 이름
                                val userId = jsonObject.getInt("userId")  // 사용자 ID
                                val phoneNumber = jsonObject.getString("phoneNumber")  // 전화번호
                                val id = jsonObject.getString("id")  // 로그인 ID
                                val role = jsonObject.getString("role")  // 사용자 역할
                                intent.putExtra("userName",name)
                                Log.d("chk", "userName: $name, userId: $userId, phoneNumber: $phoneNumber, id: $id, role: $role")
                            } catch (e: Exception) {
                                Log.e("chk", "JSON 파싱 오류: ${e.message}")
                            }
                        }

                        Toast.makeText(applicationContext, "로그인 되었습니다!", Toast.LENGTH_SHORT).show()
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(applicationContext, "Login failed: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}

