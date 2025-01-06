package com.example.itrysohard.model

class User(var name: String,
           var email: String,
           var password: String,
           var isBlocked: Boolean) {
    var id: Long? = null
}