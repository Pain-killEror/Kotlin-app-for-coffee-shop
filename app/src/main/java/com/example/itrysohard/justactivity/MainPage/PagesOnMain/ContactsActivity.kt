package com.example.itrysohard.justactivity.MainPage.PagesOnMain

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.itrysohard.BackPress.ActivityHistoryImpl
import com.example.itrysohard.BackPress.BackPressManager
import com.example.itrysohard.R
import com.example.itrysohard.databinding.ActivityContactsBinding
import com.example.itrysohard.databinding.ActivityMyAchievementsBinding
import com.example.itrysohard.databinding.ActivityReviewDetailBinding
import com.example.itrysohard.justactivity.MainPage.StartActivity
import com.example.itrysohard.justactivity.PersonalPage.PersAccActivity
import com.example.itrysohard.justactivity.RegistrationAuthentication.RegAuthActivity
import com.example.itrysohard.justactivity.menu.MenuActivity
import com.example.itrysohard.justactivity.menu.cart.CartActivity
import com.example.itrysohard.model.CurrentUser
import com.example.itrysohard.shadow.wrapInCustomShadow
import com.example.itrysohard.shadow.wrapInCustomShadowWithOffset


class ContactsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContactsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityHistoryImpl.addActivity(this::class.java)
        binding = ActivityContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val wrapper = findViewById<FrameLayout>(R.id.wrapper)
        wrapInCustomShadowWithOffset(wrapper, R.color.black, resources)


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