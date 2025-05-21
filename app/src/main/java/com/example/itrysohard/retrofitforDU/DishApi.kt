package com.example.itrysohard.retrofitforDU

import com.example.itrysohard.model.DishServ
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface DishApi {

    @GET("/dishes/all")
    fun getAllDishes(): Call<List<DishServ>>

    @GET("/dishes/getImg/{filename}")
    fun getImage(
        @Path("filename") filename: String
    ): Call<ResponseBody>

    @Multipart
    @POST("/dishes/create")
    fun uploadDish(
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody,
        @Part("volume") volume: RequestBody,
        @Part photo: MultipartBody.Part?, // имя параметра "photo" должно совпадать с сервером
        @Part("category") category: RequestBody, // исправлено имя параметра
        @Part("discount") discount: RequestBody
    ): Call<ResponseBody> // Измените тип ответа, так как сервер возвращает No Content

    @DELETE("/dishes/delete/{id}")
    fun deleteDish(@Path("id") id: Long): Call<Void>

    @DELETE("/dishes/deleteByName/{name}")
    fun deleteDishByName(@Path("name") name: String): Call<Void>

    @Multipart
    @PUT("/dishes/update/{id}")
    fun updateDish(
        @Path("id") id: Long,
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody,
        @Part("volume") volume: RequestBody,
        @Part("category") category: RequestBody,
        @Part photo: MultipartBody.Part? = null, // имя параметра "photo" (обязательно!)
        @Part("discount") discount: RequestBody
    ): Call<ResponseBody>
}
