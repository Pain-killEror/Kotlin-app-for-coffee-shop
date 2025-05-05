// TokenManager.kt
package com.example.itrysohard.jwt

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.itrysohard.justactivity.RegistrationAuthentication.RegAuthActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

interface TokenManager {
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun saveTokens(access: String, refresh: String)
    fun clearTokens()
    fun isAccessExpired(): Boolean
    fun isRefreshExpired(): Boolean


}

class SharedPrefTokenManager(private val context: Context) : TokenManager {
    private val prefs = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)

    override fun getAccessToken(): String? {
        val accessToken = prefs.getString("ACCESS_TOKEN", null)
        return accessToken
    }


    override fun getRefreshToken(): String? {
        val refresh = prefs.getString("REFRESH_TOKEN", null)
        return refresh
    }

    override fun saveTokens(access: String, refresh: String) {
        // 1. Проверка входных данных
        if (access.isEmpty() || refresh.isEmpty()) {
            Log.e("TokenDebug", "Пустые токены! Access empty: ${access.isEmpty()}, Refresh empty: ${refresh.isEmpty()}")
            return
        }



        // 2. Логирование сырых токенов перед обработкой
        Log.d("TokenDebug", "Raw Access: ${access.take(20)}... (length: ${access.length})")
        Log.d("TokenDebug", "Raw Refresh: ${refresh.take(20)}... (length: ${refresh.length})")

            // 5. Синхронное сохранение с явной проверкой результата
        val editor = prefs.edit()
        editor.putString("ACCESS_TOKEN", access)
        editor.putString("REFRESH_TOKEN", refresh)
        val success = editor.commit() // commit() возвращает boolean результат
        

    }


    // Добавьте метод проверки
    private fun isValidJwt(token: String): Boolean {
        return token.split(".").size == 3
    }

    override fun clearTokens() {
        // Удаляем токены по ключам, используемым при сохранении, например, "access_token" и "refresh_token"
        prefs.edit()
            .remove("ACCESS_TOKEN")
            .remove("REFRESH_TOKEN")
            .apply()

    }

    override fun isAccessExpired(): Boolean {
        return JWTDecoder.isExpired(getAccessToken())
    }

    override fun isRefreshExpired(): Boolean {
        return JWTDecoder.isExpired(getRefreshToken())
    }
}