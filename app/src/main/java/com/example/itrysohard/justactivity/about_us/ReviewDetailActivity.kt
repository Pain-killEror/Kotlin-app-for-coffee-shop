package com.example.itrysohard.justactivity.about_us

import android.content.Intent
import android.os.Bundle
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.itrysohard.R
import com.example.itrysohard.databinding.ActivityReviewDetailBinding
import com.example.itrysohard.justactivity.PersAccActivity
import com.example.itrysohard.justactivity.StartActivity
import com.example.itrysohard.justactivity.menu.cart.CartActivity
import com.example.itrysohard.justactivity.menu.MenuActivity
import com.example.itrysohard.model.Review
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReviewDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

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