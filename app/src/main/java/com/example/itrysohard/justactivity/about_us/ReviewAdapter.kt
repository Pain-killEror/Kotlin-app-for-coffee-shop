package com.example.itrysohard.justactivity.about_us

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.itrysohard.R
import com.example.itrysohard.model.answ.ReviewAnswDTO
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewAdapter(
    private var reviews: List<ReviewAnswDTO>,
    private val onClick: (ReviewAnswDTO) -> Unit
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    fun setReviews(newReviews: List<ReviewAnswDTO>) {
        reviews = newReviews
        notifyDataSetChanged()
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviews[position])
    }

    override fun getItemCount(): Int = reviews.size

    class ReviewViewHolder(
        private val view: View,
        private val onClick: (ReviewAnswDTO) -> Unit
    ) : RecyclerView.ViewHolder(view) {

        private val titleTextView: TextView = view.findViewById(R.id.textViewReviewTitle)
        private val ratingBar: RatingBar = view.findViewById(R.id.ratingBar)
        private val descriptionTextView: TextView = view.findViewById(R.id.textViewReviewDescription)

        fun bind(review: ReviewAnswDTO) {
            titleTextView.text = review.title
            ratingBar.rating = review.rating.toFloat()
            descriptionTextView.text = review.description.take(50) + if (review.description.length > 50) "..." else ""

            view.setOnClickListener { onClick(review) }
        }

        private fun formatDate(dateString: String): String {
            return try {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                formatter.format(parser.parse(dateString)!!)
            } catch (e: Exception) {
                "Дата недоступна"
            }
        }
    }
}