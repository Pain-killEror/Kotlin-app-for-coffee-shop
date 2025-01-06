package com.example.itrysohard.model

object CurrentUser {
    var user: User? = null
    var isAdmin: Boolean = false
    var isBlocked: Boolean
        get() = user?.isBlocked ?: false
        set(value) {
            user?.isBlocked = value
        }
}