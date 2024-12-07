package com.example.itrysohard.justactivity.about_us

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.itrysohard.R
import com.example.itrysohard.model.Review

class ReviewAdapter(
    private var reviews: List<Review>,
    private val onClick: (Review) -> Unit // Лямбда для обработки клика на отзыв
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    // Метод для обновления списка отзывов
    fun setReviews(newReviews: List<Review>) {
        reviews = newReviews
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviews[position])
    }

    override fun getItemCount(): Int = reviews.size

    class ReviewViewHolder(
        private val view: View,
        private val onClick: (Review) -> Unit
    ) : RecyclerView.ViewHolder(view) {

        fun bind(review: Review) {
            val titleTextView = view.findViewById<TextView>(R.id.textViewReviewTitle)
            val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
            val descriptionTextView = view.findViewById<TextView>(R.id.textViewReviewDescription)

            titleTextView.text = review.title
            ratingBar.rating = review.rating
            descriptionTextView.text = review.description.take(50) + if (review.description.length > 50) "..." else ""

            // Обработка клика на элемент отзыва
            view.setOnClickListener {
                onClick(review)
            // Вызов лямбды при клике
            }
        }
    }
}