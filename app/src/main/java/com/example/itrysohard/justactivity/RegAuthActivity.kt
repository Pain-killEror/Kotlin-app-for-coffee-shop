package com.example.itrysohard.justactivity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.itrysohard.R
import com.example.itrysohard.databinding.ActivityRegauthBinding
import com.example.itrysohard.model.CurrentUser
import com.example.itrysohard.model.User
import com.example.itrysohard.retrofitforDU.RetrofitService
import com.example.itrysohard.retrofitforDU.UserApi
import org.mindrot.jbcrypt.BCrypt
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RegAuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegauthBinding
    private lateinit var userApi: UserApi
    private var isSignUp = true // Переменная для отслеживания состояния (регистрация или вход)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegauthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val retrofitService = RetrofitService()
        userApi = retrofitService.getRetrofit().create(UserApi::class.java)

        binding.btReg.setOnClickListener { handleAuth() }
        binding.tvLinkSingInOnReg.setOnClickListener { toggleSignInSignUp() }
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
        val password = binding.etPassReg.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showToast("Пожалуйста, заполните все поля")
            return
        }

        if (!isEmailValid(email)) {
            showToast("Введите корректный адрес электронной почты")
            binding.edEmailReg.text.clear()
            return
        }

        // Проверка существования пользователя
        userApi.getAllUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {


                val users = response.body() ?: emptyList()

                // Проверяем, есть ли пользователь с таким именем или почтой
                if (users.any { it.name.equals(name, ignoreCase = true) }) {
                    showToast("Имя уже занято")
                    binding.etNameReg.text.clear()
                    return
                }
                if (users.any { it.email.equals(email, ignoreCase = true) }) {
                    showToast("Пользователь с такой почтой уже существует")
                    binding.edEmailReg.text.clear()
                    return
                }

                // Если имя и почта свободны, создаем нового пользователя
                val newUser = User(name, email, password)
                userApi.save(newUser).enqueue(object : Callback<User> {
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        if (response.isSuccessful) {
                            val savedUser = response.body() ?: return

                            // Проверяем ID: если это 1 или 2, назначаем администратором
                            CurrentUser.isAdmin = (savedUser.id == 1L || savedUser.id == 2L)

                            //showToast("Регистрация успешна")
                            CurrentUser.user = savedUser
                            clearFields()
                            showToast("Регистрация успешна")
                            val resultIntent = Intent().apply {
                                putExtra("userName", savedUser.name)
                                putExtra("userEmail", savedUser.email)
                                putExtra("isAdmin", CurrentUser.isAdmin)
                            }
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        } else {
                            showToast("Ошибка регистрации: ${response.message()}")
                        }
                    }

                    override fun onFailure(call: Call<User>, t: Throwable) {
                        showToast("Ошибка подключения: ${t.message}")
                    }
                })
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                showToast("Ошибка подключения: ${t.message}")
            }
        })
    }

    private fun loginUser() {
        val name = binding.etNameReg.text.toString().trim()
        val password = binding.etPassReg.text.toString().trim()

        if (name.isEmpty() || password.isEmpty()) {
            showToast("Пожалуйста, заполните все поля")
            return
        }

        userApi.getAllUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (!response.isSuccessful) {
                    showToast("Ошибка авторизации: ${response.message()}")
                    return
                }

                val users = response.body() ?: emptyList()
                val validUser = users.find { it.name.equals(name, ignoreCase = true) }

                // Проверяем, существует ли пользователь и соответствует ли пароль
                if (validUser != null && BCrypt.checkpw(password, validUser.password)) {
                    showToast("Вход успешен")

                    // Устанавливаем текущего пользователя и проверяем, является ли он администратором
                    CurrentUser.user = validUser
                    CurrentUser.isAdmin = (validUser.id == 1L || validUser.id == 2L)

                    clearFields()

                    val resultIntent = Intent().apply {
                        putExtra("userName", validUser.name)
                        putExtra("userEmail", validUser.email)
                        putExtra("isAdmin", CurrentUser.isAdmin) // Передаем информацию о том, является ли пользователь администратором
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                } else {
                    showToast("Неверный логин или пароль")
                    binding.etPassReg.text.clear()
                    binding.etNameReg.text.clear()
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                showToast("Ошибка подключения: ${t.message}")
            }
        })
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
        } else {
            binding.tvReg.text = getString(R.string.tvAuth)
            binding.tvLinkSingInOnReg.text = getString(R.string.tvLinkSingUp_onReg_change)
            binding.btReg.text = getString(R.string.btReg_changeSingIn)
            binding.edEmailReg.visibility = View.GONE
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