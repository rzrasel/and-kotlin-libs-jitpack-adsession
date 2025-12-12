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

        adSessionViewModel.configureSession(configModel)

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