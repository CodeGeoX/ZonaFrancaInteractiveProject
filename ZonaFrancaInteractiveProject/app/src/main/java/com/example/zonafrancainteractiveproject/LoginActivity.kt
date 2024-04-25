package com.example.zonafrancainteractiveproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class LoginActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loginpage)

        emailEditText = findViewById(R.id.LoginEmail)
        passwordEditText = findViewById(R.id.LoginPassword)
        loginButton = findViewById(R.id.SendLoginButton)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        Thread {
            try {
                val url = URL("http://192.168.76.174:8000/api/login")
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "POST"
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                urlConnection.doOutput = true

                val out = BufferedWriter(OutputStreamWriter(urlConnection.outputStream))
                out.write("email=$email&password=$password")
                out.close()

                val responseCode = urlConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    runOnUiThread {
                        Toast.makeText(this, "Login exitoso", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, SuccessActivity::class.java)
                        startActivity(intent)
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
