package com.rzrasel.usagesapplication.libs

import com.rzrasel.adsession.model.AdSessionConfigModel

object AdMobInitializer {
    fun adSessionConfig() = AdSessionConfigModel(
        minTime1 = (2.0 * 60 * 1000).toLong(),
        minTime2 = (2.5 * 60 * 1000).toLong(),
        maxTime1 = (4.0 * 60 * 1000).toLong(),
        maxTime2 = (4.5 * 60 * 1000).toLong(),
        minEventCount1 = 8,
        minEventCount2 = 10,
        maxEventCount1 = 12,
        maxEventCount2 = 14,
    )
}