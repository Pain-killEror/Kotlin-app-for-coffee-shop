package com.example.itrysohard.jwt

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import com.example.itrysohard.MyApplication
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object SecurityHelper {
    private const val KEY_ALIAS = "secure_key_alias"
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val AES_MODE = "AES/GCM/NoPadding"







    private fun getSecretKey(context: Context): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER).apply {
                init(
                    KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .build()
                )
                generateKey()
            }
        }
        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }

    fun encrypt(context: Context, data: String): String {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(context))
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(data.toByteArray())
        val combined = encryptedBytes + iv
        // Используем Base64.NO_WRAP, чтобы убрать переносы строк
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    fun decrypt(context: Context, encryptedData: String): String? {
        return try {
            val decodedData = Base64.decode(encryptedData, Base64.DEFAULT)
            if (decodedData.size <= 12) {
                Log.e("SECURITY", "Неверный формат данных: размер меньше или равен 12")
                return null
            }
            val iv = decodedData.takeLast(12).toByteArray()
            val cipher = Cipher.getInstance(AES_MODE)
            cipher.init(
                Cipher.DECRYPT_MODE,
                getSecretKey(context),
                GCMParameterSpec(128, iv)
            )
            val cipherText = decodedData.dropLast(12).toByteArray()
            String(cipher.doFinal(cipherText))
        } catch (e: Exception) {
            Log.e("SECURITY", "Ошибка дешифрования: ${e.message}", e)
            null
        }
    }


}
