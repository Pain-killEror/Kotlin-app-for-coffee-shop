package com.example.itrysohard.BackPress

import android.app.Activity

interface ActivityHistory {
    fun addActivity(activityClass: Class<out Activity>)
    fun getLastActivity(): Class<out Activity>?
}

