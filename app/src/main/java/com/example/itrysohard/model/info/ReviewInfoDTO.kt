package com.example.itrysohard.model.info

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class ReviewInfoDTO(
    val id: Long,
    val title: String,
    val reting: Byte,
    val description: String,
    @SerializedName("createdAt")
    val createdAt: String
)
