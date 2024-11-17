package tech.lin2j.idea.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.exception.IllegalFileTypeException;
import tech.lin2j.idea.plugin.ui.dialog.ClassHotReloadDialog;

/**
 * @author linjinjia
 * @date 2024/10/20 15:31
 */
public class ClassHotReloadAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            new ClassHotReloadDialog(e.getProject(), e).show();
        } catch (IllegalFileTypeException err) {
            Messages.showErrorDialog("Class reload only supports Java files", "File Type Error");
        }

    }
}
