package com.example.itrysohard.retrofitforDU

import com.example.itrysohard.model.Review
import com.example.itrysohard.model.answ.ReviewAnswDTO
import retrofit2.Call
import retrofit2.http.*

interface ReviewApi {


    @GET("review/all")
    fun getAllReviews(): Call<List<ReviewAnswDTO>>


    @POST("review/create")
    fun addReview(@Body review: Review): Call<Void>

    @DELETE("/review/delete/{id}") // Путь к вашему API для удаления отзыва
    fun deleteReview(@Path("id") id: Long?): Call<Void>

}