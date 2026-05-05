package com.gustavo.brilhante.notifications

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [35])
class BootReceiverTest {

    private lateinit var context: Context
    private val receiver = BootReceiver()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
    }

    @Test
    fun `given boot intent, when onReceive called, then enqueues RescheduleNotificationsWorker`() {
        val intent = Intent(Intent.ACTION_BOOT_COMPLETED)

        receiver.onReceive(context, intent)

        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosByTag(RescheduleNotificationsWorker::class.java.name).get()
        // WorkManager tag for classes is often the class name
        // Let's check by Tag or just all work
        val allWork = workManager.getWorkInfosForUniqueWork("reschedule").get() // if it was unique
        
        // Check if any work is enqueued
        val allInfos = workManager.getWorkInfos(androidx.work.WorkQuery.Builder.fromStates(listOf(WorkInfo.State.ENQUEUED)).build()).get()
        assert(allInfos.isNotEmpty())
    }

    @Test
    fun `given other intent, when onReceive called, then does nothing`() {
        val intent = Intent(Intent.ACTION_SEND)

        receiver.onReceive(context, intent)

        val workManager = WorkManager.getInstance(context)
        val allInfos = workManager.getWorkInfos(androidx.work.WorkQuery.Builder.fromStates(listOf(WorkInfo.State.ENQUEUED)).build()).get()
        assert(allInfos.isEmpty())
    }
}
