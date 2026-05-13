package com.gustavo.brilhante.wiseprior.startup

sealed class StartupPermissionAction {
    object None : StartupPermissionAction()
    data class RequestNotificationPermission(val permission: String) : StartupPermissionAction()
    object RequestExactAlarmPermission : StartupPermissionAction()
}
