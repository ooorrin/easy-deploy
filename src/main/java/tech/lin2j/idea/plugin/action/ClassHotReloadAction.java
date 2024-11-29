package tech.lin2j.idea.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.exception.IllegalFileTypeException;
import tech.lin2j.idea.plugin.ui.dialog.ClassRetransformDialog;
import tech.lin2j.idea.plugin.uitl.MessagesBundle;

import javax.swing.SwingUtilities;

/**
 * @author linjinjia
 * @date 2024/10/20 15:31
 */
public class ClassHotReloadAction extends AnAction {

    public ClassHotReloadAction() {
        super(MessagesBundle.getText("dialog.retransform.frame"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            Project project = e.getProject();
            assert project != null;
            ClassRetransformDialog dialog = new ClassRetransformDialog(project, e);
            dialog.setData();
            dialog.show();
        } catch (IllegalFileTypeException err) {
            Messages.showErrorDialog("Class reload only supports Java files", "File Type Error");
        }

    }
}
