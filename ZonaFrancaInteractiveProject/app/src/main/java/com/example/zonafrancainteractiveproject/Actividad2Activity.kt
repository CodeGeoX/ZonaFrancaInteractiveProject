package com.example.zonafrancainteractiveproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class Actividad2Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actividad02)

        val btnExit = findViewById<Button>(R.id.btnExit)

        btnExit.setOnClickListener {
            finish()
        }
    }
}