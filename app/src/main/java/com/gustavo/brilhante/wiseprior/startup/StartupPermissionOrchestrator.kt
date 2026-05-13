package com.gustavo.brilhante.wiseprior.startup

import com.gustavo.brilhante.domain.system.AndroidVersionProvider
import com.gustavo.brilhante.domain.system.TestEnvironmentProvider
import javax.inject.Inject

class StartupPermissionOrchestrator @Inject constructor(
    private val versionProvider: AndroidVersionProvider,
    private val testEnvironmentProvider: TestEnvironmentProvider
) {
    fun getNextPermissionAction(
        notificationPermissionGranted: Boolean,
        canScheduleExactAlarms: Boolean
    ): StartupPermissionAction {
        // Notification Permission (Android 13+)
        if (versionProvider.sdkInt >= 33) { // TIRAMISU
            if (!notificationPermissionGranted) {
                return StartupPermissionAction.RequestNotificationPermission("android.permission.POST_NOTIFICATIONS")
            }
        }

        // Exact Alarm Permission (Android 12+)
        if (versionProvider.sdkInt >= 31 && !testEnvironmentProvider.isTesting()) { // S
            if (!canScheduleExactAlarms) {
                return StartupPermissionAction.RequestExactAlarmPermission
            }
        }

        return StartupPermissionAction.None
    }
}
