package com.rzrasel.adsession.tracker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.rzrasel.adsession.manager.AdMobSessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max

class AdSessionCountdownTracker(
    private val adSessionManager: AdMobSessionManager,
    private val coroutineScope: CoroutineScope,
    // 1️⃣ Called when ad must run
    private val adTriggerCallback: (() -> Unit)? = null,
    // 2️⃣ Called every second
    private val adTrackerCallback: (() -> Unit)? = null
) {

    private val _minTimeFlow = MutableStateFlow(0L)
    val minTimeFlow: StateFlow<Long> = _minTimeFlow

    private val _maxTimeFlow = MutableStateFlow(0L)
    val maxTimeFlow: StateFlow<Long> = _maxTimeFlow

    private val _remainingEventFlow = MutableStateFlow(0)
    val remainingEventFlow: StateFlow<Int> = _remainingEventFlow

    private val _minTimeLiveData = MutableLiveData(0L)
    val minTimeLiveData: LiveData<Long> = _minTimeLiveData

    private val _maxTimeLiveData = MutableLiveData(0L)
    val maxTimeLiveData: LiveData<Long> = _maxTimeLiveData

    private val _remainingEventLiveData = MutableLiveData(0)
    val remainingEventLiveData: LiveData<Int> = _remainingEventLiveData

    private var job: Job? = null
    private val tickDelay = 1000L

    fun start() {
        stop()

        job = coroutineScope.launch(Dispatchers.Default) {
            while (isActive) {

                val minLeft = max(0L, adSessionManager.getRemainingMinTime())
                val maxLeft = max(0L, adSessionManager.getRemainingMaxTime())
                val eventLeft = max(0, adSessionManager.getRemainingEventCount())

                // Update flows / Livedata
                _minTimeFlow.value = minLeft
                _maxTimeFlow.value = maxLeft
                _remainingEventFlow.value = eventLeft

                _minTimeLiveData.postValue(minLeft)
                _maxTimeLiveData.postValue(maxLeft)
                _remainingEventLiveData.postValue(eventLeft)

                // 2️⃣ Call tracker callback every second
                adTrackerCallback?.invoke()

                // Check ad trigger logic
                val sessionConfigure = adSessionManager.sessionConfigure
                val now = System.currentTimeMillis()

                val isMaxTime = sessionConfigure.isMaxTimePassed(now)
                val isMinTimeAndEvent =
                    sessionConfigure.isMinTimePassed(now) && sessionConfigure.isMinEventCountReached()

                if (isMaxTime || isMinTimeAndEvent) {
                    adTriggerCallback?.invoke()
                }

                delay(tickDelay)
            }
        }
    }

    fun stop() {
        job?.cancel()
    }
}