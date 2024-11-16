package tech.lin2j.idea.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.ui.dialog.ClassHotReloadDialog;

/**
 * @author linjinjia
 * @date 2024/10/20 15:31
 */
public class ClassHotReloadAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        new ClassHotReloadDialog(e.getProject(), e).show();
    }
}
