package com.myme.qrapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.myme.qrapp.databinding.ActivityMainBinding
import com.myme.qrapp.databinding.ActivitySelectBinding

class SelectActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select)
        supportActionBar?.hide()
        val intent = Intent(this@SelectActivity, MainActivity::class.java)

        binding.btnEnterInbound.setOnClickListener {
            intent.putExtra("isInbound",true)
            startActivity(intent)
            finish()
        }
        binding.btnEnterOutbound.setOnClickListener {
            intent.putExtra("isInbound",false)
            startActivity(intent)
            finish()
        }

    }
}