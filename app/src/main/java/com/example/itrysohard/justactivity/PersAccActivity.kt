package com.example.itrysohard.justactivity

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.itrysohard.R
import com.example.itrysohard.databinding.ActivityPersAccBinding


import com.example.itrysohard.justactivity.menu.UserAdapter
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersAccBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()

        binding.btLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun setupView() {
        val user = CurrentUser.user

        if (user != null) {
            val isAdmin = CurrentUser.isAdmin
            binding.PersName.text = if (isAdmin) "Администратор" else user.name
            binding.PersEmail.text = if (isAdmin) "" else user.email
            binding.PersEmail.visibility = if (isAdmin) View.GONE else View.VISIBLE

            binding.avatar.setImageResource(R.drawable.avatar)

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
            showDeleteConfirmationDialog(user)
        }
        recyclerView.adapter = userAdapter
    }

    private fun fetchUsers() {
        val api = retrofitService.getUserApi() // Получите экземпляр UserApi

        api.getAllUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    userAdapter.updateUsers(response.body() ?: emptyList())
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                // Обработка ошибки
            }
        })
    }

    private fun showDeleteConfirmationDialog(user: User) {
        AlertDialog.Builder(this)
            .setTitle("Подтверждение удаления")
            .setMessage("Вы уверены, что хотите удалить пользователя ${user.name}?")
            .setPositiveButton("Да") { _, _ ->
                deleteUser(user)
            }
            .setNegativeButton("Нет", null)
            .show()
    }

    private fun deleteUser(user: User) {
        val api = retrofitService.getUserApi() // Получите экземпляр UserApi

        api.deleteUser(user.id!!).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    fetchUsers() // Обновляем список пользователей после удаления
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // Обработка ошибки
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
                setResult(RESULT_OK)
                finish()
            }
            .setNegativeButton("Нет", null)
            .show()
    }
}