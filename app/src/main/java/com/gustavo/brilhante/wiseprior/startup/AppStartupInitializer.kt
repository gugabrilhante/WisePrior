package com.gustavo.brilhante.wiseprior.startup

import com.gustavo.brilhante.notifications.NotificationHelper
import javax.inject.Inject

class AppStartupInitializer @Inject constructor(
    private val notificationHelper: NotificationHelper
) {
    fun initialize() {
        notificationHelper.createChannel()
    }
}
