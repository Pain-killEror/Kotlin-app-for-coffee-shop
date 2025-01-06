package com.example.itrysohard.justactivity.about_us

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.itrysohard.BackPress.ActivityHistoryImpl
import com.example.itrysohard.BackPress.BackPressManager
import com.example.itrysohard.R
import com.example.itrysohard.databinding.ActivityLeaveReviewBinding
import com.example.itrysohard.model.CurrentUser
import com.example.itrysohard.model.Review
import com.example.itrysohard.retrofitforDU.ReviewApi
import com.example.itrysohard.retrofitforDU.RetrofitService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LeaveReviewActivity : AppCompatActivity() {

    private lateinit var reviewApi: ReviewApi
    private lateinit var editTextTitle: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var ratingBar: RatingBar
    private lateinit var buttonSubmit: Button
    private lateinit var binding: ActivityLeaveReviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityHistoryImpl.addActivity(this::class.java)
        binding = ActivityLeaveReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editTextTitle = findViewById(R.id.editTextTitle)
        editTextDescription = findViewById(R.id.editTextDescription)
        ratingBar = findViewById(R.id.ratingBar)
        buttonSubmit = findViewById(R.id.buttonSubmit)

        reviewApi = RetrofitService().getRetrofit().create(ReviewApi::class.java)

        buttonSubmit.setOnClickListener {
            submitReview()

        }


    }

    override fun onBackPressed() {
        BackPressManager.handleBackPress(this) {
            super.onBackPressed()
        }
    }

    private fun submitReview() {
        val title = editTextTitle.text.toString().trim()
        val description = editTextDescription.text.toString().trim()
        val rating = ratingBar.rating

        // Проверяем заполненность всех полей
        if (title.isEmpty() || description.isEmpty() || rating == 0f) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        val newReview = Review(
            username = CurrentUser.user?.name ?: "Аноним",
            title = title,
            rating = rating,
            description = description,
            createdAt = null
        )

        Log.d("LeaveReviewActivity", "Отправляемый объект: $newReview")
        buttonSubmit.isEnabled = false

        reviewApi.addReview(newReview).enqueue(object : Callback<Review> {
            override fun onResponse(call: Call<Review>, response: Response<Review>) {
                buttonSubmit.isEnabled = true
                if (response.isSuccessful) {
                    Toast.makeText(this@LeaveReviewActivity, "Отзыв успешно отправлен.", Toast.LENGTH_SHORT).show()
                    finish() // Закрываем активность после успешной отправки
                } else {
                    val errorMessage = when (response.code()) {
                        500 -> "Ошибка: пользователь не может оставить более 1 отзыва в месяц."
                        else -> response.errorBody()?.string() ?: "Неизвестная ошибка"
                    }
                    Toast.makeText(this@LeaveReviewActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Review>, t: Throwable) {
                buttonSubmit.isEnabled = true
                Log.e("LeaveReviewActivity", "Ошибка: ${t.message}")
                Toast.makeText(this@LeaveReviewActivity, "Ошибка: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}