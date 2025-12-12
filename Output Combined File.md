/ File: AdMobSessionManager.kt **/

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

/ File: AdSessionConfigure.kt **/

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
    var minTime: Long = 0,
    var maxTime: Long = 0,
    var minEventCount: Int = 0,
    var totalEventCount: Int = 0,
    var lastRunTime: Long = System.currentTimeMillis(),
) {
    private var sessionModel: AdSessionModel = AdSessionModel()

    companion object {
        private const val TAG = "AdSessionConfigure"
        const val KEY_PREF = AdSessionModel.KEY_PREF
    }

    init {
        require(minTime1 <= minTime2) { "minTime1 must be <= minTime2" }
        require(maxTime1 <= maxTime2) { "maxTime1 must be <= maxTime2" }
        require(minEventCount1 <= minEventCount2) { "minEventCount1 must be <= minEventCount2" }

        resetSession()
    }

    fun randomMinTime() = Random.nextLong(minTime1, minTime2 + 1)
    fun randomMaxTime() = Random.nextLong(maxTime1, maxTime2 + 1)
    fun randomMinEventCount() = Random.nextInt(minEventCount1, minEventCount2 + 1)

    fun recalc() {
        minTime = randomMinTime()
        maxTime = randomMaxTime()
        minEventCount = randomMinEventCount()
        sessionModel = sessionModel.copy(
            minTime = minTime,
            maxTime = maxTime,
            minEventCount = minEventCount,
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
            totalEventCount = data.totalEventCount
            lastRunTime = data.lastRunTime
        }
        return this
    }

    fun logConfigState(customTag: String? = null) {
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

/ File: AdSessionConfigModel.kt **/

package com.rzrasel.adsession.model

data class AdSessionConfigModel(
    val minTime1: Long,
    val minTime2: Long,
    val maxTime1: Long,
    val maxTime2: Long,
    val minEventCount1: Int,
    val minEventCount2: Int,
)

/ File: AdSessionModel.kt **/

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

/ File: AppAdPreferences.kt **/

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

/ File: AdUiState.kt **/

package com.rzrasel.adsession.state

sealed class AdUiState {
    object NotInitialized : AdUiState()
    object ReadyForAdTriggers : AdUiState()
    object ShowAd : AdUiState()
}

/ File: AdSessionCountdownTracker.kt **/

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

/ File: AdMobSharedViewModel.kt **/

package com.rzrasel.adsession.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rzrasel.adsession.model.AdSessionConfigModel
import com.rzrasel.adsession.state.AdUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdMobSharedViewModel(
    private val adSessionViewModel: AdSessionViewModel,
    private val configModel: AdSessionConfigModel,
) : ViewModel() {

    // State for all ad-related UI states
    private val _appAdUiState = MutableStateFlow<AdUiState>(AdUiState.NotInitialized)
    val appAdUiState: StateFlow<AdUiState> = _appAdUiState.asStateFlow()

    init {
        Log.d("AdMobSharedModel", "DEBUG_LOG AdMobSharedModel init started")

        viewModelScope.launch {
            //kotlinx.coroutines.delay(100)

            Log.d("AdMobSharedModel", "DEBUG_LOG Starting ad trigger flow collection")
            appAdUiState.collect { adState ->
                when (adState) {
                    AdUiState.ReadyForAdTriggers -> {
                        adSessionViewModel.adTriggeredFlow.collect { triggered ->
                            if (triggered) {
                                triggerAd()
                                adSessionViewModel.resetAdTrigger()
                            }
                        }
                    }
                    else -> {
                        // Not ready for ad triggers yet
                    }
                }
            }
        }

        Log.d("AdMobSharedModel", "DEBUG_LOG AdMobSharedModel init completed")
    }

    /** Mark as ready for ad triggers */
    fun markReadyForAdTriggers() {
        Log.d("AdMobSharedModel", "DEBUG_LOG markReadyForAdTriggers called")
        _appAdUiState.value = AdUiState.ReadyForAdTriggers
    }

    fun onEventAction() {
        Log.d("AdMobSharedModel", "DEBUG_LOG Event action triggered")
        adSessionViewModel.addEvent(1)
        /*viewModelScope.launch {
            kotlinx.coroutines.delay(100)
            logRemainingStatus()
        }*/
    }

    /** Trigger ad display */
    fun triggerAd() {
        Log.d("AdMobSharedModel", "DEBUG_LOG AdMobSharedModel triggerAd() called")
        _appAdUiState.value = AdUiState.ShowAd
    }

    /** Call this from composable after showing the ad to reset the state */
    fun onAdShown() {
        Log.d("AdMobSharedModel", "DEBUG_LOG onAdShown called - resetting to ReadyForAdTriggers")
        _appAdUiState.value = AdUiState.ReadyForAdTriggers
    }

    fun logRemainingStatus() {
        Log.d("AdMobSharedModel", "DEBUG_LOG Requesting remaining status from ad session")
        adSessionViewModel.logRemainingStatus()
    }

    // Clean up method
    override fun onCleared() {
        super.onCleared()
        Log.d("AdMobSharedModel", "DEBUG_LOG AdMobSharedModel cleared")
        _appAdUiState.value = AdUiState.NotInitialized
    }
}

/ File: AdSessionTrackerViewModel.kt **/

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

/ File: AdSessionViewModel.kt **/

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
                _adTriggeredFlow.value = true
                Log.d("AdSessionViewModel", "DEBUG_LOG Ad triggered callback called")
            }
        }
        configureSession()
        adSessionManager.initSession()
        refreshFlows()
    }

    /*fun configureSession(
        minTime1: Long = (5.0 * 60 * 1000).toLong(),
        minTime2: Long = (5.5 * 60 * 1000).toLong(),
        maxTime1: Long = (10.0 * 60 * 1000).toLong(),
        maxTime2: Long = (10.5 * 60 * 1000).toLong(),
        minEventCount1: Int = 10,
        minEventCount2: Int = 14,
    )*/

    fun configureSession(config: AdSessionConfigModel) {
        adSessionManager.configureSession(
            minTime1 = config.minTime1,
            minTime2 = config.minTime2,
            maxTime1 = config.maxTime1,
            maxTime2 = config.maxTime2,
            minEventCount1 = config.minEventCount1,
            minEventCount2 = config.minEventCount2
        )
        adSessionManager.initSession()
        refreshFlows()
    }

    /*fun configureSession(
        minTime1: Long = (2.0 * 60 * 1000).toLong(),
        minTime2: Long = (2.5 * 60 * 1000).toLong(),
        maxTime1: Long = (4.0 * 60 * 1000).toLong(),
        maxTime2: Long = (4.5 * 60 * 1000).toLong(),
        *//*maxTime1: Long = (9.0 * 60 * 1000).toLong(),
        maxTime2: Long = (9.5 * 60 * 1000).toLong(),*//*
        minEventCount1: Int = 10,
        minEventCount2: Int = 14,
    ) {
        adSessionManager.configureSession(
            minTime1, minTime2,
            maxTime1, maxTime2,
            minEventCount1, minEventCount2
        )
        refreshFlows()
    }*/

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

- all working nice, don't change codebase structure, and class name
- make a data class AdSessionConfigModel
- AdSessionConfigModel pass in AdMobSharedViewModel
- provide full codebase