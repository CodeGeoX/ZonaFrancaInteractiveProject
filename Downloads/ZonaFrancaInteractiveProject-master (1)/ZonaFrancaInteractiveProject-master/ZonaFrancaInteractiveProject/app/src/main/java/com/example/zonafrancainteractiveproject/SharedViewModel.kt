package com.example.zonafrancainteractiveproject

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ac2_fragmentsmapes.models.Equipment
import com.google.android.gms.maps.model.LatLng

class SharedViewModel : ViewModel() {
    val selectedEquipment = MutableLiveData<Equipment>()
    val selectedMarkerColor = MutableLiveData<Float>()
    val allowMultipleMarkers = MutableLiveData<Boolean>().apply { value = false }
}

