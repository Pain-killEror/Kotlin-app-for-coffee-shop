package com.example.itrysohard.justactivity.MainPage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
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
import com.example.itrysohard.jwt.JWTDecoder
import com.example.itrysohard.jwt.RefreshTokenResponse
import com.example.itrysohard.justactivity.helpfull.CurrentUser
import com.example.itrysohard.model.User
import com.example.itrysohard.jwt.SharedPrefTokenManager

import android.util.Base64
import android.util.Log
import com.example.itrysohard.justactivity.helpfull.CartCount
import com.example.itrysohard.jwt.RefreshRequest
import com.example.itrysohard.retrofitforDU.UserApi
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class StartActivity : CartCount(){

    private lateinit var binding: ActivityStartBinding
    private var isUserLoggedIn: Boolean = false
    private var userName: String? = null
    private var userEmail: String? = null

    private lateinit var loginActivityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var logoutActivityResultLauncher: ActivityResultLauncher<Intent>
    private val tokenManager by lazy { SharedPrefTokenManager(this) }
    private val userApi: UserApi by lazy {
        Retrofit.Builder()
            .baseUrl("http://192.168.0.154:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserApi::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //checkTokens()
        ActivityHistoryImpl.addActivity(this::class.java)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateCartCountDisplay(binding.tvCartCount)

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

    /*private fun checkTokens() {
        val prefs = getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        val encryptedRefreshToken = prefs.getString("refresh_token", null)
        val refreshTokenExpiry = prefs.getLong("refresh_token_expiry", 0L)

        // Расшифровка для проверки
        val refreshToken = encryptedRefreshToken?.let {
            SecurityHelper.decrypt(this, it)
        }
        val isRefreshTokenValid = !refreshToken.isNullOrEmpty() &&
                System.currentTimeMillis() < refreshTokenExpiry

        android.util.Log.d("AUTH", """
        Проверка токенов:
        - Refresh Token: ${refreshToken?.take(5)}...
        - Expiry: $refreshTokenExpiry
        - Current Time: ${System.currentTimeMillis()}
        - Valid: $isRefreshTokenValid
    """.trimIndent())

        if (!isRefreshTokenValid) {
            prefs.edit().clear().apply()
            showLoginDialog()
        }
    }*/

    override fun onBackPressed() {
        BackPressManager.handleBackPress(this) {
            super.onBackPressed()
        }
    }


    private fun showAuthorizationDialogPers() {
        val tokenManager = SharedPrefTokenManager(this)
        val accessToken = tokenManager.getAccessToken()

        val isAuthorized = accessToken != null &&
                !JWTDecoder.isExpired(accessToken) &&
                JWTDecoder.getRole(accessToken) != null


        if (!isAuthorized) {
            AlertDialog.Builder(this).apply {
                setTitle("Доступ ограничен")
                setMessage("Для входа в личный кабинет требуется авторизация")
                setPositiveButton("Войти") { _, _ ->
                    startActivity(Intent(context, RegAuthActivity::class.java))
                }
                setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
            }.create().show()
        } else {
            startActivity(Intent(this, PersAccActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isFinishing) { // Проверка, что активити не в процессе завершения
            checkAuthState()
        }
        updateCartCountDisplay(binding.tvCartCount)
    }


    private fun checkAuthState() {
        // Создаем экземпляр TokenManager
        val tokenManager = SharedPrefTokenManager(this)

        // Получаем токены и проверяем, существуют ли они
        val accessToken = tokenManager.getAccessToken()
        val refreshToken = tokenManager.getRefreshToken()
        val accessExists = accessToken != null
        val refreshExists = refreshToken != null

        Log.d("AUTH_CHECK", "AccessToken exists: $accessExists")
        Log.d("AUTH_CHECK", "RefreshToken exists: $refreshExists")

        // Проверяем, не истек ли срок действия токенов
        if (accessExists && !tokenManager.isAccessExpired()) {
            Log.d("AUTH_CHECK", "Access token is valid. Пользователь авторизован.")
            setNotLoggedInUIVisibility(false)
        } else if (refreshExists && !tokenManager.isRefreshExpired()) {
            Log.d("AUTH_CHECK", "Access token истек, но refresh token действителен. Возможно, нужно обновить access token.")
            refreshAccessToken()
            setNotLoggedInUIVisibility(false)
        } else {
            Log.d("AUTH_CHECK", "Нет действительных токенов. Пользователь не авторизован.")
            setNotLoggedInUIVisibility(true)
        }
    }






    private fun refreshAccessToken() {
        val refreshToken = tokenManager.getRefreshToken()
        Log.d("REFRESH", "Полученный refresh token: $refreshToken")

        if (refreshToken.isNullOrEmpty()) {
            Log.e("REFRESH", "Refresh token is null или пустой, запрос не отправляется!")
            // Здесь можно, например, перенаправить пользователя на аутентификацию
            return
        }




        userApi.refreshToken(RefreshRequest(refreshToken)).enqueue(object : Callback<RefreshTokenResponse> {
            override fun onResponse(
                call: Call<RefreshTokenResponse>,
                response: Response<RefreshTokenResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { tokens ->
                        tokenManager.saveTokens(tokens.accessToken, tokens.refreshToken)
                        Log.d("REFRESH", "Токены успешно обновлены.")
                    }
                } else {
                    Log.e("REFRESH", "Ответ не успешен, код: ${response.code()}")
                    getSharedPreferences("auth", Context.MODE_PRIVATE).edit().clear().apply()
                    startActivity(Intent(this@StartActivity, RegAuthActivity::class.java))
                    finish()
                }
            }

            override fun onFailure(call: Call<RefreshTokenResponse>, t: Throwable) {
                Log.e("REFRESH", "Ошибка сети при обновлении токенов: ${t.message}")
                showToast("Ошибка сети: ${t.message}")
            }
        })
    }



    // Добавьте эту функцию в класс Activity
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
        //CurrentUser.user = User(userName ?: "", userEmail ?: "", "", isBlocked)        // for kotlin serv
        CurrentUser.user = User(userName ?: "", userEmail ?: "", "")        // for java serv
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



}