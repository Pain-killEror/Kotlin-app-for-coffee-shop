package com.example.itrysohard.justactivity.menu.cart

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.itrysohard.MyApplication
import com.example.itrysohard.databinding.ActivityCartBinding
import com.example.itrysohard.model.DishServ

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var cartAdapter: CartAdapter
    private var cartItems = mutableListOf<DishServ>()
    private lateinit var myApplication: MyApplication
    private lateinit var selectedSizes: MutableMap<Int, String?> // Изменено на MutableMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)


        myApplication = application as MyApplication

        cartItems = myApplication.cartItems
        selectedSizes = myApplication.selectedSizes.toMutableMap() // Получаем выбранные размеры как изменяемую карту

        setupRecyclerView()

        // Обновляем итоговую стоимость
        updateTotalPrice()

        binding.btnCheckout.setOnClickListener {
            clearCart()
            Toast.makeText(this, "Покупка успешно совершена!", Toast.LENGTH_SHORT).show()
        }

    }

    private fun clearCart() {
        cartItems.clear() // Очищаем список корзины
        myApplication.cartItemCount = 0 // Сбрасываем счетчик товаров в корзине
        selectedSizes.clear() // Очищаем выбранные размеры
        cartAdapter.setDishes(cartItems) // Обновляем адаптер
        updateTotalPrice() // Обновляем итоговую стоимость
    }

    private fun setupRecyclerView() {
        binding.recyclerViewCart.layoutManager = LinearLayoutManager(this)
        cartAdapter = CartAdapter(cartItems, selectedSizes) { dish -> removeFromCart(dish) } // Передаем onRemoveClick
        binding.recyclerViewCart.adapter = cartAdapter
    }

    private fun removeFromCart(dish: DishServ) {
        val index = cartItems.indexOf(dish)
        if (index != -1) {
            cartItems.removeAt(index)
            myApplication.cartItemCount -= 1
            selectedSizes.remove(dish.id) // Удаляем соответствующий размер по ID
            cartAdapter.setDishes(cartItems) // Обновляем адаптер
            updateTotalPrice() // Обновляем итоговую стоимость
        }
    }

    private fun updateTotalPrice() {
        if (cartItems.isEmpty()) {
            binding.tvTotalPrice.text = "Корзина пуста"
        } else {
            val totalPrice = cartItems.sumOf { it.price }
            binding.tvTotalPrice.text = "Итоговая стоимость: $totalPrice руб."
        }
    }
}