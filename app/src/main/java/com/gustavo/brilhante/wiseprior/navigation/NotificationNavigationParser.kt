package com.gustavo.brilhante.wiseprior.navigation

import android.content.Intent
import com.gustavo.brilhante.notifications.EXTRA_TASK_ID
import javax.inject.Inject

class NotificationNavigationParser @Inject constructor() {
    fun getTaskId(intent: Intent?): Long? {
        val id = intent?.getLongExtra(EXTRA_TASK_ID, -1L) ?: return null
        return if (id > 0L) id else null
    }
}
