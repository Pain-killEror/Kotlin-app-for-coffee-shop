package com.example.itrysohard.justactivity.menu.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.itrysohard.databinding.ItemDishForCartBinding
import com.example.itrysohard.model.DishServ

class CartAdapter(
    private var dishes: List<DishServ>,
    private val selectedSizes: Map<Int, String?>,
    private val onRemoveClick: (DishServ) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    fun setDishes(newDishes: List<DishServ>) {
        dishes = newDishes
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemDishForCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding, onRemoveClick)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val dish = dishes[position]
        val size = selectedSizes[dish.id] ?: "Не выбран" // Получаем размер
        holder.bind(dish, size) // Передаем размер в метод bind
    }

    override fun getItemCount(): Int = dishes.size

    class CartViewHolder(
        private val binding: ItemDishForCartBinding,
        private val onRemoveClick: (DishServ) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(dish: DishServ, size: String?) {
            binding.tvDishName.text = dish.name
            binding.tvDishPrice.text = "${dish.price} руб."
            binding.tvDishDiscount.text = "${dish.discount} %"

            val discountAmount = dish.price * (dish.discount / 100.0)

            // Рассчитываем итоговую сумму
            val finalPrice = dish.price - discountAmount


            if(dish.discount <= 0){
                val params = binding.tvDishPrice.layoutParams as ViewGroup.MarginLayoutParams
                binding.tvDishDiscount.visibility = View.GONE
                params.bottomMargin = 20
                binding.tvDishPrice.layoutParams = params
                binding.tvDishPrice.text = "${dish.price} р."
            }
            else{
                binding.tvDishDiscount.visibility = View.VISIBLE
                val params =  binding.tvDishPrice.layoutParams as ViewGroup.MarginLayoutParams
                params.bottomMargin = 0
                binding.tvDishPrice.layoutParams = params

                binding.tvDishPrice.text = "${finalPrice} р."
            }

            // Показываем размер только если он не null
            if (size != "Не выбран") {
                binding.tvSize.text = "Размер: $size"
                binding.tvSize.visibility = View.VISIBLE // Делаем видимым
            } else {
                binding.tvSize.visibility = View.GONE // Скрываем, если размер null
            }

            binding.btnRemove.setOnClickListener {
                onRemoveClick(dish)
            }
        }
    }
}