package com.example.itrysohard

import android.app.Application
import android.graphics.drawable.Drawable
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.example.myappforcafee.retrofit.DishApi
import com.example.myappforcafee.retrofit.RetrofitService
import com.example.myappforcafee.model.DishServ
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Очистить кэш в Glide при открытии приложения
        clearGlideCache()

        // Загрузить блюда с сервера в кэш
        loadDishesFromServer()
    }

    private fun clearGlideCache() {
        Glide.get(applicationContext).clearMemory() // Очистка кэша в памяти
        Thread {
            Glide.get(applicationContext).clearDiskCache() // Очистка кэша на диске
        }.start()
    }

    private fun loadDishesFromServer() {
        val retrofitService = RetrofitService()
        val dishApi = retrofitService.getRetrofit().create(DishApi::class.java)

        dishApi.getAllDishes().enqueue(object : Callback<List<DishServ>> {
            override fun onResponse(call: Call<List<DishServ>>, response: Response<List<DishServ>>) {
                if (response.isSuccessful) {
                    val dishes = response.body() ?: emptyList()
                    cacheImages(dishes) // Кэшируем изображения
                }
            }

            override fun onFailure(call: Call<List<DishServ>>, t: Throwable) {
                // Обработка ошибки сети, если нужно
            }
        })
    }

    private fun cacheImages(dishes: List<DishServ>) {
        for (dish in dishes) {
            Log.d("MyLog", "Кэшируем изображение: ${dish.imageUrl}")
            Glide.with(applicationContext)
                .load(dish.imageUrl)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e("MyLog", "Ошибка загрузки изображения: ${dish.imageUrl}", e)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d("MyLog", "Изображение загружено и кэшировано: ${dish.imageUrl}")
                        return false
                    }
                })
                .preload()  // Используйте preload для кэширования
        }
    }
}