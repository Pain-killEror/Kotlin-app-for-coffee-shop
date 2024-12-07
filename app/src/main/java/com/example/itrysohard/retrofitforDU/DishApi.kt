package com.example.itrysohard.retrofitforDU

import com.example.itrysohard.model.DishServ
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
        @Part image: MultipartBody.Part,
        @Part ("category") category: RequestBody
    ): Call<DishServ>

    @DELETE("/dish/delete/{id}")
    fun deleteDish(@Path("id") id: Int): Call<Void>

    @Multipart
    @PUT("/dish/update/{id}")
    fun updateDish(
        @Path("id") id: Int,
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody,
        @Part("category") category: RequestBody,
        @Part image: MultipartBody.Part? // image is optional
    ): Call<DishServ>
}
