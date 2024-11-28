package tech.lin2j.idea.plugin.action;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.service.ISshService;
import tech.lin2j.idea.plugin.service.impl.PluginNotificationService;
import tech.lin2j.idea.plugin.service.impl.SshjSshService;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ssh.SshStatus;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author linjinjia
 * @date 2024/11/28 22:27
 */
public class TestConnectionAction implements ActionListener {
    private final SshServer sshServer;
    private final Project project;
    private final ISshService sshService;
    private final PluginNotificationService notificationService;

    public TestConnectionAction(Project project, SshServer server) {
        this.sshServer = server;
        this.project = project;
        sshService = ApplicationManager.getApplication().getService(ISshService.class);
        notificationService = ApplicationManager.getApplication().getService(PluginNotificationService.class);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String title = String.format("Testing %s:%s", sshServer.getIp(), sshServer.getPort());
        ProgressManager.getInstance().run(new Task.Backgroundable(project, title) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                SshStatus status = sshService.isValid(sshServer);

                String title = MessagesBundle.getText("dialog.panel.host.test-connect.title");
                String tip = MessagesBundle.getText("dialog.panel.host.test-connect.tip");
                String msg = status.isSuccess() ? tip : status.getMessage();
                notificationService.showNotification(project, title, msg);
            }
        });
    }
}
