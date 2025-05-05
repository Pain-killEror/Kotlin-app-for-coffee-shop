package com.example.itrysohard.model.info

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserInfoDTO(
    val id: Long,
    val name: String,
    val email: String
) : Parcelable