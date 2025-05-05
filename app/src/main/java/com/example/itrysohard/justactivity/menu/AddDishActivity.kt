package com.example.itrysohard.justactivity.menu

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.itrysohard.BackPress.ActivityHistoryImpl
import com.example.itrysohard.databinding.ActivityAddDishBinding
import com.example.itrysohard.justactivity.MainPage.StartActivity
import com.example.itrysohard.jwt.SharedPrefTokenManager
import com.example.itrysohard.model.CurrentUser
import com.example.itrysohard.model.DishServ
import com.example.itrysohard.retrofitforDU.DishApi
import com.example.itrysohard.retrofitforDU.RetrofitService
import com.squareup.picasso.Picasso
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class AddDishActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddDishBinding
    private var selectedImageUri: Uri? = null
    private var selectedCategory: String? = null
    private var dishId: Int? = null // ID редактируемого блюда
    private var isSaving = false
    private lateinit var dishApi: DishApi


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityHistoryImpl.addActivity(this::class.java)
        binding = ActivityAddDishBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinner() // Настройка Spinner

        // Загрузка данных существующего блюда, если редактирование
        val intent = intent
        if (intent.hasExtra("dish_id")) {
            dishId = intent.getIntExtra("dish_id", -1)
            binding.etDishName.setText(intent.getStringExtra("dish_name"))
            binding.etDishDescription.setText(intent.getStringExtra("dish_description"))
            binding.etDishPrice.setText(intent.getByteExtra(("dish_price"), 0).toString())
            val category = intent.getStringExtra("dish_category")

// Если категория не null, попробуем найти её позицию в адаптере спиннера
            if (category != null) {
                val adapter = binding.spinnerDishCategory.adapter as ArrayAdapter<String>
                val position = adapter.getPosition(category)
                binding.spinnerDishCategory.setSelection(position)
            }
            binding.etDishDiscount.setText(intent.getByteExtra(("dish_discount"), 0).toString())

        }
        var imageUriString = intent.getStringExtra("dish_image_uri")
        if (!imageUriString.isNullOrEmpty()) {
            // Преобразуем строку в Uri для существующего изображения
            selectedImageUri = Uri.parse(imageUriString)
            Picasso.get()
                .load(imageUriString)
                .into(binding.ivDishPhoto)
        }

        binding.btnChoosePhoto.setOnClickListener { openGallery() }

        if (dishId == null) binding.btnDelete.visibility = View.GONE
        else binding.btnDelete.visibility = View.VISIBLE

        binding.btnDelete.setOnClickListener{
            showDeleteConfirmationDialog()
        }

        binding.btnSaveDish.setOnClickListener {
            if (isSaving) return@setOnClickListener // Если уже сохраняем, ничего не делаем

            isSaving = true // Устанавливаем, что процесс сохранения начался
                binding.btnSaveDish.isEnabled = false // Отключаем кнопку

            if (dishId == null) {
                saveDish()// Добавляем новое блюдо

            } else {
                updateDish()// Обновляем существующее блюдо

            }
        }
    }

    private fun saveDish() {
        val name = binding.etDishName.text.toString()
        val description = binding.etDishDescription.text.toString()
        val price = binding.etDishPrice.text.toString().toDoubleOrNull()
        val discount = binding.etDishDiscount.text.toString().toIntOrNull() ?: 0

        val priceByte = price!!.toInt().toByte()// убедитесь, что значение в диапазоне -128..127
        val discountByte = discount.toByte()

        if (name.isEmpty() || description.isEmpty() || price == null || selectedImageUri == null || selectedCategory == null) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        // Загрузка изображения как Bitmap
        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImageUri)

        val sizeBeforeCompression = bitmap.byteCount / 1024 // размер в КБ
        Log.d("MyLog", "Размер изображения до сжатия: $sizeBeforeCompression KB")

        // Сохранение Bitmap без метаданных
        val file = File(getExternalFilesDir(null), "image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream) // Сохранение в JPEG без метаданных
        outputStream.flush()
        outputStream.close()


        val sizeAfterCompression = file.length() / 1024 // размер в КБ
        Log.d("MyLog", "Размер изображения после сжатия: $sizeAfterCompression KB")


        // Создание RequestBody для загрузки
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("photo", file.name, requestBody)

        val tokenManager = SharedPrefTokenManager(this)
        val retrofitService = RetrofitService(this, tokenManager)
        val dishApi = retrofitService.getRetrofit().create(DishApi::class.java)

        val call = dishApi.uploadDish(
            name = name.toRequestBody("text/plain".toMediaTypeOrNull()),
            description = description.toRequestBody("text/plain".toMediaTypeOrNull()),
            price = priceByte.toString().toRequestBody("text/plain".toMediaTypeOrNull()), // отправляем как Byte
            category = selectedCategory!!.toRequestBody("text/plain".toMediaTypeOrNull()),
            photo = imagePart,
            discount = discountByte.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        )

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AddDishActivity, "Блюдо добавлено!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Неизвестная ошибка"
                    Toast.makeText(this@AddDishActivity, "Ошибка: $errorMessage", Toast.LENGTH_SHORT).show()
                }
                isSaving = false // Сбрасываем состояние
                binding.btnSaveDish.isEnabled = true // Включаем кнопку снова
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                isSaving = false // Сбрасываем состояние
                binding.btnSaveDish.isEnabled = true // Включаем кнопку снова
                Toast.makeText(this@AddDishActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateDish() {
        val name = binding.etDishName.text.toString()
        val description = binding.etDishDescription.text.toString()
        val price = binding.etDishPrice.text.toString().toDoubleOrNull()
        val discount = binding.etDishDiscount.text.toString().toIntOrNull() ?: 0

        // Проверка обязательных полей
        if (name.isEmpty() || description.isEmpty() || price == null || selectedCategory == null || selectedImageUri == null) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверяем, является ли изображение веб-ссылкой
        if (selectedImageUri!!.scheme?.startsWith("http") == true) {
            val tokenManager = SharedPrefTokenManager(this)
            val retrofitService = RetrofitService(this, tokenManager)
            val dishApi = retrofitService.getRetrofit().create(DishApi::class.java)
            val priceByte = price.toInt().coerceIn(-128..127).toByte()
            val discountByte = discount.toByte()
            dishApi.updateDish(
                id = dishId!!.toLong(),
                name = name.toRequestBody("text/plain".toMediaTypeOrNull()),
                description = description.toRequestBody("text/plain".toMediaTypeOrNull()),
                price = priceByte.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                category = selectedCategory!!.toRequestBody("text/plain".toMediaTypeOrNull()),
                discount = discountByte.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            ).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@AddDishActivity, "Блюдо обновлено!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Ошибка сервера"
                        Toast.makeText(this@AddDishActivity, "Ошибка: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@AddDishActivity, "Сетевая ошибка: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
        else{
        try {
            val priceByte = price.toInt().coerceIn(-128..127).toByte()
            val discountByte = discount.toByte()

            // Обработка локального изображения
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
            val file = File(getExternalFilesDir(null), "image_${System.currentTimeMillis()}.jpg").apply {
                outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 70, it) }
            }
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("photo", file.name, requestBody)

            // Вызов API
            val tokenManager = SharedPrefTokenManager(this)
            val retrofitService = RetrofitService(this, tokenManager)
            val dishApi = retrofitService.getRetrofit().create(DishApi::class.java)

            dishApi.updateDish(
                id = dishId!!.toLong(),
                name = name.toRequestBody("text/plain".toMediaTypeOrNull()),
                description = description.toRequestBody("text/plain".toMediaTypeOrNull()),
                price = priceByte.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                category = selectedCategory!!.toRequestBody("text/plain".toMediaTypeOrNull()),
                photo = imagePart,
                discount = discountByte.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            ).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@AddDishActivity, "Блюдо обновлено!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Ошибка сервера"
                        Toast.makeText(this@AddDishActivity, "Ошибка: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@AddDishActivity, "Сетевая ошибка: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })

        } catch (e: Exception) {
            Log.e("UpdateDish", "Ошибка: ${e.stackTraceToString()}")
            Toast.makeText(this, "Ошибка обработки изображения", Toast.LENGTH_SHORT).show()
        }
        }
    }

    private fun showDeleteConfirmationDialog() {
        Toast.makeText(this@AddDishActivity, "dishId=${dishId!!}", Toast.LENGTH_SHORT).show()
        AlertDialog.Builder(this@AddDishActivity)
            .setTitle("Подтверждение")
            .setMessage("Вы уверены, что хотите БЕЗВОЗВРАТНО удалить это блюдо?")
            .setPositiveButton("Да") { _, _ ->
                deleteDish()
            }
            .setNegativeButton("Нет", null)
            .show()
    }

    private fun deleteDish(){
        val tokenManager = SharedPrefTokenManager(this)
        val retrofitService = RetrofitService(this, tokenManager)
        val dishApi = retrofitService.getRetrofit().create(DishApi::class.java)
        dishApi.deleteDish(dishId!!).enqueue(object: Callback<Void>{
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AddDishActivity, "Блюдо успешно удалено", Toast.LENGTH_SHORT).show()
                    finish()
                }
                else Toast.makeText(this@AddDishActivity, "Что-то пошло не так!", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@AddDishActivity, "Ошибак сети", Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            binding.ivDishPhoto.setImageURI(selectedImageUri)
        }
    }

    private fun setupSpinner() {
        val categories = arrayOf("Завтрак", "Десерт", "Напиток")
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.textSize = 20f
                view.setTextColor(resources.getColor(android.R.color.black)) // Установка цвета текста
                view.setBackgroundColor(resources.getColor(android.R.color.white)) // Установка цвета фона
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.textSize = 20f
                view.setTextColor(resources.getColor(android.R.color.black)) // Установка цвета текста
                view.setBackgroundColor(resources.getColor(android.R.color.white)) // Установка цвета фона
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDishCategory.adapter = adapter

        binding.spinnerDishCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategory = categories[position]
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                selectedCategory = null
            }
        }
    }



    companion object {
        private const val REQUEST_CODE_SELECT_IMAGE = 101
    }
}

