package com.example.myappforcafee.model

data class DishServ(
    var name: String,
    var description: String,
    var price: Double,
    var imageUrl: String?,
    var category: String) {
    var id: Int? = null
    override fun toString(): String {
        return "DishServ(id=$id, name='$name', price=$price, imageurl=$imageUrl, category=$category)"
    }
}

