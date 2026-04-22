package com.gustavo.brilhante.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gustavo.brilhante.domain.usecase.GetTasksUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class RescheduleNotificationsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val getTasksUseCase: GetTasksUseCase,
    private val notificationScheduler: NotificationScheduler
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return runCatching {
            val tasks = getTasksUseCase().first()
            notificationScheduler.rescheduleAll(tasks)
            Result.success()
        }.getOrElse { Result.retry() }
    }
}
