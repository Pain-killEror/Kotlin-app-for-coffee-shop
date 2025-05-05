package com.example.itrysohard.jwt

import com.google.gson.annotations.SerializedName


// Добавьте аннотации @SerializedName для точного соответствия
data class LoginResponse(
     val accessToken: String,
     val refreshToken: String
)