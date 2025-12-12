package com.rzrasel.adsession.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rzrasel.adsession.manager.AdMobSessionManager
import com.rzrasel.adsession.preferences.AppAdPreferences
import com.rzrasel.adsession.tracker.AdSessionCountdownTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AdSessionTrackerViewModel : ViewModel() {
    private val KEY_PREF = "ad_mob_session"

    private val adSessionManager = AdMobSessionManager()

    private val _adTriggeredFlow = MutableStateFlow(false)
    val adTriggeredFlow: StateFlow<Boolean> get() = _adTriggeredFlow

    // Tracker with 2 callbacks: trigger + tracker tick
    private val tracker = AdSessionCountdownTracker(
        adSessionManager,
        viewModelScope,

        // 1️⃣ ad trigger callback
        adTriggerCallback = {
            adSessionManager.runAdAndReset(adSessionManager.sessionConfigure)
            _adTriggeredFlow.value = true
            Log.d("DEBUG_LOG", "DEBUG_LOG ----call from adTriggerCallback")
            AppAdPreferences.printKey(KEY_PREF)
        },

        // 2️⃣ every second callback
        adTrackerCallback = {
            // You may add logs if needed
            Log.d("DEBUG_LOG", "DEBUG_LOG call from adTrackerCallback")
            AppAdPreferences.printKey(KEY_PREF)
        }
    )

    val minTimeFlow get() = tracker.minTimeFlow
    val maxTimeFlow get() = tracker.maxTimeFlow
    val remainingEventFlow get() = tracker.remainingEventFlow

    init {
        configureSession()
        adSessionManager.initSession()
        tracker.start()
    }

    /*fun configureSession(
        minTime1: Long = (1.0 * 60 * 1000).toLong(),
        minTime2: Long = (1.4 * 60 * 1000).toLong(),
        maxTime1: Long = (1.5 * 60 * 1000).toLong(),
        maxTime2: Long = (2 * 60 * 1000).toLong(),
        minEventCount1: Int = 8,
        minEventCount2: Int = 10,
    ) {
        adSessionManager.configureSession(
            minTime1, minTime2,
            maxTime1, maxTime2,
            minEventCount1, minEventCount2
        )
        adSessionManager.initSession()
    }*/

    fun configureSession(
        minTime1: Long = (1.0 * 60 * 1000).toLong(),
        minTime2: Long = (1.4 * 60 * 1000).toLong(),
        maxTime1: Long = (1.5 * 60 * 1000).toLong(),
        maxTime2: Long = (2 * 60 * 1000).toLong(),
        minEventCount1: Int = 8,
        minEventCount2: Int = 10,
    ) {
        adSessionManager.configureSession(
            minTime1, minTime2,
            maxTime1, maxTime2,
            minEventCount1, minEventCount2
        )
        adSessionManager.initSession()
    }

    fun addEvent(count: Int = 1) {
        adSessionManager.addEvent(count)
    }

    fun resetAdTrigger() {
        _adTriggeredFlow.value = false
    }

    fun logRemainingStatus() {
        adSessionManager.logRemainingStatus()
    }

    override fun onCleared() {
        tracker.stop()
        super.onCleared()
    }
}