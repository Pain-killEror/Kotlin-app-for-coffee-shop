package com.example.itrysohard.justactivity.menu.cart

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.itrysohard.BackPress.ActivityHistoryImpl
import com.example.itrysohard.BackPress.BackPressManager
import com.example.itrysohard.MyApplication
import com.example.itrysohard.databinding.ActivityCartBinding
import com.example.itrysohard.justactivity.MainPage.StartActivity
import com.example.itrysohard.justactivity.PersonalPage.PersAccActivity
import com.example.itrysohard.justactivity.RegistrationAuthentication.RegAuthActivity
import com.example.itrysohard.justactivity.menu.MenuActivity
import com.example.itrysohard.model.CurrentUser
import com.example.itrysohard.model.DishServ

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var cartAdapter: CartAdapter
    private var cartItems = mutableListOf<DishServ>()
    private lateinit var myApplication: MyApplication
    private lateinit var selectedSizes: MutableMap<Int, String?> // Изменено на MutableMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityHistoryImpl.addActivity(this::class.java)
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

        binding.btnHome.setOnClickListener {
            startActivity(Intent(this, StartActivity::class.java))
            finish()
        }

        binding.btnMenu.setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }

        binding.btnCart.setOnClickListener {
            Toast.makeText(this, "Вы на экране корзины!", Toast.LENGTH_SHORT).show()

        }

        binding.btnPersAcc.setOnClickListener {
            showAuthorizationDialogPers()
        }

    }

    override fun onBackPressed() {
        BackPressManager.handleBackPress(this) {
            super.onBackPressed()
        }
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
            val totalPrice = cartItems.sumOf {
                it.price.toDouble() * (1 - it.discount.toDouble() / 100)
            }
            binding.tvTotalPrice.text = "Итоговая стоимость: $totalPrice руб."
        }
    }
}