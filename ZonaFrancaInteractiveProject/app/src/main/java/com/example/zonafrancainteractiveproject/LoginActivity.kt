package com.example.zonafrancainteractiveproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.OutputStreamWriter

class LoginActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var sharedPreferences: android.content.SharedPreferences
    private lateinit var notRegisteredButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loginpage)

        emailEditText = findViewById(R.id.LoginEmail)
        passwordEditText = findViewById(R.id.LoginPassword)
        loginButton = findViewById(R.id.SendLoginButton)
        sharedPreferences = this.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        notRegisteredButton = findViewById(R.id.SendToRegiterButton)

        notRegisteredButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        Thread {
            try {
                val url = URL("http://192.168.199.174:8000/api/login")
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "POST"
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                urlConnection.doOutput = true

                val out = BufferedWriter(OutputStreamWriter(urlConnection.outputStream))
                out.write("email=$email&password=$password")
                out.flush()
                out.close()

                val responseCode = urlConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = BufferedReader(InputStreamReader(urlConnection.inputStream)).use { it.readText() }
                    val jsonObject = JSONObject(response)
                    val token = jsonObject.getString("token")
                    sharedPreferences.edit().apply {
                        putString("token", token)
                        putBoolean("isLoggedIn", true)
                        apply()
                    }

                    Log.d("LoginActivity", "Token stored: $token")

                    runOnUiThread {
                        showAlertDialog("Login exitoso", "¡Inicio de sesión completado exitosamente!")
                        startActivity(Intent(this, MapActivity::class.java))
                    }
                } else {
                    val response = BufferedReader(InputStreamReader(urlConnection.errorStream)).use { it.readText() }
                    val jsonObject = JSONObject(response)
                    val errorMessage = jsonObject.optString("message", "Login fallido")

                    runOnUiThread {
                        showAlertDialog("Login fallido", errorMessage)
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showAlertDialog("Error en login", "Error: ${e.message}")
                }
                Log.e("LoginActivity", "Error en login: ${e.message}", e)
            }
        }.start()
    }


    private fun showAlertDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
}
