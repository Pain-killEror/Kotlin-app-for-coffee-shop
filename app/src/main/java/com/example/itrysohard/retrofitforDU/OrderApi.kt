package com.example.itrysohard.retrofitforDU

import com.example.itrysohard.model.Order
import com.example.itrysohard.model.answ.AnalyticsResponseItem
import com.example.itrysohard.model.answ.UserStatisticsAnswDTO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface OrderApi {
    @POST("orders/create")
    fun createOrder(
        @Header("Authorization") token: String,
        @Body request: Order
    ): Call<Void>

    @GET("orders/statistics/{userId}")
    fun getUserStatistics(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long
    ): Call<UserStatisticsAnswDTO>

    @GET("orders/analytics")
    fun getOrderAnalytics(
        @Header("Authorization") token: String, // Если аналитика требует авторизации
        @Query("year") year: Int?,
        @Query("month") month: String?, // На сервере принимается String, потом парсится
        @Query("categories") categories: List<String>?,
        @Query("dishes") dishes: List<String>?,
        @Query("metrics") metrics: List<String>, // Метрики обязательны
        @Query("groupBy") groupBy: String?,
        @Query("quarter") quarter: Int?,
        @Query("startDate") startDate: String?, // Даты передаем как строки "YYYY-MM-DD"
        @Query("endDate") endDate: String?,
        @Query("season") season: String?
        // Добавьте @Query("week") week: Int?, если будете фильтровать по конкретной неделе,
        // но обычно неделя - это результат группировки, а не входной фильтр.
    ): Call<List<AnalyticsResponseItem>> // Ожидаем список наших элементов
}