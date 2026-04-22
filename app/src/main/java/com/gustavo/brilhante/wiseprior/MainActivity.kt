package com.gustavo.brilhante.wiseprior

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.gustavo.brilhante.designsystem.theme.WisePriorTheme
import com.gustavo.brilhante.notifications.EXTRA_TASK_ID
import com.gustavo.brilhante.wiseprior.navigation.WisePriorNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // taskId to open on start/resume, driven by notification tap
    private var pendingTaskId by mutableStateOf<Long?>(null)

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* granted state handled by OS */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()
        pendingTaskId = intent.getTaskId()

        setContent {
            WisePriorTheme {
                WisePriorNavHost(
                    modifier = Modifier.fillMaxSize(),
                    initialTaskId = pendingTaskId
                )
            }
        }
    }

    /**
     * Called when the app is already running and a notification is tapped.
     * Updates [pendingTaskId] so the Composable reacts and navigates.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingTaskId = intent.getTaskId()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun Intent?.getTaskId(): Long? {
        val id = this?.getLongExtra(EXTRA_TASK_ID, -1L) ?: return null
        return if (id > 0L) id else null
    }
}
