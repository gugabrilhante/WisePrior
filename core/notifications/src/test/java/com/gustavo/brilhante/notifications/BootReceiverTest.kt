package com.gustavo.brilhante.notifications

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.gustavo.brilhante.domain.usecase.GetTasksUseCase
import com.gustavo.brilhante.model.Task
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [35])
class BootReceiverTest {

    private lateinit var context: Context
    private val receiver = BootReceiver()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        val config = Configuration.Builder()
            .setExecutor(SynchronousExecutor())
            .setWorkerFactory(TestWorkerFactoryForNotifications())
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @Test
    fun `given boot intent, when onReceive called, then enqueues RescheduleNotificationsWorker`() {
        receiver.onReceive(context, Intent(Intent.ACTION_BOOT_COMPLETED))

        // Process pending work
        ShadowLooper.idleMainLooper()

        val workInfos = WorkManager.getInstance(context)
            .getWorkInfos(
                WorkQuery.Builder
                    .fromTags(listOf(RescheduleNotificationsWorker::class.java.name))
                    .build()
            )
            .get()

        assertTrue(workInfos.isNotEmpty())
        // Worker should complete successfully with fake dependencies
        assertEquals(WorkInfo.State.SUCCEEDED, workInfos.first().state)
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
