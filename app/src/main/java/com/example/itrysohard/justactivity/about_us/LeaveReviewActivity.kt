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
import com.example.itrysohard.jwt.SharedPrefTokenManager
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
        val tokenManager = SharedPrefTokenManager(this)
        reviewApi = RetrofitService(this,tokenManager).getRetrofit().create(ReviewApi::class.java)

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

        if (title.isEmpty() || description.isEmpty() || rating == 0f) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        // Конвертируем Float в Byte (0-5)
        val ratingByte = rating.toInt().coerceIn(0, 5).toByte()

        // Создаем DTO без username
        val review = Review(
            title = title,
            rating = ratingByte,
            description = description
        )

        Log.d("LeaveReviewActivity", "Отправляемый объект: $review")
        buttonSubmit.isEnabled = false

        reviewApi.addReview(review).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                buttonSubmit.isEnabled = true
                when {
                    response.isSuccessful -> {
                        Toast.makeText(
                            this@LeaveReviewActivity,
                            "Отзыв успешно отправлен",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                    response.code() == 429 -> {
                        Toast.makeText(
                            this@LeaveReviewActivity,
                            "Ошибка: нельзя оставлять более 1 отзыва в месяц",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    response.code() == 404 -> {
                        Toast.makeText(
                            this@LeaveReviewActivity,
                            "Пользователь не найден",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                        val error = response.errorBody()?.string() ?: "Неизвестная ошибка"
                        Toast.makeText(
                            this@LeaveReviewActivity,
                            "Ошибка: $error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                buttonSubmit.isEnabled = true
                Log.e("LeaveReviewActivity", "Ошибка: ${t.message}")
                Toast.makeText(
                    this@LeaveReviewActivity,
                    "Сетевая ошибка: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}