package com.example.itrysohard.justactivity.menu

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
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
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.itrysohard.MyApplication
import com.example.itrysohard.R
import com.example.itrysohard.databinding.ActivityDishDetailBinding
import com.example.itrysohard.justactivity.RegAuthActivity
import com.example.itrysohard.model.CurrentUser
import com.example.itrysohard.model.DishServ
import com.example.itrysohard.retrofitforDU.DishApi
import com.example.itrysohard.retrofitforDU.RetrofitService
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DishDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDishDetailBinding
    private var selectedSize: String? = null
    private lateinit var myApplication: MyApplication
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDishDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)



        myApplication = application as MyApplication


        // Получаем данные из Intent
        val name = intent.getStringExtra("DISH_NAME") ?: "Неизвестное блюдо"
        val description = intent.getStringExtra("DISH_DESCRIPTION") ?: "Описание отсутствует"
        val imageUri = intent.getStringExtra("DISH_IMAGE_URL")
        val price = intent.getDoubleExtra("DISH_PRICE", 0.0)
        val dishId = intent.getIntExtra("DISH_ID", -1)
        val category = intent.getStringExtra("DISH_CATEGORY") ?: "Неизвестная категория"

        if (category != "Напиток") {
            binding.linearLayout.visibility = View.GONE // Скрываем LinearLayout с кнопками
        }

        if (!imageUri.isNullOrEmpty()) {
            Picasso.get()
                .load(imageUri)
                .resize(5000, 5000) // Укажите желаемый размер
                //.centerCrop()
                .into(binding.imgvDishPhoto) // Убедитесь, что у вас есть imgvDishPhoto в layout
        }
        // Устанавливаем данные в UI
        binding.tvDishName.text = name

        binding.tvDishDescription.text = description
        binding.tvDishPrice.text = "$price руб."

        // Настраиваем видимость кнопок для администратора
        val isAdmin = CurrentUser.isAdmin
        binding.btnDelete.visibility = if (isAdmin) View.VISIBLE else View.GONE
        binding.btnEdit.visibility = if (isAdmin) View.VISIBLE else View.GONE
        binding.btnAddToCart.visibility = if (isAdmin) View.GONE else View.VISIBLE

        // Обработчики для кнопок
        setupButtons(dishId, name, description, price, imageUri, category)

        // Настраиваем кнопки размеров
        setupSizeButtons()
    }

    private fun setupButtons(dishId: Int, name: String, description: String, price: Double, imageUri: String?, category: String) {
        binding.btnDelete.setOnClickListener {
            if (dishId != -1) {
                deleteDish(dishId)
            } else {
                showToast("Некорректный идентификатор блюда")
            }
        }

        binding.btnEdit.setOnClickListener {
            if (dishId != -1) {
                val intent = Intent(this, AddDishActivity::class.java).apply {
                    putExtra("dish_id", dishId)
                    putExtra("dish_name", name)
                    putExtra("dish_description", description)
                    putExtra("dish_price", price)
                    putExtra("dish_image_uri", imageUri)
                }
                startActivity(intent)
                finish()
            } else {
                showToast("Некорректный идентификатор блюда")
            }
        }

        binding.btnAddToCart.setOnClickListener {
            val dishToAdd = DishServ(
                id = dishId,
                name = name,
                description = description,
                price = price,
                imageUrl = imageUri,
                category = category
            )
            addToCart(dishToAdd)
        }
    }

    private fun setupSizeButtons() {
        binding.btnSizeS.setOnClickListener { selectSizeButton(binding.btnSizeS, "S") }
        binding.btnSizeM.setOnClickListener { selectSizeButton(binding.btnSizeM, "M") }
        binding.btnSizeL.setOnClickListener { selectSizeButton(binding.btnSizeL, "L") }
    }

    private fun selectSizeButton(selectedButton: Button, size: String) {
        resetButtonStyles()
        selectedSize = size
        updateButtonStyle(selectedButton, size)
    }

    private fun updateButtonStyle(button: Button, size: String) {
        val spannable = SpannableString(size).apply {
            setSpan(UnderlineSpan(), 0, size.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(StyleSpan(Typeface.BOLD), 0, size.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        button.text = spannable
        button.setTextColor(Color.WHITE)
        button.setBackgroundColor(Color.parseColor("#FF5722"))
    }

    private fun resetButtonStyles() {
        val buttons = listOf(binding.btnSizeS, binding.btnSizeM, binding.btnSizeL)
        for (button in buttons) {
            button.setTextColor(Color.BLACK)
            button.setBackgroundColor(Color.parseColor("#707070"))
            button.setTypeface(null, Typeface.NORMAL)
        }
    }

    private fun addToCart(dish: DishServ) {
        val app = application as MyApplication
        Log.d("MyLog", "Selected size before adding: $selectedSize")

        // Проверяем, выбран ли размер для напитков
        if (dish.category == "Напиток" && selectedSize == null) {
            showToast("Пожалуйста, выберите размер перед добавлением напитка в корзину.")
            return
        }

        if (CurrentUser.user == null) {
            // Show dialog if not authorized
            showAuthorizationDialog(dish)
            return
        }

        // Сохраняем выбранный размер в словаре по id блюда
        selectedSize?.let { size ->
            app.selectedSizes[dish.id!!] = size
        }

        // Добавляем блюдо в корзину
        app.cartItems.add(dish)
        myApplication.cartItemCount += 1

        // Уведомляем пользователя
        showToast("Блюдо добавлено в корзину. Текущая корзина: ${app.cartItems.size} предметов.")
    }
    private fun showAuthorizationDialog(dish: DishServ) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Необходимо авторизоваться")
        builder.setMessage("Вы не авторизованы. Пожалуйста, авторизуйтесь или вернитесь к просмотру меню.")

        builder.setPositiveButton("Авторизоваться") { _, _ ->
            // Redirect to RegAuthActivity
            val intent = Intent(this, RegAuthActivity::class.java)
            startActivity(intent)
            finish()
        }

        builder.setNegativeButton("Отмена") { dialog, _ ->
            dialog.dismiss() // Close the dialog
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun deleteDish(id: Int) {
        val retrofitService = RetrofitService()
        val dishApi = retrofitService.getRetrofit().create(DishApi::class.java)
        val call: Call<Void> = dishApi.deleteDish(id)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    showToast("Блюдо успешно удалено")
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    showToast("Ошибка при удалении блюда: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                showToast("Ошибка сети: ${t.message}")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}