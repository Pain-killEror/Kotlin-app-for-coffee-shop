package com.example.itrysohard

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.itrysohard.databinding.ActivityAddDishBinding
import com.example.myappforcafee.model.DishServ
import com.example.myappforcafee.retrofit.DishApi
import com.example.myappforcafee.retrofit.RetrofitService
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class AddDishActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddDishBinding
    private fun saveDish() {
        val name = binding.etDishName.text.toString()
        val description = binding.etDishDescription.text.toString()
        val price = binding.etDishPrice.text.toString().toDoubleOrNull()

        if (name.isEmpty() || description.isEmpty() || price == null || selectedImageUri == null) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        val file = File(getRealPathFromURI(selectedImageUri!!))
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("file", file.name, requestBody)

        val retrofitService = RetrofitService()
        val dishApi = retrofitService.getRetrofit().create(DishApi::class.java)

        val call = dishApi.uploadDish(
            name = name.toRequestBody("text/plain".toMediaTypeOrNull()),
            description = description.toRequestBody("text/plain".toMediaTypeOrNull()),
            price = price.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
            image = imagePart
        )

        call.enqueue(object : Callback<DishServ> {
            override fun onResponse(call: Call<DishServ>, response: Response<DishServ>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AddDishActivity, "Блюдо добавлено!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Неизвестная ошибка"
                    Toast.makeText(this@AddDishActivity, "Ошибка: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DishServ>, t: Throwable) {
                Toast.makeText(this@AddDishActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDishBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnChoosePhoto.setOnClickListener { openGallery() }
        binding.btnSaveDish.setOnClickListener {
            saveDish()

        }
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
            binding.ivDishPhoto.setImageURI(selectedImageUri) // Исправление отображения выбранного изображения
        }
    }

    private fun getRealPathFromURI(uri: Uri): String {
        var path = ""
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        if (cursor != null) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            path = cursor.getString(columnIndex)
            cursor.close()
        }
        return path
    }

    companion object {
        private const val REQUEST_CODE_SELECT_IMAGE = 101
    }
}
