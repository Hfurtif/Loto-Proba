package com.example.data.api

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig

/**
 * Gestionnaire de la clé API pour l'intelligence artificielle (Gemini).
 * Permet à l'utilisateur de saisir sa propre clé API dans les paramètres
 * ou d'utiliser la clé par défaut si disponible.
 */
class ApiKeyManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "loto_proba_ai_prefs"
        private const val KEY_CUSTOM_API_KEY = "custom_gemini_api_key"
    }

    /**
     * Récupère la clé API effective.
     * Priorité 1 : La clé personnalisée saisie par l'utilisateur dans les paramètres.
     * Priorité 2 : La clé injectée par défaut dans BuildConfig (Secrets Gradle).
     */
    fun getEffectiveApiKey(): String {
        val customKey = prefs.getString(KEY_CUSTOM_API_KEY, "")?.trim() ?: ""
        if (customKey.isNotEmpty()) {
            return customKey
        }
        return try {
            BuildConfig.GEMINI_API_KEY.trim()
        } catch (e: Throwable) {
            ""
        }
    }

    /**
     * Retourne la clé personnalisée enregistrée par l'utilisateur (ou vide si non définie).
     */
    fun getCustomApiKey(): String {
        return prefs.getString(KEY_CUSTOM_API_KEY, "")?.trim() ?: ""
    }

    /**
     * Enregistre une nouvelle clé API personnalisée.
     */
    fun setCustomApiKey(key: String) {
        prefs.edit().putString(KEY_CUSTOM_API_KEY, key.trim()).apply()
    }

    /**
     * Indique si l'utilisateur a configuré sa propre clé dans les paramètres.
     */
    fun hasCustomApiKey(): Boolean {
        return getCustomApiKey().isNotEmpty()
    }

    /**
     * Indique si une clé API valide (personnalisée ou par défaut) est disponible.
     */
    fun isKeyConfigured(): Boolean {
        val key = getEffectiveApiKey()
        return key.isNotEmpty() &&
                !key.contains("MY_GEMINI_API_KEY", ignoreCase = true) &&
                !key.contains("unused", ignoreCase = true)
    }

    /**
     * Réinitialise la clé personnalisée pour revenir à la configuration par défaut.
     */
    fun clearCustomApiKey() {
        prefs.edit().remove(KEY_CUSTOM_API_KEY).apply()
    }
}
