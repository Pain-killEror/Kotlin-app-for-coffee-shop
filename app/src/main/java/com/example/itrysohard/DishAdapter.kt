package com.example.itrysohard

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.example.itrysohard.databinding.ItemDishBinding
import com.example.myappforcafee.model.DishServ

class DishAdapter : RecyclerView.Adapter<DishAdapter.DishViewHolder>() {

    private val dishes = mutableListOf<DishServ>()

    fun setDishes(newDishes: List<DishServ>) {
        dishes.clear()
        dishes.addAll(newDishes)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishViewHolder {
        val binding = ItemDishBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DishViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DishViewHolder, position: Int) {
        holder.bind(dishes[position])
    }

    override fun getItemCount(): Int = dishes.size

    class DishViewHolder(private val binding: ItemDishBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(dish: DishServ) {
            binding.tvDishName.text = dish.name
            binding.tvDishPrice.text = "Цена: ${dish.price} руб."
            var imageUrl: String?
            imageUrl = dish.photoPath // Используем дефолтное изображение, если путь пуст
            Log.d("DishAdapter", "Image path: $imageUrl")
            Log.d("DishAdapter", "name: ${dish.name}")
            Log.d("DishAdapter", "price: ${dish.price}")

            Glide.with(binding.ivDishImage.context)
                .load("$imageUrl")  // Правильный URL для загрузки изображения
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(binding.ivDishImage)
        }
    }

}
