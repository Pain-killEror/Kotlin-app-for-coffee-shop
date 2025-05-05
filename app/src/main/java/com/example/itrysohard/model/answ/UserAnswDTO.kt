package com.example.itrysohard.model.answ

import com.example.itrysohard.model.info.ReviewInfoDTO

data class UserAnswDTO(
    val id: Long,
    val name: String,
    val email: String,
    val reviews: List<ReviewInfoDTO>

)
