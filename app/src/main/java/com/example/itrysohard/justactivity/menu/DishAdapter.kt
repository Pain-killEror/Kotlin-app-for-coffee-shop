package com.example.itrysohard.justactivity.menu

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.marginBottom
import androidx.recyclerview.widget.RecyclerView
import com.example.itrysohard.MyApplication
import com.example.itrysohard.R
import com.example.itrysohard.justactivity.MainPage.StartActivity
import com.example.itrysohard.jwt.JWTDecoder
import com.example.itrysohard.model.CurrentUser
import com.example.itrysohard.model.DishServ
import com.example.itrysohard.retrofitforDU.DishApi
import com.example.itrysohard.retrofitforDU.RetrofitService
import com.example.itrysohard.retrofitforDU.UserApi
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DishAdapter(private val activity: MenuActivity, private val onDishClick: (DishServ) -> Unit) : RecyclerView.Adapter<DishAdapter.DishViewHolder>() {
    private val dishes = mutableListOf<DishServ>()


    fun setDishes(newDishes: List<DishServ>) {
        dishes.clear()
        dishes.addAll(newDishes)
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
        holder.bind(dishes[position])
    }

    override fun getItemCount(): Int = dishes.size

    class DishViewHolder(itemView: View, private val activity: MenuActivity, private val onDishClick: (DishServ) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        private val dishImage: ImageView = itemView.findViewById(R.id.ivDishImage)
        private val dishName: TextView = itemView.findViewById(R.id.tvDishName)
        private var dishPrice: TextView = itemView.findViewById(R.id.tvDishPrice)
        private val btnPlus: ImageButton = itemView.findViewById(R.id.btnPlus)
        private val btnMinus: ImageButton = itemView.findViewById(R.id.btnMinus)
        private val dishCount: TextView = itemView.findViewById(R.id.tvItomCount)
        private var itemCount = 1
        private val btnAddToCart: ImageButton = itemView.findViewById(R.id.btnAddToCart)
        private var selectedSize = "S"
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        private val btnInformation: ImageButton = itemView.findViewById(R.id.btnInformation)
        private val dishDiscount: TextView = itemView.findViewById(R.id.tvDishDiscount)

        private lateinit var dishApi: DishApi


        fun bind(dish: DishServ) {
            Picasso.get()
                .load(dish.photo)
                .resize(500, 500)
                .centerInside()
                .into(dishImage)


            // Рассчитываем сумму скидки
            val discountAmount = dish.price * (dish.discount / 100.0)

            // Рассчитываем итоговую сумму
            val finalPrice = dish.price - discountAmount

            dishName.text = dish.name
            dishDiscount.text = "${dish.discount} %"

            if(dish.discount <= 0){
                val params = dishPrice.layoutParams as ViewGroup.MarginLayoutParams
                dishDiscount.visibility = View.GONE
                params.bottomMargin = 20
                dishPrice.layoutParams = params

                dishPrice.text = "${dish.price} р."
            }
            else{
                dishDiscount.visibility = View.VISIBLE
                val params = dishPrice.layoutParams as ViewGroup.MarginLayoutParams
                params.bottomMargin = 0
                dishPrice.layoutParams = params

                dishPrice.text = "${finalPrice} р."
            }



            btnEdit.visibility = if (isAdmin()) View.VISIBLE else View.GONE


            btnEdit.setOnClickListener {
                if (dish.id != -1) {
                    val intent = Intent(activity, AddDishActivity::class.java).apply {
                        putExtra("dish_id", dish.id)
                        putExtra("dish_name", dish.name)
                        putExtra("dish_description", dish.description)
                        putExtra("dish_price", dish.price)
                        putExtra("dish_image_uri", dish.photo)
                        putExtra("dish_category", dish.category)
                        putExtra("dish_discount", dish.discount)
                    }
                    activity.startActivity(intent)

                } else {
                    showToast(activity, "Некорректный идентификатор блюда")
                }
            }

            btnInformation.setOnClickListener{
                showInformationDialog(dish.description, dish.name)
            }

            if(dish.category == "Напиток") {
                dishCount.text = "S"
                btnPlus.setOnClickListener {
                    if (itemCount < 3) itemCount++
                    when (itemCount) {
                        1 -> {
                            dishCount.text = "S"
                            selectedSize = "S"
                        }

                        2 -> {
                            dishCount.text = "M"
                            selectedSize = "M"
                        }

                        3 -> {
                            dishCount.text = "L"
                            selectedSize = "L"
                        }
                    }
                }

                btnMinus.setOnClickListener {
                    if (itemCount > 1) itemCount--
                    when (itemCount) {
                        1 -> {
                            dishCount.text = "S"
                            selectedSize = "S"
                        }

                        2 -> {
                            dishCount.text = "M"
                            selectedSize = "M"
                        }

                        3 -> {
                            dishCount.text = "L"
                            selectedSize = "L"
                        }
                    }
                }
                btnAddToCart.setOnClickListener {
                    addToCartDrink(dish, itemView.context, selectedSize)
                    activity.updateCartCountDisplay()
                }

            } else {

                itemCount = 1
                dishCount.text = itemCount.toString()

                btnPlus.setOnClickListener {
                    itemCount++
                    dishCount.text = itemCount.toString()
                }
                btnMinus.setOnClickListener {
                    if (itemCount > 1) {
                        itemCount--
                        dishCount.text = itemCount.toString()
                    }
                }

                btnAddToCart.setOnClickListener {
                    addToCartDish(dish, itemView.context, itemCount)
                    activity.updateCartCountDisplay()
                    itemCount = 1
                    dishCount.text = itemCount.toString()
                }
            }
        }

        private fun isAdmin(): Boolean {
            val prefs = itemView.context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
            val accessToken = prefs.getString("ACCESS_TOKEN", null)
            return JWTDecoder.getRole(accessToken) == "ROLE_ADMIN"
        }

        private fun addToCartDish(dish: DishServ, context: Context, count: Int) {
            val app = context.applicationContext as MyApplication
            repeat(count) {
                app.cartItems.add(dish)
            }
            app.cartItemCount += count
        }

        private fun addToCartDrink(dish: DishServ, context: Context, size: String) {
            val app = context.applicationContext as MyApplication
            selectedSize?.let { size ->
                app.selectedSizes[dish.id!!] = size
            }
            app.cartItems.add(dish)
            app.cartItemCount += 1
        }

        private fun showInformationDialog(description: String, name: String) {
            val builder = AlertDialog.Builder(activity, R.style.CustomAlertDialog)
            val inflater = activity.layoutInflater
            val dialogView = inflater.inflate(R.layout.dialog_dish_info, null)
            builder.setView(dialogView)

            val tvDescription: TextView = dialogView.findViewById(R.id.tvDescription)
            val btnClose: Button = dialogView.findViewById(R.id.btnClose)
            val tvDishName: TextView = dialogView.findViewById(R.id.tvDishName)
            tvDescription.text = description
            tvDishName.text = name
            val dialog = builder.create()

            btnClose.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }

        // Метод showToast для отображения сообщений
        private fun showToast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
