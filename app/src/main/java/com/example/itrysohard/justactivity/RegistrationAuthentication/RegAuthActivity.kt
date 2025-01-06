package com.example.itrysohard.justactivity.RegistrationAuthentication

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.itrysohard.BackPress.ActivityHistory
import com.example.itrysohard.BackPress.ActivityHistoryImpl
import com.example.itrysohard.R
import com.example.itrysohard.databinding.ActivityRegauthBinding
import com.example.itrysohard.justactivity.MainPage.StartActivity
import com.example.itrysohard.model.CurrentUser
import com.example.itrysohard.model.User
import com.example.itrysohard.retrofitforDU.RetrofitService
import com.example.itrysohard.retrofitforDU.UserApi
import org.mindrot.jbcrypt.BCrypt
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RegAuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegauthBinding
    private lateinit var userApi: UserApi
    private var isSignUp = true // Переменная для отслеживания состояния (регистрация или вход)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegauthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityHistoryImpl.addActivity(this::class.java)
        val retrofitService = RetrofitService()
        userApi = retrofitService.getRetrofit().create(UserApi::class.java)

        binding.btReg.setOnClickListener { handleAuth() }
        binding.tvLinkSingInOnReg.setOnClickListener { toggleSignInSignUp() }



        binding.showPasswordButton.setOnClickListener {
            if (binding.etPassReg.transformationMethod is PasswordTransformationMethod) {
                // Показать пароль
                binding.etPassReg.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.showPasswordButton.setImageResource(R.drawable.icon_edit)
            } else {
                // Скрыть пароль
                binding.etPassReg.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.showPasswordButton.setImageResource(R.drawable.icon_plus)
            }
            // Переместить курсор в конец текста
            binding.etPassReg.setSelection(binding.etPassReg.text.length)
        }

        binding.showPasswordButtonSec.setOnClickListener {
            if (binding.etPassRegSec.transformationMethod is PasswordTransformationMethod) {
                // Показать пароль
                binding.etPassRegSec.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.showPasswordButtonSec.setImageResource(R.drawable.icon_edit)
            } else {
                // Скрыть пароль
                binding.etPassRegSec.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.showPasswordButtonSec.setImageResource(R.drawable.icon_plus)
            }
            // Переместить курсор в конец текста
            binding.etPassRegSec.setSelection(binding.etPassRegSec.text.length)
        }

    }

    private fun handleAuth() {
        if (isSignUp) {
            registerUser()
        } else {
            loginUser()
        }
    }

    private fun registerUser() {
        val name = binding.etNameReg.text.toString().trim()
        val email = binding.edEmailReg.text.toString().trim()
        val password1 = binding.etPassReg.text.toString().trim()
        val password2 = binding.etPassRegSec.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password1.isEmpty() || password2.isEmpty()) {
            showToast("Пожалуйста, заполните все поля")
            return
        }

        if (!isEmailValid(email)) {
            showToast("Введите корректный адрес электронной почты")
            binding.edEmailReg.text.clear()
            return
        }

        if(password1 != password2){
            showToast("Пароли в двух полях должны совпадать")
            return
        }

        userApi.getUserByName(name).enqueue(object : Callback<User>{
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful){
                    showToast("Пользователь с таким именем уже существует")
                    return
                }
                else{
                    if (response.code() == 404)
                    else showToast("Ошибка")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                showToast("Ошибка!")
                return
            }
        })

        userApi.getUserByEmail(email).enqueue(object : Callback<User>{
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful){
                    showToast("Пользователь с таким email уже существует")
                    return
                }
                else{
                    if (response.code() == 404)
                    else showToast("Ошибка")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                showToast("Ошибка!")
                return
            }
        })

        val newUser = User(name, email, password2, false)
        CurrentUser.isBlocked = false
        userApi.save(newUser).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val savedUser = response.body() ?: return

                    // Проверяем ID: если это 1 или 2, назначаем администратором
                    CurrentUser.isAdmin = (savedUser.id == 1L || savedUser.id == 2L)

                    //showToast("Регистрация успешна")
                    CurrentUser.user = savedUser
                    clearFields()
                    showToast("Регистрация успешна")
                    val lastActivity = ActivityHistoryImpl.getSecondToLastActivity()
                    if (lastActivity != null) {
                        startActivity(Intent(this@RegAuthActivity, lastActivity))
                        finish()
                    } else {
                        startActivity(Intent(this@RegAuthActivity, StartActivity::class.java))
                        finish()
                    }
                } else {
                    showToast("Ошибка регистрации: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                showToast("Ошибка подключения: ${t.message}")
            }
        })

    }





    private fun loginUser() {
        val name = binding.etNameReg.text.toString().trim()
        val password = binding.etPassReg.text.toString().trim()

        if (name.isEmpty() || password.isEmpty()) {
            showToast("Пожалуйста, заполните все поля")
            return
        }

        userApi.getUserByName(name).enqueue(object : Callback<User>{
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful){
                   val validUser = response.body() ?: return showToast("Ошибка чтения данных")

                    if (validUser != null && BCrypt.checkpw(password, validUser.password)) {
                        showToast("Вход успешен")

                        // Устанавливаем текущего пользователя и проверяем, является ли он администратором
                        CurrentUser.user = validUser
                        CurrentUser.isAdmin = (validUser.id == 1L || validUser.id == 2L)
                        CurrentUser.isBlocked = validUser.isBlocked


                        clearFields()
                        val lastActivity = ActivityHistoryImpl.getSecondToLastActivity()
                        if (lastActivity != null) {
                            startActivity(Intent(this@RegAuthActivity, lastActivity))
                            finish()
                        } else {
                            startActivity(Intent(this@RegAuthActivity, StartActivity::class.java))
                            finish()
                        }
                    }

                    else{
                        showToast("Введен неверный пароль!")
                        binding.etPassReg.text.clear()
                    }
                }
                else{
                    if (response.code() == 404) showToast("Пользователя с таким именем не существует")
                    else showToast("Ошибка")
                    return
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                showToast("Ошибка!")
                return
            }
        })


    }

    private fun toggleSignInSignUp() {
        isSignUp = !isSignUp
        updateUI()
        clearFields()
    }

    private fun updateUI() {
        if (isSignUp) {
            binding.tvReg.text = getString(R.string.tvReg)
            binding.tvLinkSingInOnReg.text = getString(R.string.tvLinkSingIn_onReg_change)
            binding.btReg.text = getString(R.string.btReg)
            binding.edEmailReg.visibility = View.VISIBLE
            binding.etPassRegSec.visibility = View.VISIBLE
            binding.showPasswordButtonSec.visibility = View.VISIBLE
        } else {
            binding.tvReg.text = getString(R.string.tvAuth)
            binding.tvLinkSingInOnReg.text = getString(R.string.tvLinkSingUp_onReg_change)
            binding.btReg.text = getString(R.string.btReg_changeSingIn)
            binding.edEmailReg.visibility = View.GONE
            binding.etPassRegSec.visibility = View.GONE
            binding.showPasswordButtonSec.visibility = View.GONE
        }
    }

    private fun clearFields() {
        binding.etNameReg.text.clear()
        binding.edEmailReg.text.clear()
        binding.etPassReg.text.clear()
    }

    private fun isEmailValid(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


}