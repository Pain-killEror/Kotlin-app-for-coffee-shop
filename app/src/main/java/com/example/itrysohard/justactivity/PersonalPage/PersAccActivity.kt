package com.example.itrysohard.justactivity.PersonalPage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.itrysohard.BackPress.ActivityHistoryImpl
import com.example.itrysohard.BackPress.BackPressManager
import com.example.itrysohard.R
import com.example.itrysohard.databinding.ActivityPersAccBinding
import com.example.itrysohard.justactivity.MainPage.StartActivity
import com.example.itrysohard.justactivity.RegistrationAuthentication.RegAuthActivity
import com.example.itrysohard.justactivity.menu.MenuActivity
import com.example.itrysohard.justactivity.menu.cart.CartActivity
import com.example.itrysohard.jwt.JWTDecoder
import com.example.itrysohard.jwt.SharedPrefTokenManager
import com.example.itrysohard.jwt.TokenManager


import com.example.itrysohard.model.CurrentUser
import com.example.itrysohard.model.User
import com.example.itrysohard.model.answ.UserAnswDTO
import com.example.itrysohard.model.answ.UserAnswDTORolesNoRev
import com.example.itrysohard.model.info.UserInfoDTO
import com.example.itrysohard.retrofitforDU.RetrofitService
import com.example.itrysohard.retrofitforDU.UserApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PersAccActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersAccBinding
    private lateinit var userAdapter: UserAdapter
    private lateinit var tokenManager: TokenManager
    private lateinit var userApi: UserApi
    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable { loadUsers(currentSearchText) }
    private var currentSearchText: String = ""
    private var currentUser: UserInfoDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersAccBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = SharedPrefTokenManager(this)
        userApi = RetrofitService(this, tokenManager).getUserApi()



        loadUsers()
        checkAuthorization()
        setupClickListeners()
        setupView()
    }


    private fun checkAuthorization() {
        val prefs = getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        val accessToken = prefs.getString("ACCESS_TOKEN", null)
        val refreshToken = prefs.getString("REFRESH_TOKEN", null)

        if (accessToken == null || JWTDecoder.isExpired(refreshToken)) {
            redirectToLogin()
        } else {
            loadUserData()
        }
    }

    private fun getUserByName(onResult: (UserInfoDTO?) -> Unit) {
        val prefs = getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        val accessToken = prefs.getString("ACCESS_TOKEN", null)
        val name = JWTDecoder.getName(accessToken) // Извлекаем имя из токена

        userApi.getUserByName(name).enqueue(object : Callback<UserInfoDTO> {
            override fun onResponse(
                call: Call<UserInfoDTO>,
                response: Response<UserInfoDTO>
            ) {

                if (response.isSuccessful) {
                    val userData = response.body()
                    if (userData != null) {
                        // Если пользователь является администратором, загружаем список пользователей
                        // Передаём полученные данные через callback

                        onResult(userData)
                    } else {
                        showToast("Получены пустые данные")
                        onResult(null)
                    }
                } else if (response.code() == 403) {
                    showToast("Доступ запрещен")
                    onResult(null)
                } else {
                    showToast("Ошибка: ${response.code()}")
                    onResult(null)
                }
            }

            override fun onFailure(call: Call<UserInfoDTO>, t: Throwable) {
                showToast("Ошибка сети: ${t.message}")
                onResult(null)
            }
        })

    }


    private fun loadUserData() {

        getUserByName { userData ->
            if (userData != null) {
                updateUI(userData)
            } else {
                showToast("Ошибка загрузки ваших данных")
            }
        }
    }

    private fun updateUI(user: UserInfoDTO) {
        val email = user.email
        val name = user.name
        binding.tvPersName.text = name
        binding.tvPersEmail.setText(email)
        binding.tvSearchName.visibility = if (isAdmin()) View.VISIBLE else View.INVISIBLE
        binding.tvPersEmail.visibility = if (isAdmin()) View.INVISIBLE else View.VISIBLE
        binding.btnDelete.visibility = if (isAdmin()) View.INVISIBLE else View.VISIBLE

    }

    private fun isAdmin(): Boolean {
        val prefs = getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        val accessToken = prefs.getString("ACCESS_TOKEN", null)
        if (JWTDecoder.getRole(accessToken) == "ROLE_ADMIN") return true
        else return false

    }

    // При загрузке списка пользователей:
    fun loadUsers(prefix: String = "") {
        val call = if (prefix.isEmpty()) {
            userApi.getAllUsersWithRoles()
        } else {
            userApi.getUserByPartOfName(prefix)
        }

        call.enqueue(object : Callback<List<UserAnswDTORolesNoRev>> {
            override fun onResponse(
                call: Call<List<UserAnswDTORolesNoRev>>,
                response: Response<List<UserAnswDTORolesNoRev>>
            ) {
                if (response.isSuccessful) {
                    val prefs = getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
                    val accessToken = prefs.getString("ACCESS_TOKEN", null)

                    val filteredUsers = response.body()
                        ?.filter {
                            it.role != "REMOVED" &&
                                    it.role != "MODERATOR" &&
                                    it.name != JWTDecoder.getName(accessToken)
                        } ?: emptyList()

                    userAdapter.updateUsers(filteredUsers)
                }
            }

            override fun onFailure(call: Call<List<UserAnswDTORolesNoRev>>, t: Throwable) {
                showToast("Ошибка загрузки: ${t.message}")
            }
        })
    }





    private fun setupClickListeners() {
        binding.btLogout.setOnClickListener { showLogoutConfirmationDialog() }
        binding.btnHome.setOnClickListener { navigateTo(StartActivity::class.java) }
        binding.btnMenu.setOnClickListener { navigateTo(MenuActivity::class.java) }
        binding.btnCart.setOnClickListener { navigateTo(CartActivity::class.java) }
        binding.btnDelete.setOnClickListener { showDeleteConfirmationDialog() }
    }

    private fun setupView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        userAdapter = UserAdapter(
            context = this,
            users = emptyList(),
            onBlock = { user -> showBlockConfirmationDialog(user) }
        )

        binding.recyclerView.adapter = userAdapter

        // Добавляем слушатель изменений текста
        // Правильная реализация TextWatcher
        if (isAdmin()) {
            binding.tvSearchName.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val newText = s?.toString()?.trim() ?: ""
                    if (newText != currentSearchText) {
                        currentSearchText = newText
                        handler.removeCallbacks(searchRunnable)
                        handler.postDelayed(searchRunnable, 500)
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    // Пустая реализация, но обязательная
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // Пустая реализация, но обязательная
                }
            })
        }
    }

    private fun showBlockConfirmationDialog(user: UserAnswDTORolesNoRev) {
        if (user.role == "BLOCKED") {
            // Если роль пользователя ROLE_BLOCKED, выводим диалог для разблокировки
            AlertDialog.Builder(this)
                .setTitle("Разблокировка")
                .setMessage("Вы уверены, что хотите разблокировать ${user.name}?")
                .setPositiveButton("Да") { _, _ -> performUnblockUser(user.id) }
                .setNegativeButton("Нет", null)
                .show()
        } else if (user.role == "USER") {
            // Если роль пользователя USER, выводим диалог для блокировки
            AlertDialog.Builder(this)
                .setTitle("Блокировка")
                .setMessage("Заблокировать ${user.name}?")
                .setPositiveButton("Да") { _, _ -> blockUser(user.id) }
                .setNegativeButton("Нет", null)
                .show()
        }
    }

    private fun performUnblockUser(userId: Long) {
        userApi.unblockUser(userId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    showToast("Пользователь разблокирован")
                    loadUsers() // Обновляем список пользователей
                } else {
                    showToast("Ошибка разблокировки: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                showToast("Ошибка сети при разблокировке: ${t.message}")
            }
        })
    }

    private fun blockUser(userId: Long) {
        userApi.blockUser(userId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    loadUsers() // Обновляем список после блокировки
                    showToast("Пользователь заблокирован")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                showToast("Ошибка блокировки: ${t.message}")
            }
        })
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Удаление аккаунта")
            .setMessage("Вы уверены? Это действие нельзя отменить!")
            .setPositiveButton("Да") { _, _ -> deleteAccount() }
            .setNegativeButton("Нет", null)
            .show()
    }

    private fun deleteAccount() {
        getUserByName { userData ->
            if (userData != null) {
                val userId = userData.id
                userApi.removeUser(userId).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            tokenManager.clearTokens()
                            navigateTo(StartActivity::class.java)
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        showToast("Ошибка удаления: ${t.message}")
                    }
                })
            } else {
                showToast("Пользователь не найден")
            }
        }

    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Выход")
            .setMessage("Вы уверены что хотите выйти?")
            .setPositiveButton("Да") { _, _ -> logout() }
            .setNegativeButton("Нет", null)
            .show()
    }

    private fun logout() {
        clearTokens()
        navigateTo(StartActivity::class.java)
    }
    fun clearTokens() {
        val prefs = getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        val accessToken = prefs.getString("ACCESS_TOKEN", null)
        // Удаляем токены по ключам, используемым при сохранении, например, "access_token" и "refresh_token"
        prefs.edit()
            .remove("ACCESS_TOKEN")
            .remove("REFRESH_TOKEN")
            .apply()

        Log.d("TokenDebug", "Токены успешно удалены. tokens: ${accessToken}")
    }

    private fun navigateTo(cls: Class<*>) {
        startActivity(Intent(this, cls))
        finish()
    }

    private fun redirectToLogin() {
        startActivity(Intent(this, RegAuthActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}