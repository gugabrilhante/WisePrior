package com.gustavo.brilhante.notifications

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
        receiver.onReceive(context, Intent(Intent.ACTION_BOOT_COMPLETED))

        val workInfos = WorkManager.getInstance(context)
            .getWorkInfos(
                WorkQuery.Builder
                    .fromTags(listOf(RescheduleNotificationsWorker::class.java.name))
                    .build()
            )
            .get()

        assertTrue(workInfos.isNotEmpty())
        assertEquals(WorkInfo.State.ENQUEUED, workInfos.first().state)
    }

    @Test
    fun `given other intent, when onReceive called, then does nothing`() {
        receiver.onReceive(context, Intent(Intent.ACTION_SEND))

        val workInfos = WorkManager.getInstance(context)
            .getWorkInfos(
                WorkQuery.Builder
                    .fromTags(listOf(RescheduleNotificationsWorker::class.java.name))
                    .build()
            )
            .get()

        assertTrue(workInfos.isEmpty())
    }
}
