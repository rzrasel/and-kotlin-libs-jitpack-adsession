package com.rzrasel.adsession.state

sealed class AdUiState {
    object NotInitialized : AdUiState()
    object ReadyForAdTriggers : AdUiState()
    object ShowAd : AdUiState()
}