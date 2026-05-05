package com.gustavo.brilhante.notifications

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.gustavo.brilhante.domain.repository.TaskRepository
import com.gustavo.brilhante.domain.usecase.GetTasksUseCase
import com.gustavo.brilhante.model.Task
import kotlinx.coroutines.flow.flowOf

/**
 * Test WorkerFactory that provides mock dependencies to workers.
 */
class TestWorkerFactoryForNotifications : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            RescheduleNotificationsWorker::class.java.name -> {
                // Provide fake dependencies
                val fakeRepository = object : TaskRepository {
                    override fun getTasks() = flowOf(emptyList<Task>())
                    override suspend fun getTaskById(id: Long): Task? = null
                    override suspend fun addTask(task: Task) {}
                    override suspend fun updateTask(task: Task) {}
                    override suspend fun deleteTask(task: Task) {}
                }

                val getTasksUseCase = GetTasksUseCase(fakeRepository)
                val notificationScheduler = object : NotificationScheduler {
                    override fun schedule(task: Task) {}
                    override fun rescheduleAll(tasks: List<Task>) {}
                    override fun cancel(taskId: Long) {}
                }

                RescheduleNotificationsWorker(
                    appContext,
                    workerParameters,
                    getTasksUseCase,
                    notificationScheduler
                )
            }
            else -> null
        }
    }
}

