package com.example.myappforcafee.model

class DishServ(
    var name: String,
    var description: String,
    var price: Double,
    var photoPath: String?
) {
    var id: Int? = null
    override fun toString(): String {
        return "DishServ(id=$id, name='$name', price=$price, imageurl=$photoPath)"
    }
}

