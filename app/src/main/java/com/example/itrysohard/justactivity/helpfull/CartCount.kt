package com.example.itrysohard.justactivity.helpfull

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.itrysohard.MyApplication

abstract class CartCount : AppCompatActivity() {
    protected lateinit var myApplication: MyApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myApplication = application as MyApplication
    }

    protected fun updateCartCountDisplay(textView: TextView) {
        textView.text = myApplication.cartItemCount.toString()
    }
}