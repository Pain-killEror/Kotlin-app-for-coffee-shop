package com.example.itrysohard.justactivity.about_us

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.itrysohard.BackPress.ActivityHistoryImpl
import com.example.itrysohard.BackPress.BackPressManager
import com.example.itrysohard.R
import com.example.itrysohard.databinding.ActivityReviewDetailBinding
import com.example.itrysohard.justactivity.MainPage.StartActivity
import com.example.itrysohard.justactivity.PersonalPage.PersAccActivity
import com.example.itrysohard.justactivity.RegistrationAuthentication.RegAuthActivity
import com.example.itrysohard.justactivity.menu.MenuActivity
import com.example.itrysohard.justactivity.menu.cart.CartActivity
import com.example.itrysohard.model.CurrentUser
import com.example.itrysohard.model.Review
import com.example.itrysohard.retrofitforDU.RetrofitService
import com.example.itrysohard.retrofitforDU.ReviewApi
import com.example.itrysohard.retrofitforDU.UserApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReviewDetailBinding
    private lateinit var reviewApi: ReviewApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityHistoryImpl.addActivity(this::class.java)
        binding = ActivityReviewDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val retrofitService = RetrofitService()
        reviewApi = retrofitService.getRetrofit().create(ReviewApi::class.java)

        // Получаем отзыв из Intent
        val review = intent.getSerializableExtra("review") as? Review
            ?: throw IllegalArgumentException("Review data is missing")

        // Находим элементы UI
        val titleTextView: TextView = findViewById(R.id.textViewReviewTitle)
        val ratingBar: RatingBar = findViewById(R.id.ratingBar)
        val descriptionTextView: TextView = findViewById(R.id.textViewReviewDescription)
        val writerNameTextView: TextView = findViewById(R.id.textWriterName)
        val publicationTimeTextView: TextView = findViewById(R.id.textPublicationTime)

        // Заполняем элементы данными отзыва
        titleTextView.text = review.title
        ratingBar.rating = review.rating
        descriptionTextView.text = review.description
        writerNameTextView.text = "Автор: ${review.username}"
        publicationTimeTextView.text = "Дата публикации: ${formatPublicationTime(review.createdAt.toString())}" // Форматируем дату

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

        binding.btnDelete.setOnClickListener{
            deleteThisReview(review.id)
            finish()
        }
    }

    override fun onBackPressed() {
        BackPressManager.handleBackPress(this) {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        val isAdmin = CurrentUser.isAdmin
        if(isAdmin) binding.btnDelete.visibility = View.VISIBLE else binding.btnDelete.visibility = View.GONE
    }

    private fun deleteThisReview(id: Long?){
        reviewApi.deleteReview(id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // Успешное удаление отзыва
                    Toast.makeText(this@ReviewDetailActivity, "Отзыва успешно удален", Toast.LENGTH_SHORT).show()
                } else {
                    // Ошибка при удалении отзыва
                    Toast.makeText(this@ReviewDetailActivity, "Ошибка при удалении", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                // Ошибка при подключении
                Toast.makeText(this@ReviewDetailActivity, "Ошибка подключения", Toast.LENGTH_SHORT).show()
            }
        })
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
                finish()
            }

            builder.setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss() // Закрываем диалог
            }

            val dialog = builder.create()
            dialog.show()
        }
        else startActivity(Intent(this, PersAccActivity::class.java))
        finish()
    }

    private fun formatPublicationTime(publicationTime: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

        return try {
            val date: Date = inputFormat.parse(publicationTime) ?: Date()
            outputFormat.format(date)
        } catch (e: Exception) {
            publicationTime
        }
    }
}