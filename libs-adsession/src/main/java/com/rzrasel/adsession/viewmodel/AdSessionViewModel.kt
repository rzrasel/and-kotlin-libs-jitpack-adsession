package com.rzrasel.adsession.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rzrasel.adsession.manager.AdMobSessionManager
import com.rzrasel.adsession.model.AdSessionConfigModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdSessionViewModel : ViewModel() {

    private val _minTimeFlow = MutableStateFlow(0L)
    val minTimeFlow: StateFlow<Long> = _minTimeFlow.asStateFlow()

    private val _maxTimeFlow = MutableStateFlow(0L)
    val maxTimeFlow: StateFlow<Long> = _maxTimeFlow.asStateFlow()

    private val _remainingEventFlow = MutableStateFlow(0)
    val remainingEventFlow: StateFlow<Int> = _remainingEventFlow.asStateFlow()

    private val _adTriggeredFlow = MutableStateFlow(false)
    val adTriggeredFlow: StateFlow<Boolean> = _adTriggeredFlow.asStateFlow()

    private val adSessionManager = AdMobSessionManager()

    init {
        adSessionManager.setAdCallback {
            viewModelScope.launch {
                Log.d("AdSessionViewModel", "DEBUG_LOG Ad triggered")
                _adTriggeredFlow.value = true
            }
        }

        // Default init (safe fallback)
        adSessionManager.initSession()
        refreshFlows()
    }

    // 🔥 CONFIG ENTRY POINT
    fun configureSession(config: AdSessionConfigModel) {
        Log.d("AdSessionViewModel", "DEBUG_LOG configureSession(config)")

        adSessionManager.configureSession(
            minTime1 = config.minTime1,
            minTime2 = config.minTime2,
            maxTime1 = config.maxTime1,
            maxTime2 = config.maxTime2,
            minEventCount1 = config.minEventCount1,
            minEventCount2 = config.minEventCount2,
            maxEventCount1 = config.maxEventCount1,
            maxEventCount2 = config.maxEventCount2,
        )

        adSessionManager.initSession()
        refreshFlows()
    }

    fun addEvent(count: Int = 1) {
        adSessionManager.addEvent(count)
        refreshFlows()
    }

    fun resetAdTrigger() {
        _adTriggeredFlow.value = false
    }

    private fun refreshFlows() {
        _minTimeFlow.value = adSessionManager.getRemainingMinTime()
        _maxTimeFlow.value = adSessionManager.getRemainingMaxTime()
        _remainingEventFlow.value = adSessionManager.getRemainingEventCount()
    }

    fun logRemainingStatus() {
        adSessionManager.logRemainingStatus()
    }
}