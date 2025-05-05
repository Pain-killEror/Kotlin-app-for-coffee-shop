package com.example.itrysohard.jwt

data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String
)