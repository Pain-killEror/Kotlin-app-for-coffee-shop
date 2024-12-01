package com.example.itrysohard.justactivity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.itrysohard.databinding.ActivityMenuBinding
import com.example.myappforcafee.model.DishServ
import com.example.myappforcafee.retrofit.DishApi
import com.example.myappforcafee.retrofit.RetrofitService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding
    private lateinit var breakfastAdapter: DishAdapter
    private lateinit var dessertAdapter: DishAdapter
    private lateinit var drinkAdapter: DishAdapter
    private var isAdmin: Boolean = false
    private var isUserLoggedIn: Boolean = false


    companion object {
        private const val REQUEST_CODE_ADD_DISH = 101 // Move this inside the class
        private const val REQUEST_CODE_EDIT_DISH = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater) // Инициализация binding
        setContentView(binding.root)

        val resultMessage = intent.getStringExtra("result_message")
        if (resultMessage == "OK") {
            onResume()
            loadDishes() // Обновляем список блюд
        }

        isAdmin = intent.getBooleanExtra("isAdmin", false)
        isUserLoggedIn = intent.getBooleanExtra("isUserLoggedIn", false)

        // Настройка адаптеров с передачей onDishClick
        breakfastAdapter = DishAdapter { dish -> onDishSelected(dish) }
        dessertAdapter = DishAdapter { dish -> onDishSelected(dish) }
        drinkAdapter = DishAdapter { dish -> onDishSelected(dish) }

        // Подключение RecyclerView
        binding.recyclerViewBreakfast.apply {
            layoutManager = LinearLayoutManager(this@MenuActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = breakfastAdapter
        }

        binding.recyclerViewDesserts.apply {
            layoutManager = LinearLayoutManager(this@MenuActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = dessertAdapter
        }

        binding.recyclerViewDrinks.apply {
            layoutManager = LinearLayoutManager(this@MenuActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = drinkAdapter
        }

        binding.btnAddDish.visibility = if (isAdmin) View.VISIBLE else View.GONE

        // Переход на AddDishActivity
        binding.btnAddDish.setOnClickListener {
            val intent = Intent(this, AddDishActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_DISH)
        }

        // Загрузка данных
        loadDishes()
    }



    override fun onResume() {
        super.onResume()
            loadDishes()
    }

    private fun loadDishes() {
        val retrofitService = RetrofitService()
        val dishApi = retrofitService.getRetrofit().create(DishApi::class.java)

        val call: Call<List<DishServ>> = dishApi.getAllDishes()
        call.enqueue(object : Callback<List<DishServ>> {
            override fun onResponse(call: Call<List<DishServ>>, response: Response<List<DishServ>>) {
                if (response.isSuccessful) {
                    val dishes = response.body() ?: emptyList()

                    // Разделение блюд по категориям
                    val breakfasts = dishes.filter { it.category == "Завтрак" }
                    val desserts = dishes.filter { it.category == "Десерт" }
                    val drinks = dishes.filter { it.category == "Напиток" }

                    breakfastAdapter.setDishes(breakfasts)
                    dessertAdapter.setDishes(desserts)
                    drinkAdapter.setDishes(drinks)
                } else {
                    Toast.makeText(
                        this@MenuActivity,
                        "Ошибка загрузки меню: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

            override fun onFailure(call: Call<List<DishServ>>, t: Throwable) {
                Toast.makeText(
                    this@MenuActivity,
                    "Ошибка сети: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun onDishSelected(dish: DishServ) {
        // Переход на экран с деталями блюда
        val intent = Intent(this, DishDetailActivity::class.java)
        intent.putExtra("DISH_NAME", dish.name)
        intent.putExtra("DISH_DESCRIPTION", dish.description)
        intent.putExtra("DISH_IMAGE_URL", dish.imageUrl)
        intent.putExtra("DISH_PRICE", dish.price)
        intent.putExtra("DISH_ID", dish.id)
        if(isAdmin){
            intent.putExtra("isAdmin", true) // Передаем, если пользователь админ

        }
        startActivity(intent)

    }



}
