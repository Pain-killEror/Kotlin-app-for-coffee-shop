package com.example.myappforcafee.retrofit

import com.example.myappforcafee.model.DishServ
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface DishApi {

    @GET("/dish/get-all")
    fun getAllDishes(): Call<List<DishServ>>

    @Multipart
    @POST("/dish/add")
    fun uploadDish(
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody,
        @Part image: MultipartBody.Part
    ): Call<DishServ>

    @DELETE("/dish/delete/{id}")
    fun deleteDish(@Path("id") id: Int): Call<Void>
}
