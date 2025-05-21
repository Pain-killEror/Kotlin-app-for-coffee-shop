package com.example.itrysohard.justactivity.menu.cart
import android.content.Context
import retrofit2.Callback
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
import com.example.itrysohard.justactivity.about_us.LeaveReviewActivity
import com.example.itrysohard.justactivity.helpfull.CartCount
import com.example.itrysohard.justactivity.menu.MenuActivity
import com.example.itrysohard.justactivity.helpfull.CurrentUser
import com.example.itrysohard.jwt.JWTDecoder
import com.example.itrysohard.jwt.SharedPrefTokenManager
import com.example.itrysohard.model.DishServ
import com.example.itrysohard.model.Order
import com.example.itrysohard.retrofitforDU.OrderApi
import com.example.itrysohard.retrofitforDU.RetrofitService
import retrofit2.Call
import retrofit2.Response

class CartActivity : CartCount() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var cartAdapter: CartAdapter
    private var cartItems = mutableListOf<DishServ>()

    private lateinit var selectedSizes: MutableMap<Long, String> // Изменено на MutableMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityHistoryImpl.addActivity(this::class.java)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateCartCountDisplay(binding.tvCartCount)

        cartItems = myApplication.cartItems
        selectedSizes = myApplication.selectedSizes// Получаем выбранные размеры как изменяемую карту

        setupRecyclerView()

        // Обновляем итоговую стоимость
        updateTotalPrice()

        binding.btnCheckout.setOnClickListener {
            showAuthorizationDialog()

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

    private fun showAuthorizationDialog() {
        val prefs = getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        val accessToken = prefs.getString("ACCESS_TOKEN", null)
        val refreshToken = prefs.getString("REFRESH_TOKEN", null)
        val tokenManager = SharedPrefTokenManager(this)


        val isAuthorized = accessToken != null &&
                !JWTDecoder.isExpired(accessToken) &&
                JWTDecoder.getRole(accessToken) != null


        val isAdminModerator = accessToken != null &&
                !JWTDecoder.isExpired(accessToken) &&
                JWTDecoder.getRole(accessToken) == "ROLE_ADMIN" || JWTDecoder.getRole(accessToken) == "ROLE_MODERATOR"

        if (isAdminModerator) {
            AlertDialog.Builder(this).apply {
                setTitle("Вы администрация")
                setMessage("Ваш аккаунт является аккаунтом администрации на неопределенный срок, вы не можете сделать заказ!")
                setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
            }.create().show()
        }



        if (!isAuthorized && !isAdminModerator) {
            AlertDialog.Builder(this).apply {
                setTitle("Требуется авторизация")
                setMessage("Для выполнения действия необходима авторизация")
                setPositiveButton("Войти") { _, _ ->
                    startActivity(Intent(context, RegAuthActivity::class.java))
                }
                setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
            }.create().show()
        } else if(isAuthorized && !isAdminModerator){
            Checkout()
        }
    }

    private fun Checkout(){
        val tokenManager = SharedPrefTokenManager(this)
        val accessToken = tokenManager.getAccessToken()

        if (accessToken.isNullOrEmpty()) {
            showToast("Для оформления заказа требуется авторизация")
            startActivity(Intent(this, RegAuthActivity::class.java))
            return
        }

        // Получаем список ID блюд из корзины
        val dishIds = cartItems.map { it.id!!.toLong()} // Предполагается, что DishServ.id совместим с Long

        // Создаем запрос
        val orderRequest = Order(dishIds)

        // Отправляем запрос
        val retrofit = RetrofitService(this, tokenManager).getRetrofit()
        val orderApi = retrofit.create(OrderApi::class.java)

        orderApi.createOrder("Bearer $accessToken", orderRequest).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    clearCart()
                    showToast("Заказ успешно севершен!")
                } else {
                    val errorMessage = when (response.code()) {
                        400 -> "Ошибка: Некорректные данные заказа"
                        401 -> "Ошибка авторизации"
                        404 -> "Блюдо не найдено"
                        else -> "Ошибка сервера: ${response.code()}"
                    }
                    showToast(errorMessage)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                showToast("Ошибка сети: ${t.message}")
            }
        })
    }
    private fun showToast( message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        BackPressManager.handleBackPress(this) {
            super.onBackPressed()
        }
    }

    private fun showAuthorizationDialogPers() {
        val prefs = getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        val accessToken = prefs.getString("ACCESS_TOKEN", null)
        val refreshToken = prefs.getString("REFRESH_TOKEN", null)
        val tokenManager = SharedPrefTokenManager(this)


        val isAuthorized = accessToken != null &&
                !JWTDecoder.isExpired(accessToken) &&
                JWTDecoder.getRole(accessToken) != null


        if (!isAuthorized) {
            AlertDialog.Builder(this).apply {
                setTitle("Доступ ограничен")
                setMessage("Для входа в личный кабинет требуется авторизация")
                setPositiveButton("Войти") { _, _ ->
                    startActivity(Intent(context, RegAuthActivity::class.java))
                }
                setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
            }.create().show()
        } else {
            startActivity(Intent(this, PersAccActivity::class.java))
        }
    }

    private fun clearCart() {
        cartItems.clear() // Очищаем список корзины
        myApplication.cartItemCount = 0 // Сбрасываем счетчик товаров в корзине
        selectedSizes.clear() // Очищаем выбранные размеры
        cartAdapter.setDishes(cartItems) // Обновляем адаптер
        updateTotalPrice() // Обновляем итоговую стоимость
        updateCartCountDisplay(binding.tvCartCount)
    }

    private fun setupRecyclerView() {
        binding.recyclerViewCart.layoutManager = LinearLayoutManager(this)

        // Преобразование Map<Long, String> в Map<Int, String?>
        val convertedSizes = selectedSizes.mapKeys { it.key.toInt() }.mapValues { it.value }

        cartAdapter = CartAdapter(cartItems, convertedSizes) { dish -> removeFromCart(dish) }
        binding.recyclerViewCart.adapter = cartAdapter
    }

    private fun removeFromCart(dish: DishServ) {
        val index = cartItems.indexOf(dish)
        if (index != -1) {
            cartItems.removeAt(index)
            myApplication.cartItemCount -= 1
            dish.id?.let { selectedSizes.remove(it.toLong()) } // Преобразование Int? в Long
            cartAdapter.setDishes(cartItems)
            updateTotalPrice()
            updateCartCountDisplay(binding.tvCartCount)
        }
    }

    private fun updateTotalPrice() {
        if (cartItems.isEmpty()) {
            binding.tvTotalPrice.text = "Корзина пуста"
        } else {
            val totalPrice = cartItems.sumOf {
                it.price.toDouble() * (1 - it.discount.toDouble() / 100)
            }
            // Округление до 2-х знаков и форматирование
            val formattedPrice = "%.2f".format(totalPrice)
            binding.tvTotalPrice.text = "Итоговая стоимость: $formattedPrice руб."
        }
    }
}