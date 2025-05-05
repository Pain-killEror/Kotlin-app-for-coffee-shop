package com.example.itrysohard.model.answ

import com.example.itrysohard.model.info.UserInfoDTO
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReviewAnswDTO(
    val id: Long,
    val title: String,
    val rating: Byte,
    val description: String,
    val createdAt: String,
    val user: UserInfoDTO
) : Parcelable