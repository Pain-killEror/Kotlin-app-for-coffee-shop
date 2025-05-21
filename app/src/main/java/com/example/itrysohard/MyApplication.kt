package com.example.itrysohard

import android.app.Application
import com.bumptech.glide.Glide
import com.example.itrysohard.model.DishServ

class MyApplication : Application() {
    val cartItems = mutableListOf<DishServ>()
    var cartItemCount: Int = 0
    val selectedSizes = mutableMapOf<Long, String>()


    override fun onCreate() {
        super.onCreate()

        // Очистить кэш в Glide при открытии приложения
        clearGlideCache()

    }

    private fun clearGlideCache() {
        Glide.get(applicationContext).clearMemory() // Очистка кэша в памяти
        Thread {
            Glide.get(applicationContext).clearDiskCache() // Очистка кэша на диске
        }.start()
    }




}