package com.example.data

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("planner_pro_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME_ACCENT = "theme_accent"
        private const val KEY_LAST_CITY = "last_city"
    }

    fun setThemeAccent(accent: String) {
        sharedPreferences.edit().putString(KEY_THEME_ACCENT, accent).apply()
    }

    fun getThemeAccent(): String {
        return sharedPreferences.getString(KEY_THEME_ACCENT, "Indigo") ?: "Indigo"
    }

    fun setLastCity(city: String) {
        sharedPreferences.edit().putString(KEY_LAST_CITY, city).apply()
    }

    fun getLastCity(): String {
        return sharedPreferences.getString(KEY_LAST_CITY, "Jakarta") ?: "Jakarta"
    }
}
