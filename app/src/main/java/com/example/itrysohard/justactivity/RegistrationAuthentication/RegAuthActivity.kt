package com.example.itrysohard.justactivity.RegistrationAuthentication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


import com.example.itrysohard.BackPress.ActivityHistoryImpl
import com.example.itrysohard.R
import com.example.itrysohard.databinding.ActivityRegauthBinding
import com.example.itrysohard.justactivity.MainPage.StartActivity
import com.example.itrysohard.model.User
import com.example.itrysohard.jwt.LoginRequest
import com.example.itrysohard.jwt.LoginResponse
import com.example.itrysohard.retrofitforDU.RetrofitService
import com.example.itrysohard.jwt.SecurityHelper
import com.example.itrysohard.jwt.SharedPrefTokenManager
import com.example.itrysohard.jwt.TokenManager
import com.example.itrysohard.retrofitforDU.UserApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RegAuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegauthBinding
    private lateinit var userApi: UserApi
    private var isSignUp = true // Переменная для отслеживания состояния (регистрация или вход)
    private lateinit var tokenManager: TokenManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegauthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tokenManager = SharedPrefTokenManager(this)
        ActivityHistoryImpl.addActivity(this::class.java)

        val retrofitService = RetrofitService(this, tokenManager)
        userApi = retrofitService.getRetrofit().create(UserApi::class.java)

        binding.btReg.setOnClickListener { handleAuth() }
        binding.tvLinkSingInOnReg.setOnClickListener { toggleSignInSignUp() }



        binding.showPasswordButton.setOnClickListener {
            if (binding.etPassReg.transformationMethod is PasswordTransformationMethod) {
                // Показать пароль
                binding.etPassReg.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.showPasswordButton.setImageResource(R.drawable.icon_edit)
            } else {
                // Скрыть пароль
                binding.etPassReg.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.showPasswordButton.setImageResource(R.drawable.icon_plus)
            }
            // Переместить курсор в конец текста
            binding.etPassReg.setSelection(binding.etPassReg.text.length)
        }

        binding.showPasswordButtonSec.setOnClickListener {
            if (binding.etPassRegSec.transformationMethod is PasswordTransformationMethod) {
                // Показать пароль
                binding.etPassRegSec.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.showPasswordButtonSec.setImageResource(R.drawable.icon_edit)
            } else {
                // Скрыть пароль
                binding.etPassRegSec.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.showPasswordButtonSec.setImageResource(R.drawable.icon_plus)
            }
            // Переместить курсор в конец текста
            binding.etPassRegSec.setSelection(binding.etPassRegSec.text.length)
        }

    }

    private fun handleAuth() {
        if (isSignUp) {
            registerUser()
        } else {
            loginUser()
        }
    }

    private fun registerUser() {
        val name = binding.etNameReg.text.toString().trim()
        val email = binding.edEmailReg.text.toString().trim()
        val password1 = binding.etPassReg.text.toString().trim()
        val password2 = binding.etPassRegSec.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password1.isEmpty() || password2.isEmpty()) {
            showToast("Пожалуйста, заполните все поля")
            return
        }

        if (!isEmailValid(email)) {
            showToast("Введите корректный адрес электронной почты")
            binding.edEmailReg.text.clear()
            return
        }

        if(password1 != password2){
            showToast("Пароли в двух полях должны совпадать")
            return
        }


        val newUser = User(name, email, password2)                           // !!!!!!!!!!!!!

        userApi.registerUser(newUser).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val message = response.body() // "Registration was successful"
                    clearFields()
                    showToast(message!!)
                    val lastActivity = ActivityHistoryImpl.getSecondToLastActivity()
                    if (lastActivity != null) {
                        startActivity(Intent(this@RegAuthActivity, lastActivity))
                        finish()
                    } else {
                        startActivity(Intent(this@RegAuthActivity, StartActivity::class.java))
                        finish()
                    }
                } else {
                    showToast("Ошибка: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                showToast("Ошибка сети: ${t.message}")
            }
        })


        //for kotlin server

        //for java server

        /*val newUser = User(name, email, password2)                            //!!!!!!!!!!!!
        userApi.create(newUser).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val savedUser = response.body() ?: return

                    // Проверяем ID: если это 1 или 2, назначаем администратором
                    CurrentUser.isAdmin = (savedUser.id == 1L || savedUser.id == 2L)

                    //showToast("Регистрация успешна")
                    CurrentUser.user = savedUser
                    clearFields()
                    showToast("Регистрация успешна")
                    val lastActivity = ActivityHistoryImpl.getSecondToLastActivity()
                    if (lastActivity != null) {
                        startActivity(Intent(this@RegAuthActivity, lastActivity))
                        finish()
                    } else {
                        startActivity(Intent(this@RegAuthActivity, StartActivity::class.java))
                        finish()
                    }
                } else {
                    showToast("Ошибка регистрации: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<User>, t: Throwable) {
                showToast("Ошибка подключения: ${t.message}")
            }
        })*/

        //for java server

    }





    // RegAuthActivity.kt
    private fun loginUser() {
        val name = binding.etNameReg.text.toString().trim()
        val password = binding.etPassReg.text.toString().trim()
        clearTokens()
        if (name.isEmpty() || password.isEmpty()) {
            showToast("Заполните все поля")
            return
        }

        userApi.login(LoginRequest(name, password)).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                when {
                    response.isSuccessful -> {
                        response.body()?.let { loginResponse ->
                            // 1. Проверка структуры токенов
                            if (isValidJwt(loginResponse.accessToken) && isValidJwt(loginResponse.refreshToken)) {


                                // 2. Сохранение через единый метод
                                tokenManager.saveTokens(
                                    loginResponse.accessToken,
                                    loginResponse.refreshToken
                                )

                                // 3. Переход после успешного сохранения
                                startActivity(Intent(this@RegAuthActivity, StartActivity::class.java))
                                finish()
                            } else {
                                showToast("Некорректный формат токенов")
                            }
                        } ?: showToast("Пустой ответ от сервера")
                    }
                    response.code() == 401 -> showToast("Неверный логин/пароль")
                    else -> showToast("Ошибка: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                showToast("Ошибка сети: ${t.message}")
            }
        })
    }

    // Унифицированный метод сохранения токенов
    private fun saveTokens(accessToken: String, refreshToken: String) {
        val prefs = getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)

        prefs.edit()
            .putString("ACCESS_TOKEN", accessToken)
            .putString("REFRESH_TOKEN", refreshToken)
            .apply()

        Log.d("TOKEN_SAVE", "Токены сохранены: $accessToken | $refreshToken")
    }

    fun clearTokens() {
        val prefs = getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        val accessToken = prefs.getString("ACCESS_TOKEN", null)
        // Удаляем токены по ключам, используемым при сохранении, например, "access_token" и "refresh_token"
        prefs.edit()
            .remove("ACCESS_TOKEN")
            .remove("REFRESH_TOKEN")
            .apply()

        Log.d("TokenDebug", "Токены успешно удалены. tokens: ${accessToken}")
    }

    // Добавьте этот метод в конец класса
    private fun isValidJwt(token: String): Boolean {
        return token.count { it == '.' } == 2 // JWT должен содержать 2 точки
    }
    private fun clearStoredTokens() {
        getSharedPreferences("secure_prefs", Context.MODE_PRIVATE).edit().clear().apply()
    }

    private fun saveTokens(accessToken: String, refreshToken: String, expiresIn: Long = 604800) {
        val prefs = getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        val refreshTokenExpiry = System.currentTimeMillis() + expiresIn * 1000

        // Проверка шифрования
        val encryptedAccess = SecurityHelper.encrypt(this, accessToken)
        val encryptedRefresh = SecurityHelper.encrypt(this, refreshToken)

        android.util.Log.d("AUTH_DEBUG", """
        Saving Tokens:
        - Access (encrypted): ${encryptedAccess?.take(5)}...
        - Refresh (encrypted): ${encryptedRefresh?.take(5)}...
    """.trimIndent())

        if (encryptedAccess == null || encryptedRefresh == null) {
            android.util.Log.e("AUTH", "Ошибка шифрования токенов")
            showToast("Ошибка безопасности. Попробуйте снова.")
            return
        }

        prefs.edit()
            .putString("access_token", encryptedAccess)
            .putString("refresh_token", encryptedRefresh)
            .putLong("refresh_token_expiry", refreshTokenExpiry)
            .apply()

        android.util.Log.d("AUTH", "Токены сохранены: access=${accessToken.take(5)}..., expiry=$refreshTokenExpiry")
    }

    private fun logoutUser() {
        val prefs = getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        startActivity(Intent(this, StartActivity::class.java))
        finish()
    }

    // Обновление токена
    private fun refreshToken() {
        // Получаем SharedPreferences
        val prefs = getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)

        // Проверяем наличие зашифрованного refreshToken
        val encryptedRefreshToken = prefs.getString("refresh_token", null) ?: run {
            logoutUser()
            return
        }

        // Расшифровываем refreshToken
        val refreshToken = SecurityHelper.decrypt(this, encryptedRefreshToken) ?: run {
            logoutUser()
            return
        }

        // Отправляем запрос на обновление токена

    }


    private fun toggleSignInSignUp() {
        isSignUp = !isSignUp
        updateUI()
        clearFields()
    }

    private fun updateUI() {
        if (isSignUp) {
            binding.tvReg.text = getString(R.string.tvReg)
            binding.tvLinkSingInOnReg.text = getString(R.string.tvLinkSingIn_onReg_change)
            binding.btReg.text = getString(R.string.btReg)
            binding.edEmailReg.visibility = View.VISIBLE
            binding.etPassRegSec.visibility = View.VISIBLE
            binding.showPasswordButtonSec.visibility = View.VISIBLE
        } else {
            binding.tvReg.text = getString(R.string.tvAuth)
            binding.tvLinkSingInOnReg.text = getString(R.string.tvLinkSingUp_onReg_change)
            binding.btReg.text = getString(R.string.btReg_changeSingIn)
            binding.edEmailReg.visibility = View.GONE
            binding.etPassRegSec.visibility = View.GONE
            binding.showPasswordButtonSec.visibility = View.GONE
        }
    }

    private fun clearFields() {
        binding.etNameReg.text.clear()
        binding.edEmailReg.text.clear()
        binding.etPassReg.text.clear()
    }

    private fun isEmailValid(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


}