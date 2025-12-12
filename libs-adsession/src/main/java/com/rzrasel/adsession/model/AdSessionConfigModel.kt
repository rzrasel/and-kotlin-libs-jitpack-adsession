package com.rzrasel.adsession.model

data class AdSessionConfigModel(
    val minTime1: Long,
    val minTime2: Long,
    val maxTime1: Long,
    val maxTime2: Long,
    val minEventCount1: Int,
    val minEventCount2: Int,
)