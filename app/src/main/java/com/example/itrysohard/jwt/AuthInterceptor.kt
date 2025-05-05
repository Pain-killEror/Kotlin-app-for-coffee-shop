package com.example.itrysohard.jwt

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast

import com.example.itrysohard.justactivity.RegistrationAuthentication.RegAuthActivity
import com.example.itrysohard.retrofitforDU.RetrofitService
import okhttp3.Interceptor
import okhttp3.Response

// AuthInterceptor.kt
// AuthInterceptor.kt
@Suppress("UNREACHABLE_CODE")
class AuthInterceptor(
    private val context: Context,
    private val tokenManager: TokenManager


) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val request = chain.request()

        val prefs = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        val accessToken = prefs.getString("ACCESS_TOKEN", null)
        val refreshToken = prefs.getString("REFRESH_TOKEN", null)



        return if (accessToken != null) {
            chain.proceed(
                request.newBuilder()
                    .header("Authorization", "Bearer $accessToken")
                    .build()
            )
        } else {
            chain.proceed(request)
        }

        if (accessToken == null || JWTDecoder.isExpired(accessToken)) {
            // Если токен истек, перенаправляем на авторизацию
            redirectToLogin()
            return chain.proceed(request)
        }

        // Добавляем токен в заголовок
        return chain.proceed(
            request.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        )

        // Если токен невалиден, перенаправляем на авторизацию
        redirectToLogin()
        return chain.proceed(request)

        // Если access токен истек
        if (JWTDecoder.isExpired(accessToken)) {
            // Проверяем refresh токен
            if (refreshToken != null && !JWTDecoder.isExpired(refreshToken)) {
                // Пытаемся обновить токены
                val newTokens = refreshTokens(refreshToken)
                if (newTokens != null) {
                    tokenManager.saveTokens(newTokens.accessToken, newTokens.refreshToken)
                    // Повторяем запрос с новым токеном
                    return chain.proceed(
                        originalRequest.newBuilder()
                            .header("Authorization", "Bearer ${newTokens.accessToken}")
                            .build()
                    )
                } else {
                    // Оба токена недействительны
                    redirectToLogin()
                }
            } else {
                // Refresh токен истек
                redirectToLogin()
            }
        }

        // Добавляем текущий access токен в запрос
        return chain.proceed(
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        )
    }

    private fun refreshTokens(refreshToken: String): RefreshTokenResponse? {
        return try {
            // Создаем объект запроса
            val request = RefreshRequest(refreshToken = refreshToken)

            val response = RetrofitService(context, tokenManager)
                .getUserApi()
                .refreshToken(request) // Передаем объект вместо строки
                .execute()

            response.body()
        } catch (e: Exception) {
            null
        }
    }

    private fun redirectToLogin() {
        (context as Activity).runOnUiThread {
            tokenManager.clearTokens()
            val intent = Intent(context, RegAuthActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
            context.finish()
        }
    }
}