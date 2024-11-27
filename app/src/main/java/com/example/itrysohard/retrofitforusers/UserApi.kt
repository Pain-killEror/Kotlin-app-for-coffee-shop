package com.example.myappforcafee.retrofit

import com.example.myappforcafee.model.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface UserApi {
    @GET("/user/get-all")
    fun getAllUsers(): Call<List<User>>

    @POST("/user/save")
    fun save(@Body user: User): Call<User>
}