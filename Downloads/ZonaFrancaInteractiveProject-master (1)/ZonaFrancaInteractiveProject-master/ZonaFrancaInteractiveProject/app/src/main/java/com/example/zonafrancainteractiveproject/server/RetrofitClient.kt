package com.example.ac2_fragmentsmapes.server

import com.example.ac2_fragmentsmapes.models.Equipment
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET


object RetrofitClient {
    private const val BASE_URL = "https://analisi.transparenciacatalunya.cat/"

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}

interface ApiService {
    @GET("resource/8gmd-gz7i.json?")
    fun getEquipments(): Call<List<Equipment>>
}


