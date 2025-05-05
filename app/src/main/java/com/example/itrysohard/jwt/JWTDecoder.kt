package com.example.itrysohard.jwt

import android.util.Base64
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

object JWTDecoder {

    // Проверка истек ли токен
    fun isExpired(token: String?): Boolean {
        if (token.isNullOrEmpty()) return true
        return try {
            val exp = getClaim(token, "exp")?.toLong() ?: return true
            // Умножаем на 1000, чтобы привести секунды к миллисекундам
            exp * 1000 < System.currentTimeMillis()
        } catch (e: Exception) {
            true
        }
    }


    fun getName(token: String?): String {
        return getClaim(token, "sub") ?: throw IllegalArgumentException("Invalid token")
    }

    // Получение роли пользователя
    fun getRole(token: String?): String? {
        if (token.isNullOrEmpty()) return null
        return try {
            // Удаляем лишние пробелы и переносы строк
            val cleanedToken = token.replace("\n", "").replace(" ", "")
            // Проверяем, что токен состоит из 3-х частей
            val parts = cleanedToken.split(".")
            if (parts.size != 3) {
                Log.e("JWTError", "Invalid JWT format: expected 3 parts")
                return null
            }

            // Декодируем payload (с использованием URL_SAFE флагов)
            val payloadBytes = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING)
            val payload = String(payloadBytes, Charsets.UTF_8)
            Log.d("JWTDebug", "Decoded payload: $payload")

            // Парсим JSON payload
            val json = JSONObject(payload)
            val rolesValue = json.opt("roles")

            // Если roles представляет собой массив, возвращаем первый элемент
            if (rolesValue is JSONArray && rolesValue.length() > 0) {
                rolesValue.getString(0)
            } else if (rolesValue is String) {
                rolesValue
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("JWTError", "Decoding failed: ${e.message}")
            null
        }
    }


    // Общий метод для извлечения данных из токена
    private fun getClaim(token: String?, claim: String): String? {
        if (token.isNullOrEmpty()) return null
        return try {
            // 1. Удаляем все переносы строк и пробелы
            val cleanedToken = token
                .replace("\n", "")
                .replace(" ", "")

            // 2. Проверяем структуру JWT
            val parts = cleanedToken.split(".")
            if (parts.size != 3) {
                Log.e("JWTError", "Invalid JWT format: expected 3 parts")
                return null
            }

            // 3. Декодируем payload с учетом URL-safe кодирования
            val payloadBytes = Base64.decode(
                parts[1],
                Base64.URL_SAFE or Base64.NO_PADDING
            )
            val payload = String(payloadBytes, Charsets.UTF_8)

            // 4. Логирование для отладки
            Log.d("JWTDebug", "Cleaned token: $cleanedToken")
            Log.d("JWTDebug", "Decoded payload: $payload")

            // 5. Извлекаем claim
            JSONObject(payload).optString(claim, null)
        } catch (e: Exception) {
            Log.e("JWTError", "Decoding failed: ${e.message}")
            null
        }
    }
}