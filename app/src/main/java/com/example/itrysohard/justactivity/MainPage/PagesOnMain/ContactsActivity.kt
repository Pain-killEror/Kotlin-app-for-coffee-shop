package com.example.itrysohard.justactivity.MainPage.PagesOnMain

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.itrysohard.BackPress.ActivityHistoryImpl
import com.example.itrysohard.BackPress.BackPressManager
import com.example.itrysohard.R
import com.example.itrysohard.databinding.ActivityContactsBinding
import com.example.itrysohard.justactivity.MainPage.StartActivity
import com.example.itrysohard.justactivity.PersonalPage.PersAccActivity
import com.example.itrysohard.justactivity.RegistrationAuthentication.RegAuthActivity
import com.example.itrysohard.justactivity.helpfull.CartCount
import com.example.itrysohard.justactivity.menu.MenuActivity
import com.example.itrysohard.justactivity.menu.cart.CartActivity
import com.example.itrysohard.justactivity.helpfull.CurrentUser
import com.example.itrysohard.shadow.wrapInCustomShadowWithOffset


class ContactsActivity : CartCount() {
    private lateinit var binding: ActivityContactsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityHistoryImpl.addActivity(this::class.java)
        binding = ActivityContactsBinding.inflate(layoutInflater)

        setContentView(binding.main)

        updateCartCountDisplay(binding.tvCartCount)


       /* val shadow1 = findViewById<FrameLayout>(R.id.wrapper)
        wrapInCustomShadowWithOffset(shadow1, R.color.gray, resources, 40)*/

       /* val shadow2 = findViewById<View>(R.id.circle)
        wrapInCustomShadowWithOffset(shadow2, R.color.gray, resources, 50)*/

       /* val shadow4 = findViewById<FrameLayout>(R.id.wrapper2)
        wrapInCustomShadowWithOffset(shadow4, R.color.gray, resources, 30)*/


        binding.btnMap.setOnClickListener{
            val latitude = 53.931360 // Широта
            val longitude = 27.508452 // Долгота
            val locationName = "ДжоДжо"

            val uri = "yandexmaps://maps.yandex.ru/?pt=${longitude},${latitude}&z=15&text=${Uri.encode(locationName)}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                val fallbackUri = "https://yandex.ru/maps/?pt=${longitude},${latitude}&z=15&text=${Uri.encode(locationName)}"
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUri))
                startActivity(fallbackIntent)
            }
        }

        binding.btnInst.setOnClickListener{
            val username = "jojo_cafe_minsk" // Замените на имя аккаунта Instagram

            // Пытаемся открыть Instagram через приложение
            val uri = Uri.parse("instagram://user?username=$username")
            val intent = Intent(Intent.ACTION_VIEW, uri)

            // Проверяем, установлено ли приложение Instagram
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                // Если Instagram не установлен, открываем веб-версию
                val webUri = Uri.parse("https://www.instagram.com/$username/")
                val webIntent = Intent(Intent.ACTION_VIEW, webUri)
                startActivity(webIntent)
            }
        }


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