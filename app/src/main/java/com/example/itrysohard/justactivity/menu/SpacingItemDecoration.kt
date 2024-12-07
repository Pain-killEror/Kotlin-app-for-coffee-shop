package com.example.itrysohard.justactivity.menu

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpacingItemDecoration(private val topSpacing: Int, private val bottomSpacing: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)

        // Устанавливаем отступ для верхнего элемента
        if (position == 0) {
            outRect.top = topSpacing
        }

        // Устанавливаем отступ для последнего элемента
        if (position == state.itemCount - 1) {
            outRect.bottom = bottomSpacing
        }
    }
}