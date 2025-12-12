package com.rzrasel.adsession.model

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.rzrasel.adsession.preferences.AppAdPreferences
import kotlinx.serialization.Serializable

@Serializable
data class AdSessionModel(
    @SerializedName("min_time")
    var minTime: Long = 0,
    @SerializedName("max_time")
    var maxTime: Long = 0,
    @SerializedName("min_event_count")
    var minEventCount: Int = 0,
    @SerializedName("total_event_count")
    var totalEventCount: Int = 0,
    @SerializedName("last_run_time")
    var lastRunTime: Long = System.currentTimeMillis(),
) {

    companion object {
        private const val TAG = "AdSessionModel"
        const val KEY_PREF = "ad_mob_session"
        private val gson = Gson()
    }

    fun updateLastRunTime(time: Long = System.currentTimeMillis()) {
        this.lastRunTime = time
    }

    fun updateTotalEventCount(count: Int = 1) {
        this.totalEventCount += count
        savePreferences()
    }

    fun savePreferences() {
        val json = this.toJson()
        AppAdPreferences.put(KEY_PREF, json)
        Log.d(TAG, "DEBUG_LOG Session saved → $json")
    }

    fun loadPreferences(): AdSessionModel {
        val json = AppAdPreferences.get(KEY_PREF, "")
        if (json.isBlank()) {
            Log.d(TAG, "DEBUG_LOG No saved session found")
            return this
        }

        return try {
            val loaded = gson.fromJson(json, AdSessionModel::class.java)
            // copy fields into current instance
            minTime = loaded.minTime
            maxTime = loaded.maxTime
            minEventCount = loaded.minEventCount
            totalEventCount = loaded.totalEventCount
            lastRunTime = loaded.lastRunTime
            Log.d(TAG, "DEBUG_LOG Session loaded → $json")
            this
        } catch (t: Throwable) {
            Log.e(TAG, "DEBUG_LOG Failed to load AdSessionModel from preferences", t)
            this
        }
    }

    fun toJson(): String {
        return gson.toJson(this)
    }

    /**
     * 🔥 trigger conditions
     */
    fun isMinTimePassed(currentTime: Long = System.currentTimeMillis()): Boolean {
        return (currentTime - lastRunTime) >= minTime
    }

    fun isMaxTimePassed(currentTime: Long = System.currentTimeMillis()): Boolean {
        return (currentTime - lastRunTime) >= maxTime
    }

    fun isMinEventCountReached(): Boolean {
        return totalEventCount >= minEventCount
    }

    fun logSession(tag: String = "DEBUG_LOG") {
        Log.d(
            tag, "DEBUG_LOG call from AdSessionModel: " +
                    "minTime=$minTime, maxTime=$maxTime, minEventCount=$minEventCount, " +
                    "totalEventCount=$totalEventCount, lastRunTime=$lastRunTime"
        )
    }
}