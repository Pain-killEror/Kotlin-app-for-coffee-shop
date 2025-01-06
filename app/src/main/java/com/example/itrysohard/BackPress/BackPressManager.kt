package com.example.itrysohard.BackPress

import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.itrysohard.justactivity.MainPage.StartActivity

object BackPressManager {
    private var backPressCount = 0
    private val handler = Handler(Looper.getMainLooper())
    private val resetBackPressCount = Runnable { backPressCount = 0 }

    fun handleBackPress(activity: AppCompatActivity, callback: () -> Unit) {
        backPressCount++
        if (backPressCount == 1) {
            handler.postDelayed(resetBackPressCount, 1000)
            callback()  // вызовем стандартное поведение onBackPressed()
        } else if (backPressCount == 2) {
            handler.removeCallbacks(resetBackPressCount)
            activity.startActivity(Intent(activity, StartActivity::class.java))
            backPressCount = 0
        } else if (backPressCount == 3) {
            handler.removeCallbacks(resetBackPressCount)
            activity.finishAffinity() // Закрывает все активности и завершает приложение
        }
    }
}