package com.example.zonafrancainteractiveproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        if (isUserLoggedIn()) {
            redirectToMapActivity()
        }

        val loginButton = findViewById<Button>(R.id.LoginButton)
        val registerButton = findViewById<Button>(R.id.registerbutton)
        val invitedButton = findViewById<Button>(R.id.InvitedButton)

        registerButton.setOnClickListener { v: View? ->
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        loginButton.setOnClickListener { v: View? ->
            startActivity(Intent(this, LoginActivity::class.java))
        }

        invitedButton.setOnClickListener { v: View? ->
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra("guestMode", true)
            startActivity(intent)
        }
    }

    private fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }

    private fun redirectToMapActivity() {
        startActivity(Intent(this, MapActivity::class.java))
        finish()
    }
}
