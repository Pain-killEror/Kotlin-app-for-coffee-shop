package com.example.itrysohard

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.itrysohard.databinding.ActivityPersAccBinding

class PersAccActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersAccBinding
    private var userName: String? = null
    private var userEmail: String? = null
    private var isAdmin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersAccBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isAdmin = intent.getBooleanExtra("isAdmin", false)
        userName = intent.getStringExtra("userName")
        userEmail = intent.getStringExtra("userEmail")

        setupView(isAdmin, userName, userEmail)

        binding.btLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun setupView(isAdmin: Boolean, name: String?, email: String?) {
        if (isAdmin) {
            binding.PersName.text = "Администратор"
            binding.PersEmail.visibility = View.GONE // Скрываем электронную почту
        } else {
            binding.PersName.text = name ?: "Имя пользователя"
            binding.PersEmail.text = email ?: "Электронная почта"
            binding.PersEmail.visibility = View.VISIBLE
        }

        binding.avatar.setImageResource(R.drawable.avatar) // Убедитесь, что файл с именем "avatar" существует в drawable
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Подтверждение")
            .setMessage("Вы уверены, что хотите выйти из аккаунта?")
            .setPositiveButton("Да") { _, _ ->
                // Устанавливаем результат выхода и передаем значение переменной isUserLoggedIn
                val intent = Intent().apply {
                    putExtra("isUserLoggedIn", false) // Устанавливаем значение в false
                }
                setResult(RESULT_OK, intent)
                finish() // Закрываем активность
            }
            .setNegativeButton("Нет", null)
            .show()
    }
}