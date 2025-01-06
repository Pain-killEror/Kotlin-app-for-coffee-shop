package com.example.itrysohard.retrofitforDU

import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitService {
    private val retrofit: Retrofit

    init {
        retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.154:8080/")
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
    }
    
    fun getRetrofit(): Retrofit {
        return retrofit
    }

    fun getUserApi(): UserApi {
        return retrofit.create(UserApi::class.java)
    }

}