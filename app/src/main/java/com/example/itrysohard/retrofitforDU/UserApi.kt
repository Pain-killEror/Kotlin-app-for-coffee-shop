package com.example.itrysohard.retrofitforDU

import com.example.itrysohard.jwt.LoginRequest
import com.example.itrysohard.jwt.LoginResponse
import com.example.itrysohard.jwt.RefreshRequest
import com.example.itrysohard.jwt.RefreshTokenResponse
import com.example.itrysohard.model.User
import com.example.itrysohard.model.answ.UserAnswDTO
import com.example.itrysohard.model.answ.UserAnswDTORolesNoRev
import com.example.itrysohard.model.info.UserInfoDTO
import com.example.itrysohard.model.info.UserInfoDTOO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApi {

    //kotkin serv

    @GET("/user/name/{name}")
    fun getUserByName(@Path("name") name: String): Call<UserInfoDTO>

    @GET("/user/name/part/{prefix}")
    fun getUserByPartOfName(@Path("prefix") prefix: String): Call<List<UserAnswDTORolesNoRev>>
    @GET("/user/allWRNRev")
    fun getAllUsersWithRoles(): Call<List<UserAnswDTORolesNoRev>>
    @GET("/user/all")
    @Headers("Requires-Role: ROLE_ADMIN")
    fun getAllUsers(): Call<List<UserAnswDTO>>



    @POST("/user/block-user/{id}")
    fun blockUser(@Path("id") userId: Long): Call<Void>

    @POST("/user/unblock/{id}")
    fun unblockUser(@Path("id") userId: Long): Call<Void>

    @DELETE("/user/remove-user/{id}")
    fun removeUser(@Path("id") userId: Long): Call<Void>


    @POST("user/auth")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("user/refresh")
    fun refreshToken(@Body request: RefreshRequest): Call<RefreshTokenResponse>

    /*@POST("api/users/save")
    fun save(@Body user: User): Call<User>*/
    @POST("user/create")
    fun registerUser(@Body user: User): Call<String>



}