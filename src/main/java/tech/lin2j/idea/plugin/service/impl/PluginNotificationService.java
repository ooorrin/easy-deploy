package tech.lin2j.idea.plugin.service.impl;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import tech.lin2j.idea.plugin.enums.Constant;

/**
 * @author linjinjia
 * @date 2024/11/17 19:31
 */
@Service
public final class PluginNotificationService {

    private final NotificationGroup notificationGroup;

    public PluginNotificationService() {
        notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup(Constant.EASY_DEPLOY);
    }

    public void showNotification(Project project, String title, String message) {
        Notification notification = notificationGroup.createNotification(title, message, NotificationType.INFORMATION);
        notification.setTitle(title);
        notification.setContent(message);
        notification.notify(project);
    }

    public void showNotification(Project project, String title, String message, AnAction action) {
        Notification notification = notificationGroup.createNotification(title, message, NotificationType.INFORMATION);
        notification.setTitle(title);
        notification.setContent(message);
        notification.addAction(action);
        notification.notify(project);
    }
}