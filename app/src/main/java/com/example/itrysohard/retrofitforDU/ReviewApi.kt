package com.example.itrysohard.retrofitforDU

import com.example.itrysohard.model.Review
import retrofit2.Call
import retrofit2.http.*

interface ReviewApi {

    @GET("/review/get-all") // Путь к вашему API для получения всех отзывов
    fun getAllReviews(): Call<List<Review>>

    @POST("/review/add") // Путь к вашему API для добавления нового отзыва
    fun addReview(@Body review: Review): Call<Review>

    @DELETE("/review/delete/{id}") // Путь к вашему API для удаления отзыва
    fun deleteReview(@Path("id") id: Long?): Call<Void>

}