package co.tula.mermaidchart.utils

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project

fun Project.notifyError(content: String, e: Throwable) {
    thisLogger().warn(content, e)

    val notification = NotificationGroupManager.getInstance()
        .getNotificationGroup("mermaidcharts.notificationGroup")
        .createNotification(content, NotificationType.ERROR)

    ActionManager.getInstance().getAction("OpenLog")?.also {
        notification.addAction(it)
    }

    notification.notify(this)
}