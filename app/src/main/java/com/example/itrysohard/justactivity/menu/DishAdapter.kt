package com.example.itrysohard.justactivity.menu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.itrysohard.R
import com.example.itrysohard.model.DishServ
import com.squareup.picasso.Picasso

class DishAdapter(private val onDishClick: (DishServ) -> Unit) : RecyclerView.Adapter<DishAdapter.DishViewHolder>() {

    private val dishes = mutableListOf<DishServ>()

    fun setDishes(newDishes: List<DishServ>) {
        // Можно использовать методы для более точного обновления списка
        dishes.clear()
        dishes.addAll(newDishes)
        notifyDataSetChanged() // Можно заменить на более специфичные вызовы
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dish, parent, false)
        return DishViewHolder(view, onDishClick)
    }

    override fun onBindViewHolder(holder: DishViewHolder, position: Int) {
        holder.bind(dishes[position])
    }

    override fun getItemCount(): Int = dishes.size

    class DishViewHolder(itemView: View, private val onDishClick: (DishServ) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        private val dishImage: ImageView = itemView.findViewById(R.id.ivDishImage)
        private val dishName: TextView = itemView.findViewById(R.id.tvDishName)
        private val dishPrice: TextView = itemView.findViewById(R.id.tvDishPrice)
        private val btnCheck: ImageButton = itemView.findViewById(R.id.btnCheck)
        fun bind(dish: DishServ) {
            // Загружаем изображение с указанием желаемого размера
            Picasso.get()
                .load(dish.imageUrl)
                .resize(500, 500) // Укажите желаемый размер
                .centerCrop() // Обеспечивает обрезку изображения для соответствия размеру
                .into(dishImage)

            dishName.text = dish.name
            dishPrice.text = "${dish.price} руб."

            // Устанавливаем обработчик клика
            btnCheck.setOnClickListener {
                onDishClick(dish)
            }
        }
    }
}