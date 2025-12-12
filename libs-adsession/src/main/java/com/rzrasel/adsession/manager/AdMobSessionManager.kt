package com.rzrasel.adsession.manager

import android.util.Log
import com.rzrasel.adsession.preferences.AppAdPreferences

class AdMobSessionManager() {
    private var isCallConfig: Boolean = false
    var sessionConfigure: AdSessionConfigure = AdSessionConfigure(
        minTime1 = 0L,
        minTime2 = 0L,
        maxTime1 = 0L,
        maxTime2 = 0L,
        minEventCount1 = 0,
        minEventCount2 = 0,
    )

    // Default session config
    private var minTime1: Long = 2 * 60 * 1000L
    private var minTime2: Long = 3 * 60 * 1000L
    private var maxTime1: Long = 5 * 60 * 1000L
    private var maxTime2: Long = 7 * 60 * 1000L
    private var minEventCount1: Int = 10
    private var minEventCount2: Int = 14
    private var totalEventCount: Int = 0
    var lastRunTime: Long = System.currentTimeMillis()

    // Callback when an ad should run
    private var adCallback: (() -> Unit)? = null

    companion object {
        private const val KEY_PREF = "ad_mob_session"
        private const val TAG = "AdMobSessionManager"
    }

    /** Configure session parameters */
    fun configureSession(
        minTime1: Long,
        minTime2: Long,
        maxTime1: Long,
        maxTime2: Long,
        minEventCount1: Int,
        minEventCount2: Int,
        totalEventCount: Int = 0,
        lastRunTime: Long = System.currentTimeMillis(),
    ) {
        /*this.minTime1 = minTime1
        this.minTime2 = minTime2
        this.maxTime1 = maxTime1
        this.maxTime2 = maxTime2
        this.minEventCount1 = minEventCount1
        this.minEventCount2 = minEventCount2
        adSessionConfigure.minTime = adSessionConfigure.randomMinTime()
        adSessionConfigure.maxTime = adSessionConfigure.randomMaxTime()
        adSessionConfigure.minEventCount = adSessionConfigure.randomMinEventCount()
        adSessionConfigure.totalEventCount = totalEventCount
        adSessionConfigure.lastRunTime = lastRunTime*/
        sessionConfigure = AdSessionConfigure(
            minTime1 = minTime1,
            minTime2 = minTime2,
            maxTime1 = maxTime1,
            maxTime2 = maxTime2,
            minEventCount1 = minEventCount1,
            minEventCount2 = minEventCount2,
            minTime = sessionConfigure.randomMinTime(),
            maxTime = sessionConfigure.randomMaxTime(),
            minEventCount = sessionConfigure.randomMinEventCount(),
            totalEventCount = totalEventCount,
            lastRunTime = lastRunTime,
        )
        isCallConfig = true
    }

    /** Set callback to run when ad triggers */
    fun setAdCallback(callback: () -> Unit) {
        adCallback = callback
    }

    fun initSession() {
        if(!isCallConfig) {
            configureSession(
                minTime1 = this.minTime1,
                minTime2 = this.minTime2,
                maxTime1 = this.maxTime1,
                maxTime2 = this.maxTime2,
                minEventCount1 = this.minEventCount1,
                minEventCount2 = this.minEventCount2,
                totalEventCount = this.totalEventCount,
                lastRunTime = this.lastRunTime,
            )
        }
        val json = sessionConfigure.loadPreferencesJson()
        if (json.isBlank()) {
            sessionConfigure.savePreferences()
            Log.d("DEBUG_LOG", "DEBUG_LOG initSession isBlank")
        } else {
            Log.d("DEBUG_LOG", "DEBUG_LOG initSession !isBlank")
            processEventTrigger()
        }
    }

    /** Update total event count */
    fun setTotalEventCount(count: Int) {
        totalEventCount = count
        sessionConfigure.updateTotalEventCount(count)
        processEventTrigger()
        saveSession()
    }

    /** Increment event count and check if ad should run */
    fun addEvent(count: Int = 1) {
        /*Log.d("DEBUG_LOG", "DEBUG_LOG addEvent(count: Int = 1)")
        val adSessionConfigure = sessionConfigure.loadPreferences()
        val addCount = adSessionConfigure.totalEventCount + 1
        Log.d("DEBUG_LOG", "DEBUG_LOG addEvent(count: Int = 1) ${adSessionConfigure.totalEventCount} - $addCount")*/
        this.totalEventCount = count
        sessionConfigure.incrementEventCount(count)
        processEventTrigger()
        saveSession()
    }

    // -------------------------------------
    // INTERNAL: EVENT TRIGGER LOGIC
    // -------------------------------------
    /** Check conditions to trigger ad */
    private fun processEventTrigger() {
        val adSessionConfigure = sessionConfigure.loadPreferences()
        val now = System.currentTimeMillis()

        val minTimePassed = sessionConfigure.isMinTimePassed(now)
        val maxTimePassed = sessionConfigure.isMaxTimePassed(now)
        val minEventReached = sessionConfigure.isMinEventCountReached()

        Log.d(
            TAG,
            "DEBUG_LOG Check: minTime=$minTimePassed | maxTime=$maxTimePassed | minEvents=$minEventReached | count=${sessionConfigure.totalEventCount}"
        )

        // Case 1: Max time ALWAYS forces ad
        if (maxTimePassed) {
            Log.d("DEBUG_LOG", "DEBUG_LOG processEventTrigger maxTimePassed")
            runAdAndReset(adSessionConfigure)
            return
        }

        // Case 2: Minimum time + minimum events
        if (minTimePassed && minEventReached) {
            Log.d("DEBUG_LOG", "DEBUG_LOG processEventTrigger minTimePassed && minEventReached")
            runAdAndReset(adSessionConfigure)
            return
        }
    }

    /** Execute ad callback and reset session counters */
    fun runAdAndReset(adSessionConfigure: AdSessionConfigure) {
        adCallback?.invoke()

        adSessionConfigure.lastRunTime = System.currentTimeMillis()
        adSessionConfigure.totalEventCount = 0

        adSessionConfigure.updateLastRunTime(time = System.currentTimeMillis())
        adSessionConfigure.updateTotalEventCount(0)
        adSessionConfigure.resetSession()

        saveSession()
    }

    // -----------------------------
    // PREFERENCE STORAGE
    // -----------------------------
    /** Save session to SharedPreferences */
    private fun saveSession(adSessionConfigure: AdSessionConfigure = sessionConfigure) {

        adSessionConfigure.savePreferences()
    }

    /** Remaining time until minimum interval condition becomes valid */
    fun getRemainingMinTime(): Long {
        val session = sessionConfigure
        val now = System.currentTimeMillis()
        val remaining = (session.lastRunTime + session.minTime) - now
        return remaining.coerceAtLeast(0)
    }

    /** Remaining time until maximum interval forces ad */
    fun getRemainingMaxTime(): Long {
        val session = sessionConfigure
        val now = System.currentTimeMillis()
        val remaining = (session.lastRunTime + session.maxTime) - now
        return remaining.coerceAtLeast(0)
    }

    /** How many events are left to reach the required minimum event count */
    fun getRemainingEventCount(): Int {
        val session = sessionConfigure
        val remaining = session.minEventCount - session.totalEventCount
        return remaining.coerceAtLeast(0)
    }

    fun logConfig() {
        Log.d("DEBUG_LOG", "DEBUG_LOG call from AdMobSessionManager minTime1=$minTime1, minTime2=$minTime2, maxTime1=$maxTime1, maxTime2=$maxTime2, minEventCount1=$minEventCount1, minEventCount2=$minEventCount2")
    }

    /** Debug log for remaining times and event count */
    fun logRemainingStatus() {
        val minTimeLeft = getRemainingMinTime()
        val maxTimeLeft = getRemainingMaxTime()
        val eventLeft = getRemainingEventCount()

        AppAdPreferences.printKey(KEY_PREF)

        Log.d(
            TAG,
            "DEBUG_LOG Remaining → minTimeLeft=${minTimeLeft}ms, maxTimeLeft=${maxTimeLeft}ms, eventLeft=$eventLeft"
        )
    }
}