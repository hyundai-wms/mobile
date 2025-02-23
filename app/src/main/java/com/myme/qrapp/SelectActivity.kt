package com.myme.qrapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.myme.qrapp.databinding.ActivitySelectBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SelectActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectBinding
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val userName = intent.getStringExtra("userName")
        val userRole = intent.getStringExtra("userRole")
        val intent = Intent(this@SelectActivity, MainActivity::class.java)
        Log.d("chk","$userName")
        binding.welcomeText.text ="환영합니다 ${userName} 님"
        binding.btnEnterInbound.setOnClickListener {
            intent.putExtra("isInbound",true)
            intent.putExtra("userName",userName)
            intent.putExtra("userRole",userRole)
            startActivity(intent)
            finish()
        }
        binding.btnEnterOutbound.setOnClickListener {
            intent.putExtra("isInbound",false)
            intent.putExtra("userName",userName)
            intent.putExtra("userRole",userRole)
            startActivity(intent)
            finish()
        }
    }
}