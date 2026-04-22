package com.gustavo.brilhante.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders for scheduled tasks"
            enableVibration(true)
            enableLights(true)
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    /**
     * Builds and shows a notification for the given task.
     *
     * Tapping the notification opens [MainActivity] and navigates to [TaskEditorRoute]
     * for the given [taskId] via an Intent extra.
     */
    fun showNotification(taskId: Long, title: String, notes: String) {
        val pendingIntent = buildLaunchIntent(taskId)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(notes.ifBlank { "Tap to view reminder" })
            .setStyle(NotificationCompat.BigTextStyle().bigText(notes))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(taskId.notificationId(), notification)
    }

    private fun buildLaunchIntent(taskId: Long): PendingIntent {
        // Launch the app's main activity; MainActivity extracts taskId and navigates to editor
        val intent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.apply {
                putExtra(EXTRA_TASK_ID, taskId)
                // Clear stack so back from editor returns to list, not the previous task
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            } ?: Intent().apply {
            // Fallback: should never happen, but keeps the PendingIntent valid
            putExtra(EXTRA_TASK_ID, taskId)
        }

        return PendingIntent.getActivity(
            context,
            taskId.notificationId(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

// Keep notification IDs in [1, Int.MAX_VALUE] and separate from alarm request codes
// by offsetting — avoids conflicts between different PendingIntent types
private fun Long.notificationId(): Int = ((this and 0x7FFFFFFF) + 1_000_000).toInt()
