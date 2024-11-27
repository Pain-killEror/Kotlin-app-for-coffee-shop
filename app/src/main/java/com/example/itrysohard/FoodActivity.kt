package com.example.itrysohard

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.itrysohard.databinding.ActivityFoodBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.Toast
import com.example.myappforcafee.model.DishServ
import com.example.myappforcafee.retrofit.DishApi
import com.example.myappforcafee.retrofit.RetrofitService

class FoodActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFoodBinding
    private lateinit var dishAdapter: DishAdapter

    private var isAdmin: Boolean = false
    private var isUserLoggedIn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Получаем данные из интента
        isAdmin = intent.getBooleanExtra("isAdmin", false)
        isUserLoggedIn = intent.getBooleanExtra("isUserLoggedIn", false)

        // Настройка RecyclerView
        dishAdapter = DishAdapter()
        binding.recyclerViewDishes.apply {
            layoutManager = LinearLayoutManager(this@FoodActivity)
            adapter = dishAdapter
        }

        // Показываем кнопку добавления блюда только для админа
        binding.btnAddDish.visibility = if (isAdmin) View.VISIBLE else View.GONE

        // Переход на AddDishActivity
        binding.btnAddDish.setOnClickListener {
            val intent = Intent(this, AddDishActivity::class.java)
            startActivity(intent)
        }

        // Загрузка блюд из сервера
        loadDishes()
    }


    private fun loadDishes() {
        val retrofitService = RetrofitService()
        val dishApi = retrofitService.getRetrofit().create(DishApi::class.java)

        val call = dishApi.getAllDishes()
        call.enqueue(object : Callback<List<DishServ>> {
            override fun onResponse(call: Call<List<DishServ>>, response: Response<List<DishServ>>) {
                if (response.isSuccessful) {
                    val dishes = response.body() ?: emptyList()
                    dishAdapter.setDishes(dishes)
                } else {
                    Toast.makeText(this@FoodActivity, "Ошибка загрузки блюд: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<DishServ>>, t: Throwable) {
                Toast.makeText(this@FoodActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
