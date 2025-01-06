package com.example.itrysohard.retrofitforDU

import com.example.itrysohard.model.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApi {
    @GET("api/users/get-all")
    fun getAllUsers(): Call<List<User>>

    @GET ("api/users/name/{name}")
    fun getUserByName(@Path("name") name: String): Call<User>

    @GET ("api/users/email/{email}")
    fun getUserByEmail(@Path("email") name: String): Call<User>

    @POST("users/login")
    fun loginUser(@Body user: String): Call<String>

    @POST("api/users/save")
    fun save(@Body user: User): Call<User>

    @DELETE("api/users/{id}")
    fun deleteUser(@Path("id") id: Long): Call<Void>

    @PUT("api/users/block/{id}")
    fun updateUserBlockedStatus(@Path("id") id: Long, @Query("blocked") blocked: Boolean): Call<Void>
}