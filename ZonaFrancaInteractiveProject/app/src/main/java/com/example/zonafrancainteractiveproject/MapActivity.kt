package com.example.zonafrancainteractiveproject

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
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
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    private var selectedImageView: ImageView? = null

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

    private fun toggleEditMode() {
        editMode = !editMode
        val editModeButton = findViewById<Button>(R.id.editModeButton)
        editModeButton.text = if (editMode) "Modo de visualización" else "Editar/eliminar modo"
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

        val btnActividad = findViewById<ImageView>(R.id.btnActividad)
        val linearLayoutActividad = findViewById<LinearLayout>(R.id.linearLayoutActividad)
        btnActividad.setOnClickListener {
            toggleLinearLayoutVisibility(linearLayoutActividad)
        }

        val btnToAct01 = findViewById<Button>(R.id.btnToAct01)
        val btnToAct02 = findViewById<Button>(R.id.btnToAct02)
        val btnToAct03 = findViewById<Button>(R.id.btnToAct03)
        val btnToAct04 = findViewById<Button>(R.id.btnToAct04)

        btnToAct01.setOnClickListener {
            startActivity(Intent(this, Actividad1Activity::class.java))
        }

        btnToAct02.setOnClickListener {
            startActivity(Intent(this, Actividad2Activity::class.java))
        }

        btnToAct03.setOnClickListener {
            startActivity(Intent(this, Actividad3Activity::class.java))
        }

        btnToAct04.setOnClickListener {
            startActivity(Intent(this, Actividad4Activity::class.java))
        }

        val btnParadas = findViewById<ImageView>(R.id.btnParadas)
        val linearLayoutParadas = findViewById<LinearLayout>(R.id.linearLayoutParadas)
        btnParadas.setOnClickListener {
            toggleLinearLayoutVisibility(linearLayoutParadas)
        }

        val btnLogout = findViewById<ImageView>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            toolbarLogout()
        }
    }

    private fun toggleLinearLayoutVisibility(linearLayout: LinearLayout) {
        if (linearLayout.visibility == View.VISIBLE) {
            linearLayout.visibility = View.GONE
        } else {
            linearLayout.visibility = View.VISIBLE
        }
    }

    private fun toolbarLogout() {
        if (isUserLoggedIn()) {
            performLogout()
            redirectToMainActivity()
        } else {
            showConfirmationDialog()
        }
    }

    private fun isUserLoggedIn(): Boolean {
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        return isLoggedIn
    }

    private fun performLogout() {
        sharedPreferences.edit().clear().apply()
    }

    private fun getMarkerIcon(color: String): Float {
        return when (color) {
            "Red" -> BitmapDescriptorFactory.HUE_RED
            "Blue" -> BitmapDescriptorFactory.HUE_BLUE
            "Green" -> BitmapDescriptorFactory.HUE_GREEN
            "Yellow" -> BitmapDescriptorFactory.HUE_YELLOW
            else -> BitmapDescriptorFactory.HUE_RED
        }
    }

    private fun redirectToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showConfirmationDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("No estás logueado")
            setMessage("¿Deseas iniciar sesión ahora?")
            setPositiveButton("Aceptar") { dialog, _ ->
                redirectToMainActivity()
                dialog.dismiss()
            }
            setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
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
        val markerId = markerMap[marker]
        if (markerId != null) {
            Thread {
                try {
                    val token = sharedPreferences.getString("token", null)
                    val url = URL("http://192.168.199.174:8000/api/places_of_interests/$markerId")
                    (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        setRequestProperty("Authorization", "Bearer $token")
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            val response = inputStream.bufferedReader().use { it.readText() }
                            val jsonResponse = JSONObject(response)
                            val title = jsonResponse.getJSONObject("data").getString("title")
                            val description = jsonResponse.getJSONObject("data").getString("description")
                            val imageUrl = jsonResponse.getJSONObject("data").optString("image_path")

                            runOnUiThread {
                                val builder = AlertDialog.Builder(this@MapActivity)
                                builder.setTitle(title)
                                builder.setMessage(description)

                                if (imageUrl.isNotEmpty()) {
                                    val fullImageUrl = "http://192.168.199.174:8000/$imageUrl"
                                    Log.d("MapActivity", "Loading image from URL: $fullImageUrl")

                                    val imageView = ImageView(this@MapActivity)
                                    Glide.with(this@MapActivity).load(fullImageUrl).into(imageView)
                                    builder.setView(imageView)
                                }

                                builder.setPositiveButton("OK", null)
                                builder.show()
                            }
                        } else {
                            val errorMessage = inputStream.bufferedReader().use { it.readText() }
                            Log.e("MapActivity", "Error loading marker details: $errorMessage")
                            runOnUiThread {
                                Toast.makeText(this@MapActivity, "Error loading marker details: $responseCode", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MapActivity", "Error loading marker details: ${e.message}", e)
                    runOnUiThread {
                        Toast.makeText(this@MapActivity, "Error loading marker details", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }
    }




    private fun showInputDialog(latlng: LatLng) {
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_marker, null)

        val titleBox = dialogView.findViewById<EditText>(R.id.titleBox)
        val descriptionBox = dialogView.findViewById<EditText>(R.id.descriptionBox)
        val colorSpinner = dialogView.findViewById<Spinner>(R.id.colorSpinner).apply {
            val colors = arrayOf("Red", "Blue", "Green", "Yellow")
            adapter = ArrayAdapter(this@MapActivity, android.R.layout.simple_spinner_dropdown_item, colors)
        }

        val selectImageButton = dialogView.findViewById<Button>(R.id.selectImageButton)
        selectedImageView = dialogView.findViewById(R.id.selectedImageView)

        selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        AlertDialog.Builder(this).apply {
            setTitle("New Marker")
            setView(dialogView)
            setPositiveButton("OK") { _, _ ->
                val title = titleBox.text.toString()
                val description = descriptionBox.text.toString()
                val color = colorSpinner.selectedItem.toString()
                addMarker(latlng, title, description, color)
                saveMarkerToServer(latlng, title, description, color)
            }
            setNegativeButton("Cancel", null)
            show()
        }
    }

    private fun addMarker(latlng: LatLng, title: String, description: String, color: String, markerId: Int? = null) {
        val markerOptions = MarkerOptions()
            .position(latlng)
            .title(title)
            .snippet(description)
            .icon(BitmapDescriptorFactory.defaultMarker(getMarkerIcon(color)))

        val marker = mMap.addMarker(markerOptions)
        marker?.let {
            it.tag = color
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
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_marker, null)

        val titleBox = dialogView.findViewById<EditText>(R.id.titleBox).apply {
            setText(marker.title)
        }
        val descriptionBox = dialogView.findViewById<EditText>(R.id.descriptionBox).apply {
            setText(marker.snippet)
        }
        val colorSpinner = dialogView.findViewById<Spinner>(R.id.colorSpinner).apply {
            val colors = arrayOf("Red", "Blue", "Green", "Yellow")
            adapter = ArrayAdapter(this@MapActivity, android.R.layout.simple_spinner_dropdown_item, colors)
        }

        AlertDialog.Builder(this).apply {
            setTitle("Editar Marcador")
            setView(dialogView)
            setPositiveButton("OK") { _, _ ->
                val newTitle = titleBox.text.toString()
                val newDescription = descriptionBox.text.toString()
                val newColor = colorSpinner.selectedItem.toString()
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

    private fun saveMarkerToServer(latlng: LatLng, title: String, description: String, color: String) {
        Thread {
            try {
                val token = sharedPreferences.getString("token", null)
                val url = URL("http://192.168.199.174:8000/api/crearPunto")
                (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                    setRequestProperty("Authorization", "Bearer $token")
                    doOutput = true

                    val postData = StringBuilder().apply {
                        append("title=${URLEncoder.encode(title, StandardCharsets.UTF_8.toString())}")
                        append("&description=${URLEncoder.encode(description, StandardCharsets.UTF_8.toString())}")
                        append("&lat=${latlng.latitude}")
                        append("&long=${latlng.longitude}")
                        append("&color=${URLEncoder.encode(color, StandardCharsets.UTF_8.toString())}")

                        if (imageUri != null) {
                            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                            val byteArrayOutputStream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
                            val imageBytes = byteArrayOutputStream.toByteArray()
                            val encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT)
                            append("&image=$encodedImage")
                        }
                    }.toString()

                    OutputStreamWriter(outputStream).use { out ->
                        out.write(postData)
                    }

                    val responseCode = responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val responseMessage = inputStream.bufferedReader().use { it.readText() }
                        if (responseMessage.startsWith("{")) {
                            val jsonResponse = JSONObject(responseMessage)
                            val markerId = jsonResponse.getInt("id")
                            runOnUiThread {
                                val marker = mMap.addMarker(MarkerOptions().position(latlng).title(title).snippet(description))
                                marker?.let {
                                    it.tag = color
                                    markerMap[it] = markerId
                                }
                            }
                            Log.i("MapActivity", "Point saved successfully: $responseMessage")
                        } else {
                            Log.e("MapActivity", "Server error: $responseMessage")
                        }
                    } else {
                        Log.e("MapActivity", "Error: $responseCode")
                    }
                }
            } catch (e: Exception) {
                Log.e("MapActivity", "Error saving point: ${e.message}", e)
            }
        }.start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            selectedImageView?.setImageURI(imageUri)
            selectedImageView?.visibility = View.VISIBLE
        }
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
                            val color = jsonObject.getString("color")
                            val id = jsonObject.getInt("id")
                            runOnUiThread {
                                addMarker(LatLng(lat, long), title, description, color, id)
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
