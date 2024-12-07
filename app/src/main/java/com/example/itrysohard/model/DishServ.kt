package com.example.itrysohard.model

import android.os.Parcel
import android.os.Parcelable

data class DishServ(
    var id: Int? = null, // добавляем id
    var name: String,
    var description: String,
    var price: Double,
    var imageUrl: String?,
    var category: String

) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readValue(Int::class.java.classLoader) as? Int, // считываем id
        name = parcel.readString() ?: "",
        description = parcel.readString() ?: "", // считываем description
        price = parcel.readDouble(),
        imageUrl = parcel.readString(),
        category = parcel.readString() ?: "" // считываем category
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id) // записываем id
        parcel.writeString(name)
        parcel.writeString(description) // записываем description
        parcel.writeDouble(price)
        parcel.writeString(imageUrl)
        parcel.writeString(category) // записываем category
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DishServ> {
        override fun createFromParcel(parcel: Parcel): DishServ {
            return DishServ(parcel)
        }

        override fun newArray(size: Int): Array<DishServ?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        return "DishServ(id=$id, name='$name', price=$price, imageUrl=$imageUrl, category='$category')"
    }
}