package com.gustavo.brilhante.notifications

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.gustavo.brilhante.domain.usecase.GetTasksUseCase
import com.gustavo.brilhante.model.Priority
import com.gustavo.brilhante.model.Task
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RescheduleNotificationsWorkerTest {

    private val context: Context = mockk()
    private val workerParams: WorkerParameters = mockk(relaxed = true)
    private val getTasksUseCase: GetTasksUseCase = mockk()
    private val notificationScheduler: NotificationScheduler = mockk(relaxed = true)

    private val worker = RescheduleNotificationsWorker(
        context,
        workerParams,
        getTasksUseCase,
        notificationScheduler
    )

    @Test
    fun `given tasks exist, when doWork called, then reschedules all tasks and returns success`() = runTest {
        val tasks = listOf(
            Task(id = 1, title = "Task 1", priority = Priority.MEDIUM, createdAt = 1000L),
            Task(id = 2, title = "Task 2", priority = Priority.HIGH, createdAt = 1000L)
        )
        coEvery { getTasksUseCase() } returns flowOf(tasks)

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        coVerify(exactly = 1) { notificationScheduler.rescheduleAll(tasks) }
    }

    @Test
    fun `given use case throws error, when doWork called, then returns retry`() = runTest {
        coEvery { getTasksUseCase() } throws Exception("Data error")

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
    }
}
