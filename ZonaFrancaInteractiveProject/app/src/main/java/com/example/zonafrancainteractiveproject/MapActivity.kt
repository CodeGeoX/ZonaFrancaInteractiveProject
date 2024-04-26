package com.example.zonafrancainteractiveproject

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.widget.EditText
import android.widget.LinearLayout
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.io.OutputStreamWriter
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private lateinit var mMap: GoogleMap
    private lateinit var sharedPreferences: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val zonaFrancaBarcelona = LatLng(41.3544, 2.1265)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(zonaFrancaBarcelona, 15f))
        mMap.setOnMapClickListener(this)
        loadUserInterests();
    }
    override fun onMapClick(latlng: LatLng) {
        showInputDialog(latlng)
    }

    private fun showInputDialog(latlng: LatLng) {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        val titleBox = EditText(this).apply { hint = "Title" }
        val descriptionBox = EditText(this).apply { hint = "Description" }
        layout.apply {
            addView(titleBox)
            addView(descriptionBox)
        }

        AlertDialog.Builder(this).apply {
            setTitle("New Marker")
            setView(layout)
            setPositiveButton("OK") { _, _ ->
                val title = titleBox.text.toString()
                val description = descriptionBox.text.toString()
                addMarker(latlng, title, description)
                saveMarkerToServer(latlng, title, description)
            }
            setNegativeButton("Cancel", null)
            show()
        }
    }

    private fun addMarker(latlng: LatLng, title: String, description: String) {
        mMap.addMarker(MarkerOptions().position(latlng).title(title).snippet(description))
    }

    private fun saveMarkerToServer(latlng: LatLng, title: String, description: String) {
        Thread {
            try {
                val token = sharedPreferences.getString("token", null)
                val url = URL("http://192.168.76.174:8000/api/crearPunto")
                (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                    setRequestProperty("Authorization", "Bearer $token")
                    doOutput = true

                    OutputStreamWriter(outputStream).use { out ->
                        val postData = "title=${URLEncoder.encode(title, StandardCharsets.UTF_8.toString())}" +
                                "&description=${URLEncoder.encode(description, StandardCharsets.UTF_8.toString())}" +
                                "&lat=${latlng.latitude}&long=${latlng.longitude}"
                        out.write(postData)
                    }

                    val responseMessage = inputStream.bufferedReader().use { it.readText() }
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Log.i("MapActivity", "CreaciondePuntos Point saved successfully: $responseMessage")
                    } else {
                        Log.e("MapActivity", "CreaciondePuntos Server error: $responseMessage")
                    }
                }
            } catch (e: Exception) {
                Log.e("MapActivity", "CreaciondePuntos Error saving point: ${e.message}", e)
            }
        }.start()
    }

    private fun loadUserInterests() {
        Thread {
            try {
                val token = sharedPreferences.getString("token", null)
                val url = URL("http://192.168.76.174:8000/api/getUserPoints")
                (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Authorization", "Bearer $token")
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val response = InputStreamReader(inputStream).use { it.readText() }
                        val responseObject = JSONObject(response)
                        val jsonArray = responseObject.getJSONArray("data")
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val lat = jsonObject.getDouble("lat")
                            val long = jsonObject.getDouble("long")
                            val title = jsonObject.getString("title")
                            val description = jsonObject.getString("description")
                            runOnUiThread {
                                addMarker(LatLng(lat, long), title, description)
                            }
                        }
                    } else {
                        val errorMessage = InputStreamReader(inputStream).use { it.readText() }
                        Log.e("MapActivity", "Error loading points: $errorMessage")
                    }
                }
            } catch (e: Exception) {
                Log.e("MapActivity", "Error loading points: ${e.message}", e)
            }
        }.start()
    }

}
