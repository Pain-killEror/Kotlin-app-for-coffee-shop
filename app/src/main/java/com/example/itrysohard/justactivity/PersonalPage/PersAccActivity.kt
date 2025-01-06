package com.example.itrysohard.justactivity.PersonalPage

import android.content.Intent
import android.os.Bundle
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


import com.example.itrysohard.model.CurrentUser
import com.example.itrysohard.model.User
import com.example.itrysohard.retrofitforDU.RetrofitService
import com.example.itrysohard.retrofitforDU.UserApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PersAccActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersAccBinding
    private lateinit var userAdapter: UserAdapter
    private lateinit var recyclerView: RecyclerView
    private val retrofitService = RetrofitService() // Инициализация RetrofitService
    private lateinit var userApi: UserApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityHistoryImpl.addActivity(this::class.java)
        binding = ActivityPersAccBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()

        binding.btLogout.setOnClickListener {
            showLogoutConfirmationDialog()
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
            finish()
        }

        binding.btnPersAcc.setOnClickListener {
            Toast.makeText(this, "Вы на в личном кабинете!", Toast.LENGTH_SHORT).show()
        }

        binding.btnDelete.setOnClickListener{
            showDeleteConfirmationDialog()
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


    private fun setupView() {
        val user = CurrentUser.user

        if (user != null) {
            val isAdmin = CurrentUser.isAdmin
            binding.PersName.text = if (isAdmin) "Администратор" else user.name
            binding.PersEmail.text = if (isAdmin) "" else user.email
            binding.PersEmail.visibility = if (isAdmin) View.GONE else View.VISIBLE

            binding.avatar.setImageResource(R.drawable.user_photo)

            if (isAdmin) {
                setupRecyclerView()
                fetchUsers()
            }
        } else {
            binding.PersName.text = "Имя пользователя"
            binding.PersEmail.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        userAdapter = UserAdapter(this, emptyList()) { user ->
            showBlockConfirmationDialog(user)
        }
        recyclerView.adapter = userAdapter
    }

    private fun fetchUsers() {
        val api = retrofitService.getUserApi() // Получите экземпляр UserApi

        api.getAllUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    // Фильтруем пользователей, исключая тех, у кого ID 1 и 2
                    val filteredUsers = response.body()?.filter { user ->
                        user.id != 1L && user.id != 2L // Сравниваем с Long
                    } ?: emptyList()

                    userAdapter.updateUsers(filteredUsers)
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                // Обработка ошибки
            }
        })
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Подтверждение удаление")
            .setMessage("Вы уверены, что хотите удалить свой акканут НАВСЕГДА и БЕЗ возможности восстановления?")
            .setPositiveButton("Да") { _, _ ->
                deleteUser()


            }
            .setNegativeButton("Нет", null)
            .show()
    }

    private fun deleteUser(){
        val user = CurrentUser.user
        val api = retrofitService.getUserApi()

        api.deleteUser(user!!.id!!).enqueue(object: Callback<Void>{
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    fetchUsers()

                    Toast.makeText(this@PersAccActivity, "Ваш аккаунт успешно удален!", Toast.LENGTH_SHORT).show()
                    CurrentUser.user = null
                    CurrentUser.isAdmin = false
                    startActivity(Intent(this@PersAccActivity, StartActivity::class.java))
                    finish()// Обновляем список пользователей после удаления
                }
                else Toast.makeText(this@PersAccActivity, "Что-то пошло не так ${response.code()}!", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@PersAccActivity, "Ошибка подключения!", Toast.LENGTH_SHORT).show()
            }
        })

    }


    private fun showBlockConfirmationDialog(user: User) {
        AlertDialog.Builder(this)
            .setTitle("Подтверждение блокировки")
            .setMessage("Вы уверены, что хотите заблокировать пользователя ${user.name}?")
            .setPositiveButton("Да") { _, _ ->
                blockUser(user)
                CurrentUser.isBlocked = true

            }
            .setNegativeButton("Нет", null)
            .show()
    }

    private fun blockUser(user: User) {
        val api = retrofitService.getUserApi() // Получите экземпляр UserApi

        api.updateUserBlockedStatus(user.id!!, true).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    fetchUsers()
                    Toast.makeText(this@PersAccActivity, "Пользователь успешно заблокирован!", Toast.LENGTH_SHORT).show()// Обновляем список пользователей после удаления
                }
                else Toast.makeText(this@PersAccActivity, "Что-то пошло не так ${response.code()}!", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@PersAccActivity, "Ошибка подключения!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Подтверждение")
            .setMessage("Вы уверены, что хотите выйти из аккаунта?")
            .setPositiveButton("Да") { _, _ ->
                CurrentUser.user = null
                CurrentUser.isAdmin = false
                startActivity(Intent(this@PersAccActivity, StartActivity::class.java))
                finish()
            }
            .setNegativeButton("Нет", null)
            .show()
    }
}