package com.example.itrysohard.justactivity.menu

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.itrysohard.BackPress.ActivityHistoryImpl
import com.example.itrysohard.BackPress.BackPressManager
import com.example.itrysohard.MyApplication
import com.example.itrysohard.databinding.ActivityMenuBinding
import com.example.itrysohard.justactivity.PersonalPage.PersAccActivity
import com.example.itrysohard.justactivity.RegistrationAuthentication.RegAuthActivity
import com.example.itrysohard.justactivity.MainPage.StartActivity
import com.example.itrysohard.justactivity.menu.cart.CartActivity
import com.example.itrysohard.jwt.JWTDecoder
import com.example.itrysohard.jwt.SharedPrefTokenManager
import com.example.itrysohard.justactivity.helpfull.CurrentUser
import com.example.itrysohard.model.DishServ
import com.example.itrysohard.retrofitforDU.DishApi
import com.example.itrysohard.retrofitforDU.RetrofitService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding
    private lateinit var breakfastAdapter: DishAdapter
    private lateinit var dessertAdapter: DishAdapter
    private lateinit var drinkAdapter: DishAdapter
    private lateinit var myApplication: MyApplication
    private var cartCount = 0
    private var selectedButtonId: Int? = null


    companion object {
        private const val REQUEST_CODE_ADD_DISH = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityHistoryImpl.addActivity(this::class.java)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)



        // Настройка адаптеров
        breakfastAdapter = DishAdapter(this) { dish -> onDishSelected(dish) }
        dessertAdapter = DishAdapter(this) { dish -> onDishSelected(dish) }
        drinkAdapter = DishAdapter(this) { dish -> onDishSelected(dish) }

        // Подключение RecyclerView
        setupRecyclerViews()

        // Установка начальной категории
        selectCategory("Завтрак")

        // Проверка прав администратора и установка видимости кнопки добавления блюда
        val isAdmin = CurrentUser.isAdmin
        binding.btnAddDish.visibility = if (isAdmin) View.VISIBLE else View.GONE

        // Переход на AddDishActivity
        binding.btnAddDish.setOnClickListener {
            startActivityForResult(Intent(this, AddDishActivity::class.java), REQUEST_CODE_ADD_DISH)
        }

        // Установка слушателей для кнопок категорий
        setupCategoryButtons()

        // Инициализация и обновление счётчика
        cartCount = (application as MyApplication).cartItems.size
        updateCartCount(cartCount)

        // Загружаем данные о блюдах
        loadDishes()

        // Обработчики для навигации
        setupNavigationButtons()

        myApplication = application as MyApplication
        updateCartCountDisplay()
    }

    override fun onBackPressed() {
        BackPressManager.handleBackPress(this) {
            super.onBackPressed()
        }
    }

    fun updateCartCountDisplay() {
        binding.tvCartCount.text = myApplication.cartItemCount.toString()
    }

    private fun setupRecyclerViews() {
        binding.recyclerViewBreakfast.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewBreakfast.adapter = breakfastAdapter
        binding.recyclerViewDesserts.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewDesserts.adapter = dessertAdapter
        binding.recyclerViewDrinks.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewDrinks.adapter = drinkAdapter

        val bottomSpacingInPx = (85 * resources.displayMetrics.density).toInt()
        val topSpacingInPx = (80 * resources.displayMetrics.density).toInt()

        binding.recyclerViewBreakfast.addItemDecoration(SpacingItemDecoration(topSpacingInPx, bottomSpacingInPx))
        binding.recyclerViewDesserts.addItemDecoration(SpacingItemDecoration(topSpacingInPx, bottomSpacingInPx))
        binding.recyclerViewDrinks.addItemDecoration(SpacingItemDecoration(topSpacingInPx, bottomSpacingInPx))
    }

    private fun setupCategoryButtons() {
        binding.btnBreakfast.setOnClickListener {
            selectCategory("Завтрак")
            binding.textView2.text = "Завтраки"
        }

        binding.btnDessert.setOnClickListener {
            selectCategory("Десерт")
            binding.textView2.text = "Десерты"
        }

        binding.btnDrink.setOnClickListener {
            selectCategory("Напиток")
            binding.textView2.text = "Напитки"
        }
    }

    private fun selectCategory(category: String) {
        showRecyclerView(category)
        animateSidebarSelection(category)

        when (category) {
            "Завтрак" -> binding.recyclerViewBreakfast.scrollToPosition(0)
            "Десерт" -> binding.recyclerViewDesserts.scrollToPosition(0)
            "Напиток" -> binding.recyclerViewDrinks.scrollToPosition(0)
        }
    }

    private fun animateSidebarSelection(category: String) {
        // Сброс стиля предыдущей кнопки
        selectedButtonId?.let {
            val previousButton = findViewById<Button>(it)
            resetButtonStyle(previousButton)
        }

        // Выбор текущей кнопки
        when (category) {
            "Завтрак" -> selectButton(binding.btnBreakfast, "Завтраки")
            "Завтрак" -> binding.textView2.text = "Завтраки"
            "Десерт" -> selectButton(binding.btnDessert, "Десерты")
            "Десерт" -> binding.textView2.text = "Десерты"
            "Напиток" -> selectButton(binding.btnDrink, "Напитки")
            "Напиток" -> binding.textView2.text = "Напитки"
        }
    }

    private fun selectButton(selectedButton: Button, text: String) {
        // Сброс состояния предыдущей кнопки
        selectedButtonId?.let { previousId ->
            val previousButton = findViewById<Button>(previousId)
            resetButtonStyle(previousButton)
        }

        // Установка состояния для текущей кнопки
        val spannable = SpannableString(text)
        spannable.setSpan(UnderlineSpan(), 1, text.length-1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(StyleSpan(Typeface.BOLD), 0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        selectedButton.text = spannable // Установка нового текста с подчеркиванием
        selectedButtonId = selectedButton.id // Сохранение текущего состояния
    }

    private fun resetButtonStyle(button: Button) {
        button.setTypeface(null, Typeface.NORMAL) // Сброс на нормальный стиль
        button.text = button.text.toString().removeSuffix(" ") // Убираем подчеркивание
    }

    private fun showRecyclerView(category: String) {
        binding.recyclerViewBreakfast.visibility = if (category == "Завтрак") View.VISIBLE else View.GONE
        binding.recyclerViewDesserts.visibility = if (category == "Десерт") View.VISIBLE else View.GONE
        binding.recyclerViewDrinks.visibility = if (category == "Напиток") View.VISIBLE else View.GONE
    }

    private fun setupNavigationButtons() {
        binding.btnHome.setOnClickListener {
            startActivity(Intent(this, StartActivity::class.java))
            finish()
        }

        binding.btnMenu.setOnClickListener {
            Toast.makeText(this, "Вы уже в меню!", Toast.LENGTH_SHORT).show()
            onResume()
        }

        binding.btnCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        binding.btnPersAcc.setOnClickListener {
            showAuthorizationDialogPers()
        }
    }

    private fun showAuthorizationDialogPers() {

        val tokenManager = SharedPrefTokenManager(this)
        val accessToken = tokenManager.getAccessToken()

        val isAuthorized = accessToken != null &&
                !JWTDecoder.isExpired(accessToken) &&
                JWTDecoder.getRole(accessToken) != null

        if (!isAuthorized) {
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
    }



    private fun loadDishes() {
        GlobalScope.launch(Dispatchers.Main) {
            val tokenManager = SharedPrefTokenManager(this@MenuActivity)
            val retrofitService = RetrofitService(this@MenuActivity, tokenManager)
            val dishApi = retrofitService.getRetrofit().create(DishApi::class.java)

            try {
                val response = withContext(Dispatchers.IO) {
                    dishApi.getAllDishes().execute()
                }

                if (response.isSuccessful) {
                    response.body()?.let { dishes ->
                        val breakfasts = dishes.filter { it.category == "Завтрак" }
                        val desserts = dishes.filter { it.category == "Десерт" }
                        val drinks = dishes.filter { it.category == "Напиток" }

                        withContext(Dispatchers.Main) {
                            breakfastAdapter.setDishes(breakfasts)
                            dessertAdapter.setDishes(desserts)
                            drinkAdapter.setDishes(drinks)
                            updateCartCountDisplay()
                        }
                    }
                } else {
                    Toast.makeText(this@MenuActivity, "Ошибка загрузки меню: ${response.message()}", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@MenuActivity, StartActivity::class.java))
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MenuActivity, "Ошибка сети: ${e.message}", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@MenuActivity, StartActivity::class.java))
                finish()
            }
        }
    }

    private fun onDishSelected(dish: DishServ) {
        val intent = Intent(this, DishDetailActivity::class.java)
        intent.putExtra("DISH_NAME", dish.name)
        intent.putExtra("DISH_DESCRIPTION", dish.description)
        intent.putExtra("DISH_IMAGE_URL", dish.photo)
        intent.putExtra("DISH_PRICE", dish.price)
        intent.putExtra("DISH_CATEGORY", dish.category)
        intent.putExtra("DISH_ID", dish.id)
        intent.putExtra("DISH_DISCOUNT", dish.discount)
        Log.d("MyLog", "Dish ID: ${dish.id}")
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_DISH && resultCode == Activity.RESULT_OK) {
            val updatedCartCount = data?.getIntExtra("cart_count", 0) ?: 0
            cartCount = updatedCartCount
            updateCartCount(cartCount)
            loadDishes()
        }
    }

    private fun updateCartCount(count: Int) {
        // Обновите количество в корзине, если необходимо
    }


    private fun isAdmin(): Boolean {
        val prefs = getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        val accessToken = prefs.getString("ACCESS_TOKEN", null)
        val role = JWTDecoder.getRole(accessToken)
        return JWTDecoder.getRole(accessToken) == "ROLE_ADMIN"
    }

    private fun isModerator(): Boolean {
        val prefs = getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        val accessToken = prefs.getString("ACCESS_TOKEN", null)
        val role = JWTDecoder.getRole(accessToken)
        return JWTDecoder.getRole(accessToken) == "ROLE_MODERATOR"
    }

    override fun onResume() {
        super.onResume()
        val isAdmin = isAdmin()
        // Обновите адаптеры

        // Перезагрузите данные
        loadDishes()
        cartCount = (application as MyApplication).cartItems.size
        updateCartCount(cartCount)
        binding.btnAddDish.visibility = if (isAdmin()||isModerator()) View.VISIBLE else View.GONE
        breakfastAdapter.refresh()
        dessertAdapter.refresh()
        drinkAdapter.refresh()

    }

    
}