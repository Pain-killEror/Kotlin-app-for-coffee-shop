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
import android.text.Editable
import android.text.TextWatcher
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
import com.example.itrysohard.jwt.SharedPrefTokenManager
import com.example.itrysohard.retrofitforDU.DishApi
import com.example.itrysohard.retrofitforDU.RetrofitService
import com.squareup.picasso.Picasso
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AddDishActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddDishBinding
    private var selectedImageUri: Uri? = null
    private var selectedCategory: String? = null
    private var dishId: Int? = null // ID редактируемого блюда
    private var isSaving = false
    private var originalIds = mutableListOf<Int>()
    private var originalVolumes = mutableListOf<String>()
    private var originalPrices = mutableListOf<String>()
    private var originalPhotos = mutableListOf<String>()
    private lateinit var dishApi: DishApi
    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) = checkFields()
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }


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


            if (intent.hasExtra("volumes")) {
                originalIds = intent.getIntegerArrayListExtra("dish_ids")?.toMutableList() ?: mutableListOf()
                originalVolumes = intent.getStringArrayListExtra("volumes")?.toMutableList() ?: mutableListOf()
                originalPrices = intent.getStringArrayListExtra("prices")?.toMutableList() ?: mutableListOf()


                // Заполняем поля для ВСЕХ вариантов
                originalVolumes.forEachIndexed { index, volume ->
                    when (index) {
                        0 -> {
                            binding.etVolume.setText(volume)
                            binding.etDishPrice.setText(originalPrices.getOrNull(index) ?: "")
                            binding.volumePrice2.visibility = View.VISIBLE
                        }
                        1 -> {
                            binding.etVolume2.setText(volume)
                            binding.etDishPrice2.setText(originalPrices.getOrNull(index) ?: "")
                            binding.volumePrice2.visibility = View.VISIBLE
                            binding.volumePrice3.visibility = View.VISIBLE
                        }
                        2 -> {
                            binding.etVolume3.setText(volume)
                            binding.etDishPrice3.setText(originalPrices.getOrNull(index) ?: "")
                            binding.volumePrice3.visibility = View.VISIBLE
                        }
                    }
                }
            }
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

        setupTextWatchers()
    }

    private fun setupTextWatchers() {
        binding.etVolume.addTextChangedListener(textWatcher)
        binding.etDishPrice.addTextChangedListener(textWatcher)
        binding.etVolume2.addTextChangedListener(textWatcher)
        binding.etDishPrice2.addTextChangedListener(textWatcher)
        binding.etVolume3.addTextChangedListener(textWatcher)
        binding.etDishPrice3.addTextChangedListener(textWatcher)
    }

    private fun checkFields() {
        val pair1Filled = !binding.etVolume.text.isNullOrEmpty() && !binding.etDishPrice.text.isNullOrEmpty()
        val pair2Filled = !binding.etVolume2.text.isNullOrEmpty() && !binding.etDishPrice2.text.isNullOrEmpty()

        binding.volumePrice2.visibility = if (pair1Filled) View.VISIBLE else View.GONE
        binding.volumePrice3.visibility = if (pair2Filled) View.VISIBLE else View.GONE
    }

    private fun saveDish() {
        val pairs = listOf(
            Pair(binding.etVolume, binding.etDishPrice),
            Pair(binding.etVolume2, binding.etDishPrice2),
            Pair(binding.etVolume3, binding.etDishPrice3)
        ).filter { it.first.text.isNotEmpty() && it.second.text.isNotEmpty() }

        if (pairs.isEmpty()) {
            Toast.makeText(this, "Заполните хотя бы один объем и цену", Toast.LENGTH_SHORT).show()
            resetSavingState()
            return
        }

        val name = binding.etDishName.text.toString()
        val description = binding.etDishDescription.text.toString()
        val discount = binding.etDishDiscount.text.toString().toIntOrNull() ?: 0

        if (name.isEmpty() || description.isEmpty() || selectedImageUri == null || selectedCategory == null) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            resetSavingState()
            return
        }

        val calls = mutableListOf<Call<ResponseBody>>()
        var successfulRequests = 0
        val totalRequests = pairs.size

        pairs.forEach { pair ->
            val volume = pair.first.text.toString()
            val price = pair.second.text.toString().toDoubleOrNull() ?: run {
                Toast.makeText(this, "Некорректная цена для объема $volume", Toast.LENGTH_SHORT).show()
                resetSavingState()
                return
            }

            val call = uploadDishPair(name, description, volume, price, discount.toByte()) { success ->
                if (success) {
                    successfulRequests++
                    if (successfulRequests == totalRequests) {
                        runOnUiThread {
                            Toast.makeText(this@AddDishActivity, "Все варианты блюда сохранены!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@AddDishActivity, "Ошибка при сохранении некоторых вариантов", Toast.LENGTH_SHORT).show()
                        resetSavingState()
                    }
                }
            }
            calls.add(call)
        }
    }
    private fun uploadDishPair(
        name: String,
        description: String,
        volume: String,
        price: Double,
        discount: Byte,
        callback: (Boolean) -> Unit
    ): Call<ResponseBody> {
        val isImageFromWeb = selectedImageUri?.scheme?.startsWith("http") == true
        val imagePart = if (!isImageFromWeb && selectedImageUri != null) {
            // Обработка локального изображения
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)

            val sizeBeforeCompression = bitmap.byteCount / 1024
            Log.d("MyLog", "Размер изображения до сжатия: $sizeBeforeCompression KB")

            val file = File(getExternalFilesDir(null), "image_${System.currentTimeMillis()}.jpg").apply {
                FileOutputStream(this).use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                }
            }

            val sizeAfterCompression = file.length() / 1024
            Log.d("MyLog", "Размер после сжатия: $sizeAfterCompression KB")

            file.asRequestBody("image/*".toMediaTypeOrNull())
                .let { MultipartBody.Part.createFormData("photo", file.name, it) }
        } else {
            // Для веб-изображений передаем пустую часть
            "".toRequestBody("text/plain".toMediaTypeOrNull())
                .let { MultipartBody.Part.createFormData("photo", "", it) }
        }

        val tokenManager = SharedPrefTokenManager(this)
        val retrofitService = RetrofitService(this, tokenManager)
        val dishApi = retrofitService.getRetrofit().create(DishApi::class.java)

        val call = dishApi.uploadDish(
            name = name.toRequestBody("text/plain".toMediaTypeOrNull()),
            description = description.toRequestBody("text/plain".toMediaTypeOrNull()),
            volume = volume.toRequestBody("text/plain".toMediaTypeOrNull()),
            price = price.toInt().toByte().toString().toRequestBody("text/plain".toMediaTypeOrNull()),
            category = selectedCategory!!.toRequestBody("text/plain".toMediaTypeOrNull()),
            photo = imagePart,
            discount = discount.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        )

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    callback(true)
                } else {
                    callback(false)
                    val errorMessage = response.errorBody()?.string() ?: "Неизвестная ошибка"
                    Log.e("API Error", errorMessage)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                callback(false)
                Log.e("Network Error", t.message ?: "Unknown error")
            }
        })

        return call
    }

//    private fun uploadDishPair(name: String,
//                               description: String,
//                               volume: String,
//                               price: Double,
//                               discount: Byte,
//                               callback: (Boolean) -> Unit
//    ): Call<ResponseBody> {
//
//        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImageUri)
//
//        val sizeBeforeCompression = bitmap.byteCount / 1024 // размер в КБ
//        Log.d("MyLog", "Размер изображения до сжатия: $sizeBeforeCompression KB")
//
//        // Сохранение Bitmap без метаданных
//        val file = File(getExternalFilesDir(null), "image_${System.currentTimeMillis()}.jpg")
//        val outputStream = FileOutputStream(file)
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream) // Сохранение в JPEG без метаданных
//        outputStream.flush()
//        outputStream.close()
//
//
//        val sizeAfterCompression = file.length() / 1024 // размер в КБ
//        Log.d("MyLog", "Размер изображения после сжатия: $sizeAfterCompression KB")
//
//
//        // Создание RequestBody для загрузки
//        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
//        val imagePart = MultipartBody.Part.createFormData("photo", file.name, requestBody)
//
//        val tokenManager = SharedPrefTokenManager(this)
//        val retrofitService = RetrofitService(this, tokenManager)
//        val dishApi = retrofitService.getRetrofit().create(DishApi::class.java)
//
//
//        val call = dishApi.uploadDish(
//            name = name.toRequestBody("text/plain".toMediaTypeOrNull()),
//            description = description.toRequestBody("text/plain".toMediaTypeOrNull()),
//            volume = volume.toRequestBody("text/plain".toMediaTypeOrNull()), // Добавить volume в API
//            price = price.toInt().toByte().toString().toRequestBody("text/plain".toMediaTypeOrNull()),
//            category = selectedCategory!!.toRequestBody("text/plain".toMediaTypeOrNull()),
//            photo = imagePart,
//            discount = discount.toString().toRequestBody("text/plain".toMediaTypeOrNull())
//        )
//
//        call.enqueue(object : Callback<ResponseBody> {
//            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//                if (response.isSuccessful) {
//                    callback(true)
//                } else {
//                    callback(false)
//                    val errorMessage = response.errorBody()?.string() ?: "Неизвестная ошибка"
//                    Log.e("API Error", errorMessage)
//                }
//            }
//
//            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                callback(false)
//                Log.e("Network Error", t.message ?: "Unknown error")
//            }
//        })
//        return call
//    }




    private fun resetSavingState() {
        runOnUiThread {
            isSaving = false
            binding.btnSaveDish.isEnabled = true
        }
    }

//    private fun updateDish() {
//        val name = binding.etDishName.text.toString()
//        val description = binding.etDishDescription.text.toString()
//        val price = binding.etDishPrice.text.toString().toDoubleOrNull()
//        val price2 = binding.etDishPrice2.text.toString().toDoubleOrNull()
//        val price3 = binding.etDishPrice3.text.toString().toDoubleOrNull()
//        val volume = binding.etVolume.text.toString().toDoubleOrNull()
//        val volume2 = binding.etVolume2.text.toString().toDoubleOrNull()
//        val volume3 = binding.etVolume3.text.toString().toDoubleOrNull()
//        val discount = binding.etDishDiscount.text.toString().toIntOrNull() ?: 0
//
//        // Проверка обязательных полей
//        if (name.isEmpty() || description.isEmpty() || price == null || selectedCategory == null || selectedImageUri == null) {
//            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        // Проверяем, является ли изображение веб-ссылкой
//        if (selectedImageUri!!.scheme?.startsWith("http") == true) {
//            val tokenManager = SharedPrefTokenManager(this)
//            val retrofitService = RetrofitService(this, tokenManager)
//            val dishApi = retrofitService.getRetrofit().create(DishApi::class.java)
//            val priceByte = price.toInt().coerceIn(-128..127).toByte()
//            val discountByte = discount.toByte()
//            dishApi.updateDish(
//                id = dishId!!.toLong(),
//                name = name.toRequestBody("text/plain".toMediaTypeOrNull()),
//                description = description.toRequestBody("text/plain".toMediaTypeOrNull()),
//                price = priceByte.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
//                category = selectedCategory!!.toRequestBody("text/plain".toMediaTypeOrNull()),
//                discount = discountByte.toString().toRequestBody("text/plain".toMediaTypeOrNull())
//            ).enqueue(object : Callback<ResponseBody> {
//                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//                    if (response.isSuccessful) {
//                        Toast.makeText(this@AddDishActivity, "Блюдо обновлено!", Toast.LENGTH_SHORT).show()
//                        finish()
//                    } else {
//                        val errorMessage = response.errorBody()?.string() ?: "Ошибка сервера"
//                        Toast.makeText(this@AddDishActivity, "Ошибка: $errorMessage", Toast.LENGTH_SHORT).show()
//                    }
//                }
//
//                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                    Toast.makeText(this@AddDishActivity, "Сетевая ошибка: ${t.message}", Toast.LENGTH_SHORT).show()
//                }
//            })
//        }
//        else{
//        try {
//            val priceByte = price.toInt().coerceIn(-128..127).toByte()
//            val discountByte = discount.toByte()
//
//            // Обработка локального изображения
//            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
//            val file = File(getExternalFilesDir(null), "image_${System.currentTimeMillis()}.jpg").apply {
//                outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 70, it) }
//            }
//            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
//            val imagePart = MultipartBody.Part.createFormData("photo", file.name, requestBody)
//
//            // Вызов API
//            val tokenManager = SharedPrefTokenManager(this)
//            val retrofitService = RetrofitService(this, tokenManager)
//            val dishApi = retrofitService.getRetrofit().create(DishApi::class.java)
//
//            dishApi.updateDish(
//                id = dishId!!.toLong(),
//                name = name.toRequestBody("text/plain".toMediaTypeOrNull()),
//                description = description.toRequestBody("text/plain".toMediaTypeOrNull()),
//                price = priceByte.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
//                category = selectedCategory!!.toRequestBody("text/plain".toMediaTypeOrNull()),
//                photo = imagePart,
//                discount = discountByte.toString().toRequestBody("text/plain".toMediaTypeOrNull())
//            ).enqueue(object : Callback<ResponseBody> {
//                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//                    if (response.isSuccessful) {
//                        Toast.makeText(this@AddDishActivity, "Блюдо обновлено!", Toast.LENGTH_SHORT).show()
//                        finish()
//                    } else {
//                        val errorMessage = response.errorBody()?.string() ?: "Ошибка сервера"
//                        Toast.makeText(this@AddDishActivity, "Ошибка: $errorMessage", Toast.LENGTH_SHORT).show()
//                    }
//                }
//
//                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                    Toast.makeText(this@AddDishActivity, "Сетевая ошибка: ${t.message}", Toast.LENGTH_SHORT).show()
//                }
//            })
//
//        } catch (e: Exception) {
//            Log.e("UpdateDish", "Ошибка: ${e.stackTraceToString()}")
//            Toast.makeText(this, "Ошибка обработки изображения", Toast.LENGTH_SHORT).show()
//        }
//        }
//    }


    private fun updateDish() {
        val newPairs = listOf(
            Triple(0, binding.etVolume.text.toString(), binding.etDishPrice.text.toString()),
            Triple(1, binding.etVolume2.text.toString(), binding.etDishPrice2.text.toString()),
            Triple(2, binding.etVolume3.text.toString(), binding.etDishPrice3.text.toString())
        ).filter { it.second.isNotEmpty() && it.third.isNotEmpty() }

        // Проверка заполненности основных полей
        val name = binding.etDishName.text.toString()
        val description = binding.etDishDescription.text.toString()
        val discount = binding.etDishDiscount.text.toString().toIntOrNull() ?: 0

        if (name.isEmpty() || description.isEmpty() || selectedCategory == null || selectedImageUri == null) {
            Toast.makeText(this, "Заполните все обязательные поля", Toast.LENGTH_SHORT).show()
            resetSavingState()
            return
        }

        var successfulUpdates = 0
        val totalOperations = newPairs.size + (originalIds.size - newPairs.size).coerceAtLeast(0)

        fun checkCompletion() {
            if (successfulUpdates == totalOperations) {
                runOnUiThread {
                    Toast.makeText(this@AddDishActivity, "Все изменения сохранены!", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK) // Уведомляем MenuActivity об обновлении
                    finish()
                }
            }
        }

        // 1. Обновление существующих вариантов
        newPairs.take(originalIds.size).forEachIndexed { index, (_, volumeStr, priceStr) ->
            val volume = volumeStr
            val price = priceStr.toDoubleOrNull() ?: run {
                Toast.makeText(this, "Ошибка в цене для объема $volume", Toast.LENGTH_SHORT).show()
                return@forEachIndexed
            }

            updateSingleDish(
                dishId = originalIds[index],
                name = name,
                description = description,
                volume = volume,
                price = price,
                discount = discount
            ) { success ->
                if (success) successfulUpdates++
                checkCompletion()
            }
        }

        // 2. Создание новых вариантов
        newPairs.drop(originalIds.size).forEach { (_, volumeStr, priceStr) ->
            val volume = volumeStr
            val price = priceStr.toDoubleOrNull() ?: run {
                Toast.makeText(this, "Ошибка в цене для объема $volume", Toast.LENGTH_SHORT).show()
                return@forEach
            }

            uploadDishPair(
                name = name,
                description = description,
                volume = volume,
                price = price,
                discount = discount.toByte()
            ) { success ->
                if (success) successfulUpdates++
                checkCompletion()
            }
        }

        // 3. Удаление удаленных вариантов
        if (newPairs.size < originalIds.size) {
            originalIds.subList(newPairs.size, originalIds.size).forEach { id ->
                deleteDishVariant(id) {
                    successfulUpdates++
                    checkCompletion()
                }
            }
        }
    }

    private fun updateSingleDish(
        dishId: Int,
        name: String,
        description: String,
        volume: String,
        price: Double,
        discount: Int,
        callback: (Boolean) -> Unit
    ) {
        try {
            val isImageFromWeb = selectedImageUri?.scheme?.startsWith("http") == true
            var imagePart: MultipartBody.Part? = null

            if (!isImageFromWeb && selectedImageUri != null) {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                val file = File(getExternalFilesDir(null), "image_${System.currentTimeMillis()}.jpg").apply {
                    outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 70, it) }
                }
                imagePart = file.asRequestBody("image/*".toMediaTypeOrNull())
                    .let { MultipartBody.Part.createFormData("photo", file.name, it) }
            }

            val tokenManager = SharedPrefTokenManager(this)
            val retrofitService = RetrofitService(this, tokenManager)
            val dishApi = retrofitService.getRetrofit().create(DishApi::class.java)

            val priceByte = price.toInt().coerceIn(-128..127).toByte()
            val discountByte = discount.toByte()

            val call = if (isImageFromWeb) {
                dishApi.updateDish(
                    id = dishId.toLong(),
                    name = name.toRequestBody("text/plain".toMediaTypeOrNull()),
                    description = description.toRequestBody("text/plain".toMediaTypeOrNull()),
                    price = priceByte.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                    volume = volume.toRequestBody("text/plain".toMediaTypeOrNull()),
                    category = selectedCategory!!.toRequestBody("text/plain".toMediaTypeOrNull()),
                    discount = discountByte.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                )
            } else {
                dishApi.updateDish(
                    id = dishId.toLong(),
                    name = name.toRequestBody("text/plain".toMediaTypeOrNull()),
                    description = description.toRequestBody("text/plain".toMediaTypeOrNull()),
                    price = priceByte.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                    volume = volume.toRequestBody("text/plain".toMediaTypeOrNull()),
                    category = selectedCategory!!.toRequestBody("text/plain".toMediaTypeOrNull()),
                    photo = imagePart,
                    discount = discountByte.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                )
            }

            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        callback(true)
                    } else {
                        callback(false)
                        Log.e("UPDATE", "Ошибка: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    callback(false)
                    Log.e("UPDATE", "Ошибка сети", t)
                }
            })
        } catch (e: Exception) {
            Log.e("UPDATE", "Ошибка обработки", e)
            callback(false)
        }
    }

    private fun deleteDishVariant(dishId: Int, callback: () -> Unit) {
        val tokenManager = SharedPrefTokenManager(this)
        val retrofitService = RetrofitService(this, tokenManager)
        val dishApi = retrofitService.getRetrofit().create(DishApi::class.java)

        dishApi.deleteDish(dishId.toLong()).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (!response.isSuccessful) Log.e("DELETE", "Ошибка удаления")
                callback()
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("DELETE", "Сетевая ошибка", t)
                callback()
            }
        })
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
        val dishName = binding.etDishName.text.toString()
        if (dishName.isEmpty()) {
            Toast.makeText(this, "Название блюда не найдено", Toast.LENGTH_SHORT).show()
            return
        }

        val tokenManager = SharedPrefTokenManager(this)
        val retrofitService = RetrofitService(this, tokenManager)
        val dishApi = retrofitService.getRetrofit().create(DishApi::class.java)

        dishApi.deleteDishByName(dishName).enqueue(object : Callback<Void> {
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

