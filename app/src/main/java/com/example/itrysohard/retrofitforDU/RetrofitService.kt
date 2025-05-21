package com.example.itrysohard.retrofitforDU

import android.content.Context
import com.example.itrysohard.jwt.AuthInterceptor
import com.example.itrysohard.jwt.TokenManager
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitService(private val context: Context, private val tokenManager: TokenManager) {
    private val retrofit: Retrofit

    init {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context, tokenManager))
            .build()



        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss") // Формат даты с сервера
            .setLenient() // Добавьте эту строку
            .create()

        retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.154:8080/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    fun getRetrofit(): Retrofit {
        return retrofit
    }

    fun getOrderApi(): OrderApi {
        return getRetrofit().create(OrderApi::class.java)
    }

    fun getDishApi(): DishApi {
        return getRetrofit().create(DishApi::class.java)
    }

    fun getUserApi(): UserApi {
        return retrofit.create(UserApi::class.java)
    }
}