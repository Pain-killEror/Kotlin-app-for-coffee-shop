package com.example.itrysohard.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Review(
    @SerializedName("title")
    val title: String,
    @SerializedName("rating")
    val rating: Byte, // Важно использовать Byte вместо Float
    @SerializedName("description")
    val description: String
) : Serializable

