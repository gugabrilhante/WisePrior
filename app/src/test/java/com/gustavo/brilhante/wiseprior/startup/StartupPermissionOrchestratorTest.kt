package com.gustavo.brilhante.wiseprior.startup

import com.gustavo.brilhante.domain.system.AndroidVersionProvider
import com.gustavo.brilhante.domain.system.TestEnvironmentProvider
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StartupPermissionOrchestratorTest {

    private val versionProvider = mockk<AndroidVersionProvider>()
    private val testEnvironmentProvider = mockk<TestEnvironmentProvider>()
    private val orchestrator = StartupPermissionOrchestrator(versionProvider, testEnvironmentProvider)

    @Test
    fun `when sdk is Tiramisu or higher and notification permission not granted, return RequestNotificationPermission`() {
        every { versionProvider.sdkInt } returns 33
        
        val result = orchestrator.getNextPermissionAction(
            notificationPermissionGranted = false,
            canScheduleExactAlarms = true
        )

        assertTrue(result is StartupPermissionAction.RequestNotificationPermission)
        assertEquals("android.permission.POST_NOTIFICATIONS", (result as StartupPermissionAction.RequestNotificationPermission).permission)
    }

    @Test
    fun `when sdk is Tiramisu or higher and notification permission granted, and sdk is S or higher and can't schedule exact alarms, return RequestExactAlarmPermission`() {
        every { versionProvider.sdkInt } returns 33
        every { testEnvironmentProvider.isTesting() } returns false
        
        val result = orchestrator.getNextPermissionAction(
            notificationPermissionGranted = true,
            canScheduleExactAlarms = false
        )

        assertEquals(StartupPermissionAction.RequestExactAlarmPermission, result)
    }

    @Test
    fun `when sdk is Tiramisu or higher and notification permission granted, and sdk is S or higher and can schedule exact alarms, return None`() {
        every { versionProvider.sdkInt } returns 33
        every { testEnvironmentProvider.isTesting() } returns false
        
        val result = orchestrator.getNextPermissionAction(
            notificationPermissionGranted = true,
            canScheduleExactAlarms = true
        )

        assertEquals(StartupPermissionAction.None, result)
    }

    @Test
    fun `when sdk is S and not testing and can't schedule exact alarms, return RequestExactAlarmPermission`() {
        every { versionProvider.sdkInt } returns 31
        every { testEnvironmentProvider.isTesting() } returns false
        
        val result = orchestrator.getNextPermissionAction(
            notificationPermissionGranted = true,
            canScheduleExactAlarms = false
        )

        assertEquals(StartupPermissionAction.RequestExactAlarmPermission, result)
    }

    @Test
    fun `when sdk is S and testing, return None even if can't schedule exact alarms`() {
        every { versionProvider.sdkInt } returns 31
        every { testEnvironmentProvider.isTesting() } returns true
        
        val result = orchestrator.getNextPermissionAction(
            notificationPermissionGranted = true,
            canScheduleExactAlarms = false
        )

        assertEquals(StartupPermissionAction.None, result)
    }

    @Test
    fun `when sdk is below S, return None`() {
        every { versionProvider.sdkInt } returns 30
        
        val result = orchestrator.getNextPermissionAction(
            notificationPermissionGranted = false,
            canScheduleExactAlarms = false
        )

        assertEquals(StartupPermissionAction.None, result)
    }
}
