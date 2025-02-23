package com.myme.qrapp

import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.myme.qrapp.databinding.ActivityMainBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isInbound = intent.getBooleanExtra("isInbound", true)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val viewModel: SharedViewModel by viewModels()
        viewModel.setIsInbound(isInbound)
        val navView: BottomNavigationView = binding.navView
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val userName = intent.getStringExtra("userName")
        val userRole = intent.getStringExtra("userRole")

        binding.toolbarTitle.text  = "${userRole}-${userName}"
        setSupportActionBar(toolbar)
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)
        if(!isInbound){
            navController.navigate(R.id.navigation_notifications)
        }
    }
}