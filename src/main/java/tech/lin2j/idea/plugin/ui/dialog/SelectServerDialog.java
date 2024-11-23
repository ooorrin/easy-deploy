package tech.lin2j.idea.plugin.ui.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.util.ui.FormBuilder;
import javafx.scene.control.ChoiceDialog;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.model.HotReloadPersistence;
import tech.lin2j.idea.plugin.ssh.SshServer;

import javax.swing.JComponent;

/**
 * @author linjinjia
 * @date 2024/11/23 10:47
 */
public class SelectServerDialog extends DialogWrapper {

    private final Project project;
    private final ComboBox<SshServer> sshServers;

    public SelectServerDialog(@Nullable Project project) {
        super(project);
        this.project = project;

        sshServers = new ComboBox<>();

        setTitle("Select Server");
        setSize(200, 0);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        sshServers.setModel(new CollectionComboBoxModel<>(ConfigHelper.sshServers()));
        return FormBuilder.createFormBuilder()
                .addComponent(sshServers)
                .getPanel();
    }

    @Override
    protected void doOKAction() {
        SshServer selectedValue = (SshServer) sshServers.getSelectedItem();
        if (selectedValue != null) {
            HotReloadPersistence state = project.getService(HotReloadPersistence.class).getState();
            state.setSshId(selectedValue.getId());
        }
        super.doOKAction();
    }
}