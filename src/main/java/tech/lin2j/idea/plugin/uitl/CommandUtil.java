package tech.lin2j.idea.plugin.uitl;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.file.ConsoleTransferListener;
import tech.lin2j.idea.plugin.file.filter.ConsoleFileFilter;
import tech.lin2j.idea.plugin.file.filter.ExtensionFilter;
import tech.lin2j.idea.plugin.file.filter.FileFilter;
import tech.lin2j.idea.plugin.model.Command;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.model.UploadProfile;
import tech.lin2j.idea.plugin.service.ISshService;
import tech.lin2j.idea.plugin.service.impl.SshjSshService;
import tech.lin2j.idea.plugin.ssh.CommandLog;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ssh.SshStatus;

import java.time.LocalDateTime;

import static com.intellij.openapi.ui.DialogWrapper.OK_EXIT_CODE;

/**
 * @author linjinjia
 * @date 2022/5/7 08:58
 */
public class CommandUtil {

    public static void executeUpload(@NotNull Project project, @NotNull UploadProfile profile,
                                     @NotNull SshServer server, @NotNull DialogWrapper dialogWrapper) {
        closeDialog(dialogWrapper);
        showToolWindow(project);
        // Prevent blocking UI thread
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            CommandLog commandLog = project.getUserData(CommandLog.COMMAND_LOG_KEY);
            assert commandLog != null;
            executeUpload(profile, server, commandLog);
        });
    }

    public static void executeCommand(@NotNull Project project, Command command,
                                      SshServer server, DialogWrapper dialogWrapper) {
        if (dialogWrapper != null) {
            closeDialog(dialogWrapper);
        }
        showToolWindow(project);
        CommandLog commandLog = project.getUserData(CommandLog.COMMAND_LOG_KEY);
        assert commandLog != null;

        executeCommand(command, server, commandLog);
    }

    public static void executeUpload(UploadProfile profile, SshServer server, CommandLog commandLog) {
        String localFile = profile.getFile();
        String remoteTargetDir = profile.getLocation();
        String exclude = profile.getExclude();
        String initMsg = "Upload [" + localFile + "] to [" + remoteTargetDir + "]";
        FileFilter filter = new ConsoleFileFilter(new ExtensionFilter(exclude), commandLog);
        ConsoleTransferListener listener = new ConsoleTransferListener(localFile, commandLog);

        commandLog.info(initMsg);
        ISshService sshService = ApplicationManager.getApplication().getService(ISshService.class);
        SshStatus status = sshService.upload(filter, server, localFile, remoteTargetDir, listener);

        if (!status.isSuccess()) {
            commandLog.error("Upload failed: " + status.getMessage());
        } else if (profile.getCommandId() != null) {
            Command command = ConfigHelper.getCommandById(profile.getCommandId());
            executeCommand(command, server, commandLog);
        } else {
            printFinished(commandLog);
        }
    }

    public static void executeCommand(Command command, SshServer server, CommandLog commandLog) {
        String cmdContent = command.generateCmdLine();
        commandLog.info(String.format("Execute command on %s:%s : {%s}", server.getIp(), server.getPort(), cmdContent));
        ISshService sshService = ApplicationManager.getApplication().getService(ISshService.class);
        sshService.executeAsync(commandLog, server, cmdContent);
    }

    private static void showToolWindow(Project project) {
        ToolWindow deployToolWindow = ToolWindowManager.getInstance(project).getToolWindow("Easy Deploy");
        deployToolWindow.activate(null);
        Content messages = deployToolWindow.getContentManager().findContent("Console");
        deployToolWindow.getContentManager().setSelectedContent(messages);
    }

    private static void closeDialog(DialogWrapper dialogWrapper) {
        dialogWrapper.close(OK_EXIT_CODE);
    }

    private static void printFinished(CommandLog commandLog) {
        commandLog.info("Finished at: " + LocalDateTime.now());
    }
}