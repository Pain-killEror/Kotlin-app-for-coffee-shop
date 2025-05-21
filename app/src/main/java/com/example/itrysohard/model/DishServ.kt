package com.example.itrysohard.model

import android.os.Parcel
import android.os.Parcelable

data class DishServ(
    var id: Int? = null, // добавляем id
    var name: String,
    var description: String,
    var price: Byte,
    var volume: String,
    var photo: String?,
    var category: String,
    var discount: Byte

) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readValue(Int::class.java.classLoader) as? Int, // считываем id
        name = parcel.readString() ?: "",
        description = parcel.readString() ?: "", // считываем description
        price = parcel.readByte(),
        volume = parcel.readString() ?: "",
        photo = parcel.readString(),
        category = parcel.readString() ?: "", // считываем category
        discount = parcel.readByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id) // записываем id
        parcel.writeString(name)
        parcel.writeString(description) // записываем description
        parcel.writeByte(price)
        parcel.writeString(volume)
        parcel.writeString(photo)
        parcel.writeString(category) // записываем category
        parcel.writeByte(discount)
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
        return "DishServ(id=$id, name='$name', price=$price,volume=$volume , photo=$photo, category='$category'), discount=$discount"
    }
}