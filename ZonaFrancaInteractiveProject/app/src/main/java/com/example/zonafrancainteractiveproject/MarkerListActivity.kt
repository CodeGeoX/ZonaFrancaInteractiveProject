package com.example.zonafrancainteractiveproject

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zonafrancainteractiveproject.R

class MarkerListActivity : AppCompatActivity() {

    private lateinit var adapter: MarkerAdapter
    private lateinit var markerList: ArrayList<MarkerData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marker_list)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewMarkers)
        recyclerView.layoutManager = LinearLayoutManager(this)

        markerList = intent.getSerializableExtra("markerList") as? ArrayList<MarkerData> ?: arrayListOf()

        adapter = MarkerAdapter(this, markerList)
        recyclerView.adapter = adapter

        val btnExit = findViewById<Button>(R.id.btnExit)
        btnExit.setOnClickListener {
            finish()
        }

        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })
    }
}