package com.lotoproba.app.data.api

import android.content.Context
import android.content.SharedPreferences
import com.lotoproba.app.BuildConfig

/**
 * Gestionnaire de la clé API personnelle pour l'analyste IA (Gemini) via OkHttp.
 */
class ApiKeyManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "loto_proba_ai_prefs"
        private const val KEY_CUSTOM_API_KEY = "custom_gemini_api_key"
    }

    fun getEffectiveApiKey(): String {
        val customKey = prefs.getString(KEY_CUSTOM_API_KEY, "")?.trim().orEmpty()
        if (customKey.isNotEmpty()) {
            return customKey
        }
        return try {
            BuildConfig.GEMINI_API_KEY.trim()
        } catch (_: Throwable) {
            ""
        }
    }

    fun getCustomApiKey(): String {
        return prefs.getString(KEY_CUSTOM_API_KEY, "")?.trim().orEmpty()
    }

    fun setCustomApiKey(key: String) {
        prefs.edit().putString(KEY_CUSTOM_API_KEY, key.trim()).apply()
    }

    fun hasCustomApiKey(): Boolean {
        return getCustomApiKey().isNotEmpty()
    }

    fun isKeyConfigured(): Boolean {
        val key = getEffectiveApiKey()
        return key.isNotEmpty() &&
            !key.contains("MY_GEMINI_API_KEY", ignoreCase = true) &&
            !key.contains("unused", ignoreCase = true)
    }

    fun clearCustomApiKey() {
        prefs.edit().remove(KEY_CUSTOM_API_KEY).apply()
    }
}
