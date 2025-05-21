package com.example.itrysohard.justactivity.about_us

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.itrysohard.MyApplication
import com.example.itrysohard.databinding.ActivityAboutUsBinding
import com.example.itrysohard.justactivity.PersonalPage.PersAccActivity
import com.example.itrysohard.justactivity.RegistrationAuthentication.RegAuthActivity
import com.example.itrysohard.justactivity.MainPage.StartActivity
import com.example.itrysohard.justactivity.menu.cart.CartActivity
import com.example.itrysohard.retrofitforDU.ReviewApi
import com.example.itrysohard.retrofitforDU.RetrofitService
import com.example.itrysohard.justactivity.menu.MenuActivity
import com.example.itrysohard.jwt.JWTDecoder
import com.example.itrysohard.jwt.SharedPrefTokenManager
import com.example.itrysohard.jwt.TokenManager
import com.example.itrysohard.model.answ.ReviewAnswDTO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AboutUsActivity : AppCompatActivity() {

    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var reviewApi: ReviewApi
    private lateinit var myApplication: MyApplication
    private lateinit var binding: ActivityAboutUsBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutUsBinding.inflate(layoutInflater) // Инициализация View Binding
        setContentView(binding.root) // Устанавливаем корневой элемент из binding
        tokenManager = SharedPrefTokenManager(this)

        // Инициализация RecyclerView
        reviewAdapter = ReviewAdapter(emptyList()) { review ->
            val intent = Intent(this, ReviewDetailActivity::class.java).apply {
                putExtra("review", review) // Теперь работает, так как DTO Parcelable
            }
            startActivity(intent)
        }

        binding.recyclerViewReviews.adapter = reviewAdapter
        binding.recyclerViewReviews.layoutManager = LinearLayoutManager(this)

        // Инициализация Retrofit и API
        val tokenManager = SharedPrefTokenManager(this)
        reviewApi = RetrofitService(this,tokenManager).getRetrofit().create(ReviewApi::class.java)

        // Загрузка отзывов
        loadReviews()

        binding.buttonLeaveReview.setOnClickListener {
            showAuthorizationDialog()
            startActivity(Intent(this, LeaveReviewActivity::class.java))

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
            startActivity(Intent(this, CartActivity::class.java))

        }

        binding.btnPersAcc.setOnClickListener {
            showAuthorizationDialogPers()
        }
        myApplication = application as MyApplication
        updateCartCountDisplay()



        binding.buttonLeaveReview.setOnClickListener {
           showAuthorizationDialog()

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

        val isBlocked = accessToken != null &&
                !JWTDecoder.isExpired(accessToken) &&
                JWTDecoder.getRole(accessToken) == "ROLE_BLOCKED"

        val isAdminModerator = accessToken != null &&
                !JWTDecoder.isExpired(accessToken) &&
                JWTDecoder.getRole(accessToken) == "ROLE_ADMIN" || JWTDecoder.getRole(accessToken) == "ROLE_MODERATOR"

        if (isAdminModerator) {
            AlertDialog.Builder(this).apply {
                setTitle("Вы администрация")
                setMessage("Ваш аккаунт является аккаунтом администрации на неопределенный срок, вы не можете написать отзвы!")
                setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
            }.create().show()
        }

        if (isBlocked && !isAdminModerator) {
            AlertDialog.Builder(this).apply {
                setTitle("Вы заблокированы")
                setMessage("Ваш аккаунт является заблокированным на неопределенный срок, вы не можете написать отзвы! Обратитесь к администратору ")
                setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
            }.create().show()
        }

        if (!isAuthorized && !isBlocked && !isAdminModerator) {
            AlertDialog.Builder(this).apply {
                setTitle("Требуется авторизация")
                setMessage("Для выполнения действия необходима авторизация")
                setPositiveButton("Войти") { _, _ ->
                    startActivity(Intent(context, RegAuthActivity::class.java))
                }
                setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
            }.create().show()
        } else if(isAuthorized && !isBlocked && !isAdminModerator){
            startActivity(Intent(this, LeaveReviewActivity::class.java))
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


    override fun onResume() {
        super.onResume()
        loadReviews()

    }



    private fun updateCartCountDisplay() {
        binding.tvCartCount.text = myApplication.cartItemCount.toString()
    }

    private fun loadReviews() {
        reviewApi.getAllReviews().enqueue(object : Callback<List<ReviewAnswDTO>> {
            override fun onResponse(call: Call<List<ReviewAnswDTO>>, response: Response<List<ReviewAnswDTO>>) {
                if (response.isSuccessful) {
                    reviewAdapter.setReviews(response.body() ?: emptyList()) // Используем переименованный метод
                } else if (response.code() == 401) {
                    tokenManager.clearTokens() // Теперь tokenManager доступен
                    showAuthorizationDialog()
                } else {
                    Toast.makeText(this@AboutUsActivity,
                        "Ошибка: ${response.errorBody()?.string()}",
                        Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<ReviewAnswDTO>>, t: Throwable) {
                Toast.makeText(this@AboutUsActivity,
                    "Ошибка сети: ${t.message}",
                    Toast.LENGTH_SHORT).show()
                t.printStackTrace()
            }


        })


    }

}