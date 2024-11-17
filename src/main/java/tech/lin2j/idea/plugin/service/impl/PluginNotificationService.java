package tech.lin2j.idea.plugin.service.impl;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.components.Service;

/**
 * @author linjinjia
 * @date 2024/11/17 19:31
 */
@Service
public final class PluginNotificationService {

    public void showHotReloadNotification(String title, String message) {
        Notification notification = NotificationGroup.balloonGroup("ed-hot-reload").createNotification();
        notification.setTitle(title);
        notification.setContent(message);
        notification.notify();
    }
}