package com.rzrasel.adsession.manager

import android.util.Log
import com.rzrasel.adsession.model.AdSessionModel
import com.rzrasel.adsession.preferences.AppAdPreferences
import kotlinx.serialization.json.Json
import kotlin.random.Random

data class AdSessionConfigure(
    val minTime1: Long,
    val minTime2: Long,
    val maxTime1: Long,
    val maxTime2: Long,
    val minEventCount1: Int,
    val minEventCount2: Int,
    val maxEventCount1: Int,
    val maxEventCount2: Int,
    var minTime: Long = 0,
    var maxTime: Long = 0,
    var minEventCount: Int = 0,
    var maxEventCount: Int = 0,
    var totalEventCount: Int = 0,
    var lastRunTime: Long = System.currentTimeMillis(),
) {
    private var sessionModel: AdSessionModel = AdSessionModel()

    companion object {
        private const val TAG = "AdSessionConfigure"
        const val KEY_PREF = AdSessionModel.KEY_PREF
    }

    init {
        require(minTime1 < minTime2) { "minTime1 must be less than minTime2" }
        require(maxTime1 < maxTime2) { "maxTime1 must be less than maxTime2" }
        require(minEventCount1 < minEventCount2) { "minEventCount1 must be less than minEventCount2" }
        require(maxEventCount1 < maxEventCount2) { "maxEventCount1 must be less than maxEventCount2" }

        resetSession()
    }

    fun randomMinTime() = Random.nextLong(minTime1, minTime2 + 1)
    fun randomMaxTime() = Random.nextLong(maxTime1, maxTime2 + 1)
    fun randomMinEventCount() = Random.nextInt(minEventCount1, minEventCount2 + 1)
    fun randomMaxEventCount() = Random.nextInt(maxEventCount1, maxEventCount2 + 1)

    fun recalc() {
        minTime = randomMinTime()
        maxTime = randomMaxTime()
        minEventCount = randomMinEventCount()
        maxEventCount = randomMaxEventCount()
        sessionModel = sessionModel.copy(
            minTime = minTime,
            maxTime = maxTime,
            minEventCount = minEventCount,
            maxEventCount = maxEventCount,
            totalEventCount = totalEventCount,
            lastRunTime = lastRunTime
        )
    }

    fun resetSession() {
        recalc()
        totalEventCount = 0
        lastRunTime = System.currentTimeMillis()
    }

    fun updateLastRunTime(time: Long = System.currentTimeMillis()) {
        sessionModel.updateLastRunTime(time)
    }

    fun updateTotalEventCount(count: Int) {
        sessionModel.updateTotalEventCount(count)
    }

    fun incrementEventCount(count: Int = 1) {
        this.totalEventCount = count
        sessionModel.updateTotalEventCount(count)
    }

    fun isMinTimePassed(currentTime: Long = System.currentTimeMillis()): Boolean {
        return (currentTime - sessionModel.lastRunTime) >= sessionModel.minTime
    }

    fun isMaxTimePassed(currentTime: Long = System.currentTimeMillis()): Boolean {
        return (currentTime - sessionModel.lastRunTime) >= sessionModel.maxTime
    }

    fun isMinEventCountReached(): Boolean {
        return sessionModel.totalEventCount >= sessionModel.minEventCount
    }

    fun isMaxEventCountReached(): Boolean {
        return sessionModel.totalEventCount >= sessionModel.maxEventCount
    }

    fun toJson(): String = Json.encodeToString(this)

    fun loadPreferencesJson(): String {
        return AppAdPreferences.get(KEY_PREF, "")
    }

    fun savePreferences() {
        sessionModel.savePreferences()
    }

    fun loadPreferences(): AdSessionConfigure {
        sessionModel.loadPreferences().also { data ->
            minTime = data.minTime
            maxTime = data.maxTime
            minEventCount = data.minEventCount
            maxEventCount = data.maxEventCount
            totalEventCount = data.totalEventCount
            lastRunTime = data.lastRunTime
        }
        return this
    }

    fun logConfigState(customTag: String? = null) {
        val tag = customTag ?: TAG

        Log.d(
            tag,
            """
        ── AdSessionConfigure State ──
        Range:
          minTime   : [$minTime1 .. $minTime2]
          maxTime   : [$maxTime1 .. $maxTime2]
          minEvent  : [$minEventCount1 .. $minEventCount2]
          maxEvent  : [$maxEventCount1 .. $maxEventCount2]

        Current:
          minTime        = $minTime
          maxTime        = $maxTime
          minEventCount  = $minEventCount
          maxEventCount  = $maxEventCount
          totalEvents    = $totalEventCount
          lastRunTime    = $lastRunTime
        ────────────────────────────
        """.trimIndent()
        )
    }

    fun logConfigStateV1(customTag: String? = null) {
        Log.d(
            TAG,
            "DEBUG_LOG AdSession State → " +
                    "minTime1=$minTime1, minTime2=$minTime2, maxTime1=$maxTime1, maxTime2=$maxTime2, " +
                    "minEventCount1=$minEventCount1, minEventCount2=$minEventCount2, " +
                    "minTime=$minTime, maxTime=$maxTime, minEventCount=$minEventCount, " +
                    "totalEventCount=$totalEventCount, lastRunTime=$lastRunTime"
        )
    }
}