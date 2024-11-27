package tech.lin2j.idea.plugin.ui.module;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.JBSplitter;

/**
 * @author linjinjia
 * @date 2022/4/24 17:01
 */
public class DashboardView extends SimpleToolWindowPanel {

    private final Project project;
    private final tech.lin2j.idea.plugin.ui.DashboardView consoleUi;

    public DashboardView(Project project) {
        super(false, true);
        this.project = project;
        this.consoleUi = new tech.lin2j.idea.plugin.ui.DashboardView(project);

        JBSplitter splitter = new JBSplitter(false);
        splitter.setSplitterProportionKey("main.splitter.key");
        splitter.setFirstComponent(consoleUi);
        splitter.setProportion(0.3f);
        setContent(splitter);
    }

    public Project getProject() {
        return project;
    }

    public tech.lin2j.idea.plugin.ui.DashboardView getConsoleUi() {
        return consoleUi;
    }
}