package com.gustavo.brilhante.wiseprior

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
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
import com.gustavo.brilhante.wiseprior.navigation.NotificationNavigationParser
import com.gustavo.brilhante.wiseprior.navigation.WisePriorNavHost
import com.gustavo.brilhante.wiseprior.startup.StartupPermissionAction
import com.gustavo.brilhante.wiseprior.startup.StartupPermissionOrchestrator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var permissionOrchestrator: StartupPermissionOrchestrator
    @Inject lateinit var navigationParser: NotificationNavigationParser

    // taskId to open on start/resume, driven by notification tap
    private var pendingTaskId by mutableStateOf<Long?>(null)

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* granted state handled by OS */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        requestPermissionsIfNeeded()
        pendingTaskId = navigationParser.getTaskId(intent)

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
    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingTaskId = navigationParser.getTaskId(intent)
    }

    private fun requestPermissionsIfNeeded() {
        val notificationPermissionGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        val alarmManager = getSystemService(AlarmManager::class.java)
        val canScheduleExactAlarms = alarmManager?.canScheduleExactAlarms() ?: true

        when (val action = permissionOrchestrator.getNextPermissionAction(
            notificationPermissionGranted,
            canScheduleExactAlarms
        )) {
            is StartupPermissionAction.RequestNotificationPermission -> {
                notificationPermissionLauncher.launch(action.permission)
            }
            is StartupPermissionAction.RequestExactAlarmPermission -> {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
            StartupPermissionAction.None -> { /* Do nothing */ }
        }
    }
}
