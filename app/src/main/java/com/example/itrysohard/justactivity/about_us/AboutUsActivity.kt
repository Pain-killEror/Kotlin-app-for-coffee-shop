package com.example.itrysohard.justactivity.about_us

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.itrysohard.MyApplication
import com.example.itrysohard.R
import com.example.itrysohard.databinding.ActivityAboutUsBinding
import com.example.itrysohard.justactivity.PersAccActivity
import com.example.itrysohard.justactivity.RegAuthActivity
import com.example.itrysohard.justactivity.StartActivity
import com.example.itrysohard.justactivity.menu.cart.CartActivity
import com.example.itrysohard.model.Review
import com.example.itrysohard.retrofitforDU.ReviewApi
import com.example.itrysohard.retrofitforDU.RetrofitService
import com.example.itrysohard.justactivity.menu.MenuActivity
import com.example.itrysohard.model.CurrentUser
import com.example.itrysohard.model.DishServ
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AboutUsActivity : AppCompatActivity() {

    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var reviewApi: ReviewApi
    private lateinit var myApplication: MyApplication
    private lateinit var binding: ActivityAboutUsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutUsBinding.inflate(layoutInflater) // Инициализация View Binding
        setContentView(binding.root) // Устанавливаем корневой элемент из binding

        // Инициализация RecyclerView
        reviewAdapter = ReviewAdapter(emptyList()) { review ->
            // Обработка клика на отзыв
            val intent = Intent(this, ReviewDetailActivity::class.java).apply {
                putExtra("review", review)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Удаляем все предыдущие активности
            }
            startActivity(intent)

        }

        binding.recyclerViewReviews.adapter = reviewAdapter
        binding.recyclerViewReviews.layoutManager = LinearLayoutManager(this)

        // Инициализация Retrofit и API
        reviewApi = RetrofitService().getRetrofit().create(ReviewApi::class.java)

        // Загрузка отзывов
        loadReviews()

        binding.buttonLeaveReview.setOnClickListener {
            startActivity(Intent(this, LeaveReviewActivity::class.java))

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
        myApplication = application as MyApplication
        updateCartCountDisplay()

        binding.buttonLeaveReview.setOnClickListener {
           showAuthorizationDialog()

        }
    }

    private fun showAuthorizationDialog() {
        if (CurrentUser.user == null) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Необходимо авторизоваться")
            builder.setMessage("Вы не авторизованы. Пожалуйста, авторизуйтесь или вернитесь к просмотру отзывов.")

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
        else startActivity(Intent(this, LeaveReviewActivity::class.java))
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
        loadReviews()
        updateCartCountDisplay()
    }
    private fun updateCartCountDisplay() {
        binding.tvCartCount.text = myApplication.cartItemCount.toString()
    }

    private fun loadReviews() {
        reviewApi.getAllReviews().enqueue(object : Callback<List<Review>> {
            override fun onResponse(call: Call<List<Review>>, response: Response<List<Review>>) {
                if (response.isSuccessful) {
                    val reviews = response.body() ?: emptyList()
                    reviewAdapter.setReviews(reviews)

                } else {
                    Toast.makeText(this@AboutUsActivity, "Ошибка загрузки отзывов: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Review>>, t: Throwable) {
                Toast.makeText(this@AboutUsActivity, "Ошибка загрузки: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}