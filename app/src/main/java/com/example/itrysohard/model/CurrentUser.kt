package com.example.itrysohard.model

object CurrentUser {
    var user: User? = null
    var isAdmin: Boolean = false // Новое поле для хранения информации об администраторе
}