package com.example.zonafrancainteractiveproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class RegisterActivity : AppCompatActivity() {
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registerpage)

        nameEditText = findViewById(R.id.RegisterName)
        emailEditText = findViewById(R.id.RegisterEmail)
        passwordEditText = findViewById(R.id.RegisterPassword)
        registerButton = findViewById(R.id.SendRegisterButton)

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
                val url = URL("http://192.168.76.174:8000/api/register") // Cambia por tu URL de la API
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "POST"
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                urlConnection.doOutput = true

                val out = BufferedWriter(OutputStreamWriter(urlConnection.outputStream))
                out.write("name=$name&email=$email&password=$password")
                out.close()

                val responseCode = urlConnection.responseCode

                // Logs para identificar el código de respuesta
                Log.d("RegisterActivity", "Registro Response Code: $responseCode")

                runOnUiThread {
                    if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, SuccessActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Registro fallido", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                // Log para identificar el error
                Log.e("RegisterActivity", "Registro Error: ${e.message}", e)

                runOnUiThread {
                    Toast.makeText(this, "Registro Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

}
