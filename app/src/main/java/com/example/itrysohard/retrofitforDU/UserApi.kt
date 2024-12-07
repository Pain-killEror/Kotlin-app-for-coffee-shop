package com.example.itrysohard.retrofitforDU

import com.example.itrysohard.model.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserApi {
    @GET("api/users/get-all")
    fun getAllUsers(): Call<List<User>>

    @POST("api/users/save")
    fun save(@Body user: User): Call<User>

    @DELETE("api/users/{id}")
    fun deleteUser(@Path("id") id: Long): Call<Void>
}