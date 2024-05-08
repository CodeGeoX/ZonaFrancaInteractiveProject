package com.example.zonafrancainteractiveproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONObject
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private lateinit var mMap: GoogleMap
    private lateinit var sharedPreferences: android.content.SharedPreferences
    private val markerMap = mutableMapOf<Marker, Int>()
    private var editMode = false
    private var isGuest: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        isGuest = intent.getBooleanExtra("guestMode", false)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        val editModeButton = findViewById<Button>(R.id.editModeButton)
        editModeButton.setOnClickListener {
            if (!isGuest) {
                toggleEditMode()
            } else {
                showGuestAlert()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(41.3544, 2.1265), 15f))
        mMap.setOnMapClickListener(this)
        if (!isGuest) {
            loadUserInterests()
        }

        mMap.setOnMarkerClickListener { marker ->
            if (!editMode) {
                showMarkerInfoDialog(marker)
            } else {
                showEditDeleteDialog(marker)
            }
            true
        }
    }


    private fun toggleEditMode() {
        editMode = !editMode
        val editModeButton = findViewById<Button>(R.id.editModeButton)
        editModeButton.text = if (editMode) "Modo de visualización" else "Editar/eliminar modo"
    }
    override fun onMapClick(latlng: LatLng) {
        if (!isGuest || !editMode) {
            showInputDialog(latlng)
        } else {
            showGuestAlert()
        }
    }

    private fun showGuestAlert() {
        AlertDialog.Builder(this).apply {
            setTitle("Acción Restringida")
            setMessage("Debe estar registrado para realizar esta acción. ¿Desea registrarse ahora?")
            setPositiveButton("Registrar") { _, _ ->
                // Redirigir al usuario a la pantalla de registro
                startActivity(Intent(this@MapActivity, RegisterActivity::class.java))
            }
            setNegativeButton("Cancelar", null)
            show()
        }
    }
    private fun showMarkerInfoDialog(marker: Marker) {
        AlertDialog.Builder(this).apply {
            setTitle(marker.title)
            setMessage(marker.snippet)
            setPositiveButton("OK", null)
            show()
        }
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

    private fun addMarker(latlng: LatLng, title: String, description: String, markerId: Int? = null) {
        val marker = mMap.addMarker(MarkerOptions().position(latlng).title(title).snippet(description))
        marker?.let {
            if (markerId != null) markerMap[it] = markerId
        }
    }

    private fun showEditDeleteDialog(marker: Marker) {
        if (editMode) {
            val items = arrayOf("Editar", "Eliminar")
            AlertDialog.Builder(this).apply {
                setTitle("Seleccione una opción")
                setItems(items) { dialog, which ->
                    when (which) {
                        0 -> showEditDialog(marker)
                        1 -> deleteMarker(marker)
                    }
                }
                show()
            }
        }
    }

    private fun showEditDialog(marker: Marker) {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        val titleBox = EditText(this).apply {
            hint = "Title"
            setText(marker.title)
        }
        val descriptionBox = EditText(this).apply {
            hint = "Description"
            setText(marker.snippet)
        }
        layout.apply {
            addView(titleBox)
            addView(descriptionBox)
        }

        AlertDialog.Builder(this).apply {
            setTitle("Editar Marcador")
            setView(layout)
            setPositiveButton("OK") { _, _ ->
                val newTitle = titleBox.text.toString()
                val newDescription = descriptionBox.text.toString()
                marker.title = newTitle
                marker.snippet = newDescription
                updateMarkerToServer(markerMap[marker]!!, newTitle, newDescription)
            }
            setNegativeButton("Cancelar", null)
            show()
        }
    }

    private fun deleteMarker(marker: Marker) {
        AlertDialog.Builder(this).apply {
            setTitle("Confirmar Eliminación")
            setMessage("¿Está seguro que desea eliminar este punto de interés?")
            setPositiveButton("Eliminar") { _, _ ->
                deleteMarkerFromServer(markerMap[marker]!!)
                marker.remove()
            }
            setNegativeButton("Cancelar", null)
            show()
        }
    }

    private fun updateMarkerToServer(markerId: Int, title: String, description: String) {
        Thread {
            try {
                val token = sharedPreferences.getString("token", null)
                val url = URL("http://192.168.199.174:8000/api/places_of_interests/$markerId")
                (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                    setRequestProperty("Authorization", "Bearer $token")
                    doOutput = true

                    OutputStreamWriter(outputStream).use { out ->
                        val postData = "title=${URLEncoder.encode(title, StandardCharsets.UTF_8.toString())}" +
                                "&description=${URLEncoder.encode(description, StandardCharsets.UTF_8.toString())}"
                        out.write(postData)
                    }

                    val responseMessage = inputStream.bufferedReader().use { it.readText() }
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Log.i("MapActivity", "Update Point successfully: $responseMessage")
                    } else {
                        Log.e("MapActivity", "Server error on updating: $responseMessage")
                    }
                }
            } catch (e: Exception) {
                Log.e("MapActivity", "Error updating point: ${e.message}", e)
            }
        }.start()
    }
    private fun deleteMarkerFromServer(markerId: Int) {
        Thread {
            try {
                val token = sharedPreferences.getString("token", null)
                val url = URL("http://192.168.199.174:8000/api/places_of_interests/$markerId")
                (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "DELETE"
                    setRequestProperty("Authorization", "Bearer $token")

                    val responseMessage = inputStream.bufferedReader().use { it.readText() }
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Log.i("MapActivity", "Point deleted successfully: $responseMessage")
                    } else {
                        Log.e("MapActivity", "Server error on deleting: $responseMessage")
                    }
                }
            } catch (e: Exception) {
                Log.e("MapActivity", "Error deleting point: ${e.message}", e)
            }
        }.start()
    }

    private fun saveMarkerToServer(latlng: LatLng, title: String, description: String) {
        Thread {
            try {
                val token = sharedPreferences.getString("token", null)
                val url = URL("http://192.168.199.174:8000:8000/api/crearPunto")
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
                        val jsonResponse = JSONObject(responseMessage)
                        val markerId = jsonResponse.getInt("id")
                        runOnUiThread {
                            val marker = mMap.addMarker(MarkerOptions().position(latlng).title(title).snippet(description))
                            marker?.let { markerMap[it] = markerId }
                        }
                        Log.i("MapActivity", "Point saved successfully: $responseMessage")
                    } else {
                        Log.e("MapActivity", "Server error: $responseMessage")
                    }
                }
            } catch (e: Exception) {
                Log.e("MapActivity", "Error saving point: ${e.message}", e)
            }
        }.start()
    }


    private fun loadUserInterests() {
        Thread {
            try {
                val token = sharedPreferences.getString("token", null)
                val url = URL("http://192.168.199.174:8000/api/getUserPoints")
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
                            val id = jsonObject.getInt("id")
                            runOnUiThread {
                                addMarker(LatLng(lat, long), title, description, id)
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
