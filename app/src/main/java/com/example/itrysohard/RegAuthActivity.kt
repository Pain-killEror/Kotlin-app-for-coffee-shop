package com.example.itrysohard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.itrysohard.databinding.ActivityRegauthBinding
import com.example.myappforcafee.model.User
import com.example.myappforcafee.retrofit.RetrofitService
import com.example.myappforcafee.retrofit.UserApi
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
            return
        }

        checkUserExistsByName(name) { existsByName ->
            if (existsByName) {
                showToast("Имя уже занято")
                return@checkUserExistsByName
            }

            checkUserExistsByEmail(email) { existsByEmail ->
                if (existsByEmail) {
                    showToast("Пользователь с такой почтой уже существует")
                    return@checkUserExistsByEmail
                }

                val newUser = User(name, email, password)
                userApi.save(newUser).enqueue(object : Callback<User> {
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        if (response.isSuccessful) {
                            showToast("Регистрация успешна")
                            clearFields()

                            // Отправляем результат в StartActivity
                            val resultIntent = Intent().apply {
                                //putExtra("isUserLoggedIn", true)
                                putExtra("userName", name)
                                putExtra("userEmail", email)
                                putExtra("isAdmin", false) // Здесь можно указать администраторский статус
                            }
                            setResult(RESULT_OK, resultIntent)
                            finish() // Завершаем активность
                        } else {
                            showToast("Ошибка регистрации: ${response.message()}")
                        }
                    }

                    override fun onFailure(call: Call<User>, t: Throwable) {
                        showToast("Ошибка подключения: ${t.message}")
                    }
                })
            }
        }
    }

    private fun loginUser() {
        val name = binding.etNameReg.text.toString().trim()
        val password = binding.etPassReg.text.toString().trim()

        if (name.isEmpty() || password.isEmpty()) {
            showToast("Пожалуйста, заполните все поля")
            return
        }

        if (name.equals("admin", ignoreCase = true) && password == "12345") {
            showToast("Вход успешен как администратор")
            clearFields()

            val resultIntent = Intent().apply {
                //putExtra("isUserLoggedIn", true)
                putExtra("userName", name)
                //putExtra("userEmail", null) // Email не нужен для администраторов
                putExtra("isAdmin", true)
            }
            setResult(RESULT_OK, resultIntent)

            // Переход в StartActivity
            finish() // Завершаем активность
            return

        }


        userApi.getAllUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    val users = response.body() ?: emptyList()
                    val validUser = users.find {
                        it.name.equals(name, ignoreCase = true) && it.password == password
                    }
                    if (validUser != null) {
                        showToast("Вход успешен")

                        clearFields()

                        val resultIntent = Intent().apply {
                            //putExtra("isUserLoggedIn", true)
                            putExtra("userName", validUser.name)
                            putExtra("userEmail", validUser.email)
                            putExtra("isAdmin", false) // Указываем, что это не администратор
                        }
                        setResult(RESULT_OK, resultIntent)

                        // Переход в StartActivity

                        finish() // Завершаем текущую активностьии
                    } else {
                        showToast("Неверный логин или пароль")
                        binding.etPassReg.text.clear()
                    }
                } else {
                    showToast("Ошибка авторизации: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                showToast("Ошибка подключения: ${t.message}")
            }
        })
    }

    private fun navigateToStartActivity(name: String, email: String?, isAdmin: Boolean) {
        val intent = Intent().apply {
            putExtra("userName", name)
            putExtra("userEmail", email)
            putExtra("isAdmin", isAdmin) // Передаем isAdmin
        }
        setResult(RESULT_OK, intent)
        finish() // Закрытие MainActivity и возвращение в StartActivity
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

    private fun checkUserExistsByName(name: String, callback: (Boolean) -> Unit) {
        userApi.getAllUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    val users = response.body() ?: emptyList()
                    val exists = users.any { it.name.equals(name, ignoreCase = true) }
                    callback(exists)
                } else {
                    Log.e("APIError", "Error checking user by name: ${response.message()}")
                    callback(false)
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.e("NetworkError", "Error checking user by name: ${t.message}")
                callback(false)
            }
        })
    }

    private fun checkUserExistsByEmail(email: String, callback: (Boolean) -> Unit) {
        userApi.getAllUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    val users = response.body() ?: emptyList()
                    val exists = users.any { it.email.equals(email, ignoreCase = true) }
                    callback(exists)
                } else {
                    Log.e("APIError", "Error checking user by email: ${response.message()}")
                    callback(false)
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.e("NetworkError", "Error checking user by email: ${t.message}")
                callback(false)
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
