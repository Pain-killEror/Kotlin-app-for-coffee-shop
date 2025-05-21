package com.example.itrysohard.model.answ

data class UserStatisticsAnswDTO(
    val userId: Long,
    val ordersThisMonth: Int,
    val ordersThisYear: Int,
    val totalOrders: Int,
    val averageOrderAmount: Double,
    val topDishes: List<String>,
    val totalSpent: Double,
    val totalSaved: Double
)