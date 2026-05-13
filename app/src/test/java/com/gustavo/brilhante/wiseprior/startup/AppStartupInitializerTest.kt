package com.gustavo.brilhante.wiseprior.startup

import com.gustavo.brilhante.notifications.NotificationHelper
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class AppStartupInitializerTest {

    private val notificationHelper = mockk<NotificationHelper>(relaxed = true)
    private val initializer = AppStartupInitializer(notificationHelper)

    @Test
    fun `initialize should create notification channel`() {
        initializer.initialize()

        verify { notificationHelper.createChannel() }
        confirmVerified(notificationHelper)
    }
}
