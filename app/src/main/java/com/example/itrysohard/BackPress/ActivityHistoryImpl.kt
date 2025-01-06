package com.example.itrysohard.BackPress

import android.app.Activity

object ActivityHistoryImpl {
    private val historyStack = ArrayDeque<Class<out Activity>>(3)

    fun addActivity(activityClass: Class<out Activity>) {
        if (historyStack.size == 3) {
            historyStack.removeFirst()
        }
        historyStack.add(activityClass)
    }

    fun getLastActivity(): Class<out Activity>? {
        return if (historyStack.isNotEmpty()) historyStack.removeLastOrNull() else null
    }
    fun getSecondToLastActivity(): Class<out Activity>? {
        return if (historyStack.size >= 2) {
            val lastActivity = historyStack.removeLastOrNull()
            val secondToLastActivity = historyStack.lastOrNull()
            lastActivity?.let { historyStack.add(it) }
            secondToLastActivity
        } else null
    }
}