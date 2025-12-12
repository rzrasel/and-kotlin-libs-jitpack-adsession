package com.rzrasel.adsession.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object AppAdPreferences {

    private const val TAG = "AppPreferenceManager"
    private lateinit var prefs: SharedPreferences

    /** Must be called once from Application class */
    fun init(
        context: Context,
        prefName: String = "",
        mode: Int = Context.MODE_PRIVATE
    ) {
        val finalName = prefName.ifBlank { context.packageName }
        val finalMode = if (mode == 0) Context.MODE_PRIVATE else mode

        prefs = context.getSharedPreferences(finalName, finalMode)
        Log.d(TAG, "SharedPreferences Initialized: $finalName")
    }

    private fun ensureInitialized() {
        if (!::prefs.isInitialized) {
            throw IllegalStateException("AppPreferenceManager.init() must be called before using preferences.")
        }
    }

    // -------------------------
    // PUT
    // -------------------------
    fun put(key: String, value: Any?) {
        ensureInitialized()

        val editor = prefs.edit()

        when (value) {
            is String? -> editor.putString(key, value)
            is Int -> editor.putInt(key, value)
            is Boolean -> editor.putBoolean(key, value)
            is Float -> editor.putFloat(key, value)
            is Long -> editor.putLong(key, value)
            null -> editor.remove(key)
            else -> throw IllegalArgumentException("Unsupported type: ${value::class.java}")
        }

        editor.apply()
    }

    // -------------------------
    // GET
    // -------------------------
    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String, default: T): T {
        ensureInitialized()

        return when (default) {
            is String -> prefs.getString(key, default) as T
            is Int -> prefs.getInt(key, default) as T
            is Boolean -> prefs.getBoolean(key, default) as T
            is Float -> prefs.getFloat(key, default) as T
            is Long -> prefs.getLong(key, default) as T
            else -> throw IllegalArgumentException("Unsupported default value type")
        }
    }

    // -------------------------
    // REMOVE / CLEAR
    // -------------------------
    fun remove(key: String) {
        ensureInitialized()
        prefs.edit().remove(key).apply()
    }

    fun clear() {
        ensureInitialized()
        prefs.edit().clear().apply()
    }

    // -------------------------
    // PRINT INDIVIDUAL KEY
    // -------------------------
    fun printKey(key: String) {
        ensureInitialized()

        if (!prefs.contains(key)) {
            Log.d(TAG, "DEBUG_LOG Key '$key' not found in SharedPreferences")
            return
        }

        val value = prefs.all[key]
        Log.d(TAG, "DEBUG_LOG Key: '$key' | Value: $value | Type: ${value?.javaClass?.simpleName}")
    }

    // -------------------------
    // PRINT ALL KEYS + VALUES
    // -------------------------
    fun printAll() {
        ensureInitialized()

        val allPrefs = prefs.all

        if (allPrefs.isEmpty()) {
            Log.d(TAG, "DEBUG_LOG SharedPreferences is empty.")
            return
        }

        Log.d(TAG, "DEBUG_LOG ------ SharedPreferences Dump START ------")
        allPrefs.forEach { (key, value) ->
            Log.d(TAG, "DEBUG_LOG Key: $key | Value: $value | Type: ${value?.javaClass?.simpleName}")
        }
        Log.d(TAG, "DEBUG_LOG ------ SharedPreferences Dump END ------")
    }
}