package com.example.itrysohard

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.itrysohard.databinding.ActivityStartBinding

class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding
    private var isUserLoggedIn = false
    private var userName: String? = null
    private var userEmail: String? = null
    private var isAdmin: Boolean = false

    private lateinit var loginActivityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var logoutActivityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация ActivityResultLauncher для получения результата из MainActivity (вход)
        loginActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                handleLoginResult(result.data)
            }
        }

        // Инициализация ActivityResultLauncher для получения результата из PersAccActivity (выход)
        logoutActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                handleLogoutResult(result.data)
            }
        }

        updateButtonStates() // Инициализация состояния кнопок

        binding.btSingInUp.setOnClickListener {
            val intent = Intent(this, RegAuthActivity::class.java)
            loginActivityResultLauncher.launch(intent)
        }

        binding.btMenu.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java).apply {
                putExtra("isAdmin", isAdmin) // Передаем информацию о том, является ли пользователь администратором
                putExtra("isUserLoggedIn", isUserLoggedIn)  // Здесь можно передать логин пользователя
            }
            startActivity(intent)
        }

        binding.btAboutUs.setOnClickListener {
            Toast.makeText(this, "Информация о нас", Toast.LENGTH_SHORT).show()
        }

        binding.btPersAcc.setOnClickListener {
            navigateToPersonalAccount()
        }
    }

    private fun handleLoginResult(data: Intent?) {
        //isUserLoggedIn = data?.getBooleanExtra("isUserLoggedIn", false) ?: false
        isUserLoggedIn = true
        userName = data?.getStringExtra("userName")
        userEmail = data?.getStringExtra("userEmail")
        isAdmin = data?.getBooleanExtra("isAdmin", false) ?: false
        // Устанавливаем isUserLoggedIn в true при успешном входе
        Toast.makeText(this, "Вы вошли как $userName", Toast.LENGTH_SHORT).show() // Отладка
        updateButtonStates() // Обновляем UI
    }

    private fun handleLogoutResult(data: Intent?) {
        // Обработка выхода из аккаунта
        isUserLoggedIn = false // Устанавливаем isUserLoggedIn в false
        userName = null // Обнуляем имя пользователя
        userEmail = null // Обнуляем электронную почту
        isAdmin = false // Обнуляем статус администратора
        Toast.makeText(this, "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show() // Отладка
        updateButtonStates() // Обновляем UI
    }

    private fun updateButtonStates() {
        // Обновляем видимость кнопок в зависимости от статуса пользователя
        binding.btPersAcc.visibility = if (isUserLoggedIn) View.VISIBLE else View.GONE
        binding.btSingInUp.visibility = if (isUserLoggedIn) View.GONE else View.VISIBLE
    }

    private fun navigateToPersonalAccount() {
        val intent = Intent(this, PersAccActivity::class.java).apply {
            putExtra("userName", userName)
            putExtra("userEmail", userEmail)
            putExtra("isAdmin", isAdmin)
            putExtra("isUserLoggedIn", isUserLoggedIn)
        }
        logoutActivityResultLauncher.launch(intent) // Используем launch для получения результата выхода
    }





}