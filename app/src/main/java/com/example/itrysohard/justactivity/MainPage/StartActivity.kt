package com.example.itrysohard.justactivity.MainPage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.itrysohard.BackPress.ActivityHistoryImpl
import com.example.itrysohard.BackPress.BackPressManager
import com.example.itrysohard.MyApplication
import com.example.itrysohard.justactivity.RegistrationAuthentication.RegAuthActivity
import com.example.itrysohard.databinding.ActivityStartBinding
import com.example.itrysohard.justactivity.MainPage.PagesOnMain.ContactsActivity
import com.example.itrysohard.justactivity.MainPage.PagesOnMain.EventsActivity
import com.example.itrysohard.justactivity.MainPage.PagesOnMain.MyAchievementsActivity
import com.example.itrysohard.justactivity.PersonalPage.PersAccActivity
import com.example.itrysohard.justactivity.about_us.AboutUsActivity
import com.example.itrysohard.justactivity.menu.cart.CartActivity
import com.example.itrysohard.justactivity.menu.MenuActivity
import com.example.itrysohard.model.CurrentUser
import com.example.itrysohard.model.CurrentUser.isBlocked
import com.example.itrysohard.model.CurrentUser.user
import com.example.itrysohard.model.User

class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding
    private var isUserLoggedIn: Boolean = false
    private var userName: String? = null
    private var userEmail: String? = null
    private lateinit var myApplication: MyApplication
    private lateinit var loginActivityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var logoutActivityResultLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityHistoryImpl.addActivity(this::class.java)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)



        // Инициализация ActivityResultLauncher для получения результата из MainActivity (вход)
        /*loginActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                handleLoginResult(result.data)
            }
        }

        // Инициализация ActivityResultLauncher для получения результата из PersAccActivity (выход)
        logoutActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                handleLogoutResult(result.data)
            }
        }*/

        binding.btnAboutUs.setOnClickListener {
            startActivity(Intent(this, AboutUsActivity::class.java))
            finish()
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegAuthActivity::class.java))
        }

        binding.btnHome.setOnClickListener {
            Toast.makeText(this, "Вы на главном экране!", Toast.LENGTH_SHORT).show()
        }

        binding.btnMenu.setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }

        myApplication = application as MyApplication
        updateCartCountDisplay()

        binding.btnCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))

        }

        binding.btnPersAcc.setOnClickListener {
            showAuthorizationDialogPers()

        }



        binding.btnAchieve.setOnClickListener{
            startActivity(Intent(this, MyAchievementsActivity::class.java))
        }
        binding.btnMenu2.setOnClickListener{
            startActivity(Intent(this, MenuActivity::class.java))
        }
        binding.btnDiscounts.setOnClickListener{
            Toast.makeText(this, "В разработке!", Toast.LENGTH_SHORT).show()
        }
        binding.btnEvent.setOnClickListener{
            startActivity(Intent(this, EventsActivity::class.java))
        }
        binding.btnAboutUs.setOnClickListener{
            startActivity(Intent(this, AboutUsActivity::class.java))
        }
        binding.btnContacts.setOnClickListener{
            startActivity(Intent(this, ContactsActivity::class.java))
        }

        /*binding.btnOpenMap.setOnClickListener {
            openYandexMap()
        }*/
    }

    override fun onBackPressed() {
        BackPressManager.handleBackPress(this) {
            super.onBackPressed()
        }
    }

    private fun openYandexMap() {
        // Укажите название заведения или места
        val placeName = "ДжоДжо" // Замените на нужное название
        val uri = "https://yandex.ru/maps/?text=${Uri.encode(placeName)}"

        // Создаем Intent для открытия URL в браузере
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startActivity(intent)
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

    override fun onResume() {
        super.onResume()
        checkUserAuthorization()
        updateCartCountDisplay()

    }

    private fun checkUserAuthorization() {
        // Проверяем, есть ли текущий пользователь
        if (CurrentUser.user == null) {
            isUserLoggedIn = false
            setNotLoggedInUIVisibility(true) // Показываем элементы
        } else {
            isUserLoggedIn = true
            userName = CurrentUser.user?.name
            userEmail = CurrentUser.user?.email
            setNotLoggedInUIVisibility(false) // Скрываем элементы
        }
    }

    private fun setNotLoggedInUIVisibility(isVisible: Boolean) {
        val visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.tvTitleNotLoggedIn.visibility = visibility
        binding.tvTextNotLoggedIn.visibility = visibility
        binding.btnRegister.visibility = visibility
    }

    private fun handleLoginResult(data: Intent?) {
        userName = data?.getStringExtra("userName")
        userEmail = data?.getStringExtra("userEmail")

        // Обновляем текущего пользователя
        CurrentUser.user = User(userName ?: "", userEmail ?: "", "", isBlocked) // Пустой пароль

        // Устанавливаем статус администратора
        CurrentUser.isAdmin = data?.getBooleanExtra("isAdmin", false) ?: false

        isUserLoggedIn = true
        Toast.makeText(this, "Вы вошли как $userName", Toast.LENGTH_SHORT).show()
    }

    private fun handleLogoutResult(data: Intent?) {
        // Обработка выхода из аккаунта
        isUserLoggedIn = false
        userName = null
        userEmail = null
        CurrentUser.user = null // Сбрасываем текущего пользователя
        CurrentUser.isAdmin = false // Сбрасываем статус администратора

        Toast.makeText(this, "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show()
    }

    private fun updateCartCountDisplay() {
        binding.tvCartCount.text = myApplication.cartItemCount.toString()
    }

}