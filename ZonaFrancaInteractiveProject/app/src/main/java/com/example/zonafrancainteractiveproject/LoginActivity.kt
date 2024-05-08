package com.example.zonafrancainteractiveproject
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
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
                out.close()

                val responseCode = urlConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = BufferedReader(InputStreamReader(urlConnection.inputStream)).use { it.readText() }
                    val jsonObject = JSONObject(response)
                    val token = jsonObject.getString("token")
                    sharedPreferences.edit().putString("token", token).apply()

                    Log.d("LoginActivity", "Token stored: $token")

                    runOnUiThread {
                        Toast.makeText(this, "Login exitoso", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this, MapActivity::class.java))
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Login fallido", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
}
