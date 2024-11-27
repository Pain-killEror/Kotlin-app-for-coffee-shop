package com.example.itrysohard

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.itrysohard.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isUserLoggedIn = intent.getBooleanExtra("isUserLoggedIn", false)
        val isAdmin = intent.getBooleanExtra("isAdmin", false)

        // Обработчики для кнопок
        binding.btnBreakfast.setOnClickListener {
            navigateToFoodActivity("Завтраки", isUserLoggedIn, isAdmin )
        }

        binding.btnDesserts.setOnClickListener {
            navigateToFoodActivity("Десерты",isUserLoggedIn, isAdmin)
        }

        binding.btnDrinks.setOnClickListener {
            navigateToFoodActivity("Напитки",isUserLoggedIn, isAdmin)
        }
    }

    private fun navigateToFoodActivity(category: String, isUserLoggedIn: Boolean,isAdmin: Boolean) {
        val intent = Intent(this, FoodActivity::class.java)
        intent.putExtra("CATEGORY", category)
        intent.putExtra("isAdmin", isAdmin) // Передаем информацию о том, является ли пользователь администратором
        intent.putExtra("isUserLoggedIn", isUserLoggedIn)  // Здесь можно передать логин пользователя

        startActivity(intent)
    }
}
