package com.example.itrysohard.justactivity.MainPage.PagesOnMain

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.itrysohard.BackPress.ActivityHistoryImpl
import com.example.itrysohard.BackPress.BackPressManager
import com.example.itrysohard.databinding.ActivityMyAchievementsBinding
import com.example.itrysohard.justactivity.MainPage.StartActivity
import com.example.itrysohard.justactivity.PersonalPage.PersAccActivity
import com.example.itrysohard.justactivity.RegistrationAuthentication.RegAuthActivity
import com.example.itrysohard.justactivity.menu.MenuActivity
import com.example.itrysohard.justactivity.menu.cart.CartActivity
import com.example.itrysohard.justactivity.helpfull.CurrentUser

class MyAchievementsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyAchievementsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityHistoryImpl.addActivity(this::class.java)
        binding = ActivityMyAchievementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnHome.setOnClickListener {
            startActivity(Intent(this, StartActivity::class.java))
            finish()
        }

        binding.btnMenu.setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }

        binding.btnCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))

        }

        binding.btnPersAcc.setOnClickListener {
            showAuthorizationDialogPers()
        }

    }

    override fun onBackPressed() {
        BackPressManager.handleBackPress(this) {
            super.onBackPressed()
        }
    }

    private fun showAuthorizationDialogPers() {
        if (CurrentUser.user == null) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Необходимо авторизоваться")
            builder.setMessage("Вы не авторизованы. Пожалуйста, авторизуйтесь чтобы получить доступ к личному кабинету.")

            builder.setPositiveButton("Аторизоваться") { _, _ ->
                // Перенаправление на LeaveReviewActivity
                val intent = Intent(this, RegAuthActivity::class.java)
                startActivity(intent)
            }

            builder.setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss() // Закрываем диалог
            }

            val dialog = builder.create()
            dialog.show()
        }
        else startActivity(Intent(this, PersAccActivity::class.java))
    }
}