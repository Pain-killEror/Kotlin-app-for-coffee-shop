package com.example.itrysohard.model

import java.io.Serializable

data class Review(
    var id: Long? = null,
    val username: String,
    val title: String,
    val rating: Float,
    val description: String,
    val createdAt: String?
) : Serializable