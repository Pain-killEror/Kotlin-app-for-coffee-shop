package com.example.itrysohard.justactivity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.itrysohard.databinding.ActivityDishDetailBinding
import com.example.myappforcafee.retrofit.DishApi
import com.example.myappforcafee.retrofit.RetrofitService
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DishDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDishDetailBinding
    private var isAdmin: Boolean = false // This should be passed from the previous activity if needed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDishDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get data from Intent
        val name = intent.getStringExtra("DISH_NAME")
        val description = intent.getStringExtra("DISH_DESCRIPTION")
        val imageUri = intent.getStringExtra("DISH_IMAGE_URL")
        val price = intent.getDoubleExtra("DISH_PRICE", 0.0)
        val dishId = intent.getIntExtra("DISH_ID", -1) // Use a local variable here

        // Set data in the UI
        binding.tvDishName.text = name
        binding.tvDishDescription.text = description
        binding.tvDishPrice.text = "$price руб."
        Glide.with(this)
            .load(imageUri)
            .override(700, 700)
            .into(binding.ivDishImage)

        // Check if user is admin and set the visibility of the buttons
        isAdmin = intent.getBooleanExtra("isAdmin", false)
        binding.btnDelete.visibility = if (isAdmin) View.VISIBLE else View.GONE
        binding.btnEdit.visibility = if (isAdmin) View.VISIBLE else View.GONE

        // Set up delete button click listener
        binding.btnDelete.setOnClickListener {
            if (dishId != -1) {
                deleteDish(dishId)
            } else {
                showToast("Некорректный идентификатор блюда")
            }
        }

        // Set up edit button click listener to open AddDishActivity for editing
        binding.btnEdit.setOnClickListener {
            if (dishId != -1) {
                val intent = Intent(this, AddDishActivity::class.java).apply {
                    putExtra("dish_id", dishId)
                    putExtra("dish_name", name)
                    putExtra("dish_description", description)
                    putExtra("dish_price", price)
                    putExtra("dish_image_uri", imageUri
                    )
                }
                startActivity(intent)
                finish()
            } else {
                showToast("Некорректный идентификатор блюда")
            }
        }
    }

    private fun deleteDish(id: Int) {
        val retrofitService = RetrofitService()
        val dishApi = retrofitService.getRetrofit().create(DishApi::class.java)

        val call: Call<Void> = dishApi.deleteDish(id)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    showToast("Блюдо успешно удалено")
                    setResult(Activity.RESULT_OK)
                    finish() // Close the activity after deletion
                } else {
                    showToast("Ошибка при удалении блюда: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                showToast("Ошибка сети: ${t.message}")
            }
        })
    }

    private fun showToast(message: String) {
        // Function to show a Toast message
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}


