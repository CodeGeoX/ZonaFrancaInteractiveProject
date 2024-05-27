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
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var sharedPreferences: android.content.SharedPreferences
    private lateinit var alreadyLoggedInButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registerpage)

        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        nameEditText = findViewById(R.id.RegisterName)
        emailEditText = findViewById(R.id.RegisterEmail)
        passwordEditText = findViewById(R.id.RegisterPassword)
        registerButton = findViewById(R.id.SendRegisterButton)
        alreadyLoggedInButton = findViewById(R.id.SendBackToLoginButton)

        alreadyLoggedInButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        registerButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            registerUser(name, email, password)
        }
    }

    private fun registerUser(name: String, email: String, password: String) {
        Thread {
            try {
                val url = URL("http://172.20.10.2:8000/api/register")
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "POST"
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                urlConnection.doOutput = true

                val out = BufferedWriter(OutputStreamWriter(urlConnection.outputStream))
                out.write("name=$name&email=$email&password=$password")
                out.flush()
                out.close()

                val responseCode = urlConnection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    val response = BufferedReader(InputStreamReader(urlConnection.inputStream)).use { it.readText() }
                    val jsonObject = JSONObject(response)
                    val token = jsonObject.getString("token")

                    sharedPreferences.edit().putString("token", token).apply()

                    Log.d("RegisterActivity", "Token stored: $token")

                    runOnUiThread {
                        showAlertDialog("Registro exitoso", "¡Registro completado exitosamente!")
                        startActivity(Intent(this, MapActivity::class.java))
                    }
                } else {
                    val response = BufferedReader(InputStreamReader(urlConnection.errorStream)).use { it.readText() }
                    val jsonObject = JSONObject(response)
                    val errorMessage = jsonObject.optString("message", "Registro fallido")

                    runOnUiThread {
                        showAlertDialog("Registro fallido", errorMessage)
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showAlertDialog("Error en registro", "Error en registro: ${e.message}")
                }
                Log.e("RegisterActivity", "Error en registro: ${e.message}", e)
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
