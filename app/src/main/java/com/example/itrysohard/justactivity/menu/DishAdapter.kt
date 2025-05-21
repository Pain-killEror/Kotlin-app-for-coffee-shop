package com.example.itrysohard.justactivity.menu

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.itrysohard.MyApplication
import com.example.itrysohard.R
import com.example.itrysohard.jwt.JWTDecoder
import com.example.itrysohard.model.DishServ
import com.example.itrysohard.retrofitforDU.DishApi
import com.squareup.picasso.Picasso
import java.util.Locale
import kotlin.math.roundToInt

class DishAdapter(
    private val activity: MenuActivity,
    private val onDishClick: (DishServ) -> Unit
) : RecyclerView.Adapter<DishAdapter.DishViewHolder>() {

    private val groupedDishes = mutableListOf<List<DishServ>>()
    private val volumeRegex = Regex("""(\d+[\.,]?\d*)""")

    fun setDishes(newDishes: List<DishServ>) {
        groupedDishes.clear()

        // Группируем блюда по имени и сортируем по объему
        newDishes.groupBy { it.name }.values.forEach { group ->
            val sortedGroup = group.sortedBy { extractVolumeValue(it.volume) }
            groupedDishes.add(sortedGroup)
        }

        notifyDataSetChanged()
    }

    fun refresh() {
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dish, parent, false)
        return DishViewHolder(view, activity, onDishClick)
    }

    override fun onBindViewHolder(holder: DishViewHolder, position: Int) {
        holder.bind(groupedDishes[position])
    }

    override fun getItemCount(): Int = groupedDishes.size

    private fun extractVolumeValue(volumeStr: String): Double {
        val match = volumeRegex.find(volumeStr)?.value?.replace(",", ".")
        return match?.toDoubleOrNull() ?: 0.0
    }

    class DishViewHolder(
        itemView: View,
        private val activity: MenuActivity,
        private val onDishClick: (DishServ) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val dishImage: ImageView = itemView.findViewById(R.id.ivDishImage)
        private val dishName: TextView = itemView.findViewById(R.id.tvDishName)
        private val dishPrice: TextView = itemView.findViewById(R.id.tvDishPrice)
        private val btnPlus: ImageButton = itemView.findViewById(R.id.btnPlus)
        private val btnMinus: ImageButton = itemView.findViewById(R.id.btnMinus)
        private val dishCount: TextView = itemView.findViewById(R.id.tvItomCount)
        private val btnAddToCart: ImageButton = itemView.findViewById(R.id.btnAddToCart)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        private val btnInformation: ImageButton = itemView.findViewById(R.id.btnInformation)
        private val dishDiscount: TextView = itemView.findViewById(R.id.tvDishDiscount)

        private var currentGroup: List<DishServ> = emptyList()
        private var currentIndex = 0
        private var itemCount = 1

        fun bind(group: List<DishServ>) {
            currentGroup = group
            currentIndex = 0
            val mainDish = group.first()

            // Загрузка изображения
            Picasso.get()
                .load(mainDish.photo)
                .resize(500, 500)
                .centerInside()
                .into(dishImage)

            dishName.text = mainDish.name
            updateUI()

            // Настройка кнопки редактирования
            btnEdit.visibility = if (isAdmin()||isModerator()) View.VISIBLE else View.GONE
            btnEdit.setOnClickListener { handleEditClick(mainDish) }

            // Настройка кнопки информации
            btnInformation.setOnClickListener {
                showInformationDialog(mainDish.description, mainDish.name)
            }

            // Обработчики кнопок объема
            btnPlus.setOnClickListener {
                if (currentIndex < currentGroup.size - 1) {
                    currentIndex++
                    updateUI()
                }
            }

            btnMinus.setOnClickListener {
                if (currentIndex > 0) {
                    currentIndex--
                    updateUI()
                }
            }

            // Обработчик добавления в корзину
            btnAddToCart.setOnClickListener {
                val selectedDish = currentGroup[currentIndex]
                if (selectedDish.category == "Напиток") {
                    addToCartDrink(selectedDish, itemCount)
                } else {
                    addToCartDish(selectedDish, itemCount)
                }
                activity.updateCartCountDisplay()
            }
        }

        private fun updateUI() {
            val dish = currentGroup[currentIndex]

            // Рассчет цены со скидкой


            // Обновление отображения цены
            val price = dish.price.toDouble()
            val discount = dish.discount.toDouble()

            if (discount <= 0) {
                dishDiscount.visibility = View.GONE
                dishPrice.text = "%.2f р.".format(price)
            } else {
                val discountAmount = price * (discount / 100.0)
                val finalPrice = price - discountAmount
                dishDiscount.visibility = View.VISIBLE
                dishDiscount.text = "${discount.toInt()}%"
                dishPrice.text = "%.2f р.".format(finalPrice)
            }

            // Обновление отображения объема
            dishCount.text = when {
                currentGroup.size == 1 -> dish.volume
                dish.category == "Напиток" -> when (currentIndex) {
                    0 -> "S"
                    1 -> "M"
                    else -> "L"
                }
                else -> dish.volume
            }

            // Управление видимостью кнопок
            btnPlus.visibility = if (currentGroup.size > 1) View.VISIBLE else View.GONE
            btnMinus.visibility = if (currentGroup.size > 1) View.VISIBLE else View.GONE
            btnPlus.isEnabled = currentIndex < currentGroup.size - 1
            btnMinus.isEnabled = currentIndex > 0
        }

        private fun handleEditClick(mainDish: DishServ) {
            val allVariants = currentGroup
            val intent = Intent(activity, AddDishActivity::class.java).apply {
                putExtra("dish_id", mainDish.id) // Основной ID (берем первый)
                putExtra("dish_name", mainDish.name)
                putExtra("dish_description", mainDish.description)
                putExtra("dish_category", mainDish.category)
                putExtra("dish_discount", mainDish.discount)
                putExtra("dish_image_uri", mainDish.photo) // URI изображения

                // Передаем все варианты объемов и цен
                putStringArrayListExtra("volumes", ArrayList(allVariants.map { it.volume }))
                putStringArrayListExtra("prices", ArrayList(allVariants.map { it.price.toString() }))

                // Передаем все ID вариантов
                putIntegerArrayListExtra("dish_ids", ArrayList(allVariants.map { it.id!!.toInt() }))
            }
            activity.startActivity(intent)
        }

        private fun addToCartDish(dish: DishServ, count: Int) {
            val app = activity.application as MyApplication
            repeat(count) {
                app.cartItems.add(dish.copy()) // Сохраняем оригинальную цену
            }
            app.cartItemCount += count
        }

        private fun addToCartDrink(dish: DishServ, count: Int) {
            val app = activity.application as MyApplication
            val selectedSize = when (currentIndex) {
                0 -> "S"
                1 -> "M"
                else -> "L"
            }

            dish.id?.let { app.selectedSizes[it.toLong()] = selectedSize }
            app.cartItems.add(dish.copy()) // Сохраняем оригинальную цену
            app.cartItemCount += count
        }


        private fun isAdmin(): Boolean {
            val prefs = itemView.context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
            val accessToken = prefs.getString("ACCESS_TOKEN", null)
            return JWTDecoder.getRole(accessToken) == "ROLE_ADMIN"
        }
        private fun isModerator(): Boolean {
            val prefs = itemView.context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
            val accessToken = prefs.getString("ACCESS_TOKEN", null)
            val role = JWTDecoder.getRole(accessToken)
            return JWTDecoder.getRole(accessToken) == "ROLE_MODERATOR"
        }

        private fun showInformationDialog(description: String, name: String) {
            // 1. Создаем Builder
            val builder = AlertDialog.Builder(activity, R.style.CustomAlertDialog)

            // 2. Инфлейтим кастомный layout
            val view = activity.layoutInflater.inflate(R.layout.dialog_dish_info, null)

            // 3. Настраиваем View внутри layout'а
            view.findViewById<TextView>(R.id.tvDescription).text = description
            view.findViewById<TextView>(R.id.tvDishName).text = name
            val btnClose = view.findViewById<Button>(R.id.btnClose) // Получаем кнопку

            // 4. Устанавливаем View для Builder'а
            builder.setView(view)

            // 5. Создаем AlertDialog (но пока не показываем)
            val dialog = builder.create() // Получаем ссылку на созданный диалог

            // 6. Назначаем OnClickListener для кнопки ЗДЕСЬ, используя ссылку на 'dialog'
            btnClose.setOnClickListener {
                dialog.dismiss() // Теперь закрываем правильный диалог
            }

            // 7. Показываем диалог
            dialog.show()
        }

        private fun showToast(message: String) {
            Toast.makeText(itemView.context, message, Toast.LENGTH_SHORT).show()
        }
    }
}