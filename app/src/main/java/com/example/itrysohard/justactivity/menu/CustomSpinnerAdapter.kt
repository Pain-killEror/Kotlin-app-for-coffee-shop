package com.example.itrysohard.justactivity.menu

import android.R
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView


class CustomSpinnerAdapter(private val context: Context, private val values: Array<String>) :
    ArrayAdapter<String?>(context, R.layout.simple_spinner_item, values) {
    override fun getDropDownView(position: Int, convertView: View, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val textView = view as TextView
        textView.setTextColor(context.resources.getColor(R.color.black)) // Цвет текста
        return view
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val textView = view as TextView
        textView.setTextColor(context.resources.getColor(R.color.black)) // Цвет текста
        return view
    }
}