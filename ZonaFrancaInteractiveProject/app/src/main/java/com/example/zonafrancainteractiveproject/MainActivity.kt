package com.example.zonafrancainteractiveproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loginButton = findViewById<Button>(R.id.LoginButton)
        val registerButton = findViewById<Button>(R.id.registerbutton)
        val invitedButton =
            findViewById<Button>(R.id.InvitedButton)

        registerButton.setOnClickListener { v: View? ->
            startActivity(
                Intent(
                    this,
                    RegisterActivity::class.java
                )
            )
        }

        loginButton.setOnClickListener { v: View? ->
            startActivity(
                Intent(
                    this,
                    LoginActivity::class.java
                )
            )
        }

        invitedButton.setOnClickListener { v: View? ->
            val intent = Intent(
                this,
                MapActivity::class.java
            )
            intent.putExtra(
                "guestMode",
                true
            )
            startActivity(intent)
        }
    }
}
