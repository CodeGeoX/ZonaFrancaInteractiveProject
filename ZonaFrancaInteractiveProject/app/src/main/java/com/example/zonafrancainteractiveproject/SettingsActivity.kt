package com.example.zonafrancainteractiveproject

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.preference.PreferenceManager
import android.widget.Button
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<Button>(R.id.btnLanguageCatalan).setOnClickListener { setLocale("ca") }
        findViewById<Button>(R.id.btnLanguageSpanish).setOnClickListener { setLocale("es") }
        findViewById<Button>(R.id.btnLanguageEnglish).setOnClickListener { setLocale("en") }

        val btnExit = findViewById<Button>(R.id.btnBack)
        btnExit.setOnClickListener {
            finish()
        }
    }

    private fun setLocale(lang: String) {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        resources.updateConfiguration(config, resources.displayMetrics)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = preferences.edit()
        editor.putString("language", lang)
        editor.apply()

        val intent = Intent(this, MapActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}