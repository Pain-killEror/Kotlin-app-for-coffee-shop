// Создайте новый Kotlin файл, например, AnalyticsData.kt
package com.example.itrysohard.model.answ // или другое подходящее место

import com.google.gson.annotations.SerializedName

data class AnalyticsResponseItem(
    @SerializedName("group")
    val group: String?, // Может быть null, если что-то пойдет не так или для "Overall Result"

    @SerializedName("count")
    val count: Long?,    // Long, так как на сервере Long

    @SerializedName("revenue")
    val revenue: Double?,

    @SerializedName("percentage")
    val percentage: Double?
)