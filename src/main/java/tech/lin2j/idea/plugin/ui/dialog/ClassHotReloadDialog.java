package tech.lin2j.idea.plugin.ui.dialog;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import icons.MyIcons;
import org.apache.commons.lang3.RandomUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.model.HotReloadPersistence;
import tech.lin2j.idea.plugin.service.IHotReloadService;
import tech.lin2j.idea.plugin.ssh.JavaProcess;
import tech.lin2j.idea.plugin.ssh.SshConnectionManager;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ssh.sshj.SshjConnection;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author linjinjia
 * @date 2024/11/7 21:44
 */
public class ClassHotReloadDialog extends DialogWrapper {
    public static final Key<SshjConnection> SSHJ_CONNECTION = Key.create("EDHotReload.SSHJ_CONNECTION");
    public static final Key<List<JavaProcess>> JAVA_PROCESSES = Key.create("EDHotReload.JAVA_PROCESSES");

    private final Project project;
    private final AnActionEvent event;
    private final JPanel root;
    private final HotReloadPersistence projectConfig;
    private final IHotReloadService hotReloadService;
    private final JBLabel processChangedTip = new JBLabel();
    private Integer sshId;
    private ComboBox<SshServer> serverComboBox;
    private ComboBox<JavaProcess> javaProcessComboBox;
    private JPanel processRefreshContainer;
    private JPanel processBindContainer;
    private JSpinner arthasHttpPortInput;
    private SshjConnection sshjConnection;
    private List<JavaProcess> javaProcesses;
    private JBLabel fileLabel;

    public ClassHotReloadDialog(Project project, AnActionEvent event) {
        super(project, true);
        this.project = project;
        this.event = event;
        this.hotReloadService = ServiceManager.getService(IHotReloadService.class);
        this.projectConfig = this.project.getService(HotReloadPersistence.class).getState();
        assert projectConfig != null;
        this.sshId = projectConfig.getSshId();

        this.sshjConnection = project.getUserData(SSHJ_CONNECTION);
        this.javaProcesses = project.getUserData(JAVA_PROCESSES);

        initLabel();
        initInput();
        initComboBox();
        initContainer();

        root = FormBuilder.createFormBuilder()
                .addLabeledComponent("Remote Server", serverComboBox)
                .addLabeledComponent("Remote Process", processRefreshContainer)
                .addComponent(processChangedTip)
                .addLabeledComponent("Arthas Http Port", processBindContainer)
                .addLabeledComponent("Target Java File", fileLabel)
                .getPanel();

        setTitle("Class Reload");
        setOKButtonText("Retransform");
        setSize(600, 0);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return root;
    }

    @Override
    protected void doOKAction() {
        try {
            PsiFile data = event.getData(CommonDataKeys.PSI_FILE);
            if (data instanceof PsiJavaFile) {
                PsiJavaFile javaFile = (PsiJavaFile) data;
                hotReloadService.compileAndUploadClass(sshjConnection, javaFile, project, projectConfig.getHttpPort());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        super.doOKAction();
    }

    private void initLabel() {
        fileLabel = new JBLabel();
        PsiFile file = event.getData(CommonDataKeys.PSI_FILE);
        if (!(file instanceof PsiJavaFile)) {
            throw new RuntimeException("Class reload only supports Java files");
        }
        PsiJavaFile javaFile = (PsiJavaFile) file;
        String packageName = javaFile.getPackageName();
        String path = packageName.replaceAll("\\.", "/");
        fileLabel.setText(path + "/" + javaFile.getName());
    }

    private void initContainer() {
        // refresh remote java process
        DefaultActionGroup refreshGroup = new DefaultActionGroup();
        refreshGroup.add(new RefreshJavaProcess());
        ActionToolbar refreshToolbar = ActionManager.getInstance()
                .createActionToolbar("ClassHotReloadDialog@Refresh", refreshGroup, true);
        refreshToolbar.setTargetComponent(null);

        processRefreshContainer = new JPanel(new GridBagLayout());
        processRefreshContainer.add(javaProcessComboBox, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL,
                JBUI.emptyInsets(), 0, 0));
        processRefreshContainer.add(refreshToolbar.getComponent(), new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL,
                JBUI.emptyInsets(), 0, 0));

        // bind remote java process
        DefaultActionGroup bindGroup = new DefaultActionGroup();
        bindGroup.add(new AttachRemoteJavaProcessAction());
        ActionToolbar bindToolbar = ActionManager.getInstance()
                .createActionToolbar("ClassHotReloadDialog@Bind", bindGroup, true);
        bindToolbar.setTargetComponent(null);

        processBindContainer = new JPanel(new GridBagLayout());
        processBindContainer.add(arthasHttpPortInput, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL,
                JBUI.emptyInsets(), 0, 0));
        processBindContainer.add(bindToolbar.getComponent(), new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL,
                JBUI.emptyInsets(), 0, 0));

    }

    private void initInput() {
        Integer httpPort = projectConfig.getHttpPort();
        if (httpPort == null) {
            httpPort = RandomUtils.nextInt(35000, 40000);
            projectConfig.setHttpPort(httpPort);
        }
        SpinnerNumberModel model = new SpinnerNumberModel(httpPort.intValue(), 3500, 40000, 1);
        this.arthasHttpPortInput = new JSpinner(model);
        this.arthasHttpPortInput.addChangeListener(e -> {
            projectConfig.setHttpPort(getHttpPort());
        });
    }

    private void initComboBox() {
        serverComboBox = new ComboBox<>();
        javaProcessComboBox = new ComboBox<>();

        // java process comboBox
        javaProcessComboBox.setEnabled(false);
        javaProcessComboBox.addItemListener(e -> {
            JavaProcess selectedItem = (JavaProcess) javaProcessComboBox.getSelectedItem();
            if (selectedItem != null) {
                projectConfig.setPid(selectedItem.getPid());
                clearProcessChangedTip();
            }
        });

        // server comboBox
        List<SshServer> servers = new ArrayList<>();
        servers.add(SshServer.None);
        servers.addAll(ConfigHelper.sshServers());
        serverComboBox.setModel(new CollectionComboBoxModel<>(servers));
        serverComboBox.addActionListener(e -> {
            SshServer selectedItem = (SshServer) serverComboBox.getSelectedItem();
            if (selectedItem == SshServer.None) {
                return;
            }
            if (selectedItem != null) {
                sshId = selectedItem.getId();
                projectConfig.setSshId(sshId);

                javaProcessComboBox.setEnabled(true);
                refreshJavaProcessComboBox();
            }
        });
        // locate server
        if (sshId != null) {
            SshServer selected = ConfigHelper.getSshServerById(sshId);
            serverComboBox.setSelectedItem(selected);
        }
    }

    private void refreshJavaProcessComboBox() {
        try {
            javaProcessComboBox.setModel(new CollectionComboBoxModel<>(getJavaProcesses()));
            if (projectConfig.getPid() != null) {
                Optional<JavaProcess> first = javaProcesses.stream()
                        .filter(proc -> Objects.equals(proc.getPid(), projectConfig.getPid()))
                        .findFirst();
                if (first.isPresent()) {
                    first.ifPresent(javaProcess -> javaProcessComboBox.setSelectedItem(javaProcess));
                } else {
                    showProcessChangedTip();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves an existing SSH connection from the project or creates a new
     * instance if none exists.
     */
    private SshjConnection getSshjConnection() throws IOException {
        SshServer server = ConfigHelper.getSshServerById(sshId);
        if (this.sshjConnection == null || this.sshjConnection.isClosed()) {
            this.sshjConnection = SshConnectionManager.makeSshjConnection(server);
            this.project.putUserData(SSHJ_CONNECTION, sshjConnection);
        }
        return this.sshjConnection;
    }

    /**
     * Retrieve the cached remote Java process information from the project.
     * If the cached information is null, request it again.
     */
    private List<JavaProcess> getJavaProcesses() throws IOException {
        if (this.javaProcesses == null) {
            List<String> processes = hotReloadService.listJavaProcess(getSshjConnection());
            this.javaProcesses = processes.stream().map(JavaProcess::new).collect(Collectors.toList());
            this.project.putUserData(JAVA_PROCESSES, javaProcesses);
        }
        return this.javaProcesses;
    }

    private void setJavaProcessesNull() {
        this.javaProcesses = null;
    }

    private int getHttpPort() {
        return (int) arthasHttpPortInput.getValue();
    }

    private void showProcessChangedTip() {
        processChangedTip.setText("Process info has changed");
        processChangedTip.setForeground(JBColor.RED);

    }

    private void clearProcessChangedTip() {
        processChangedTip.setText("");
    }

    private class RefreshJavaProcess extends AnAction {
        public RefreshJavaProcess() {
            super("Refresh", "Refresh java process", AllIcons.Actions.Refresh);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
            setJavaProcessesNull();
            refreshJavaProcessComboBox();
        }
    }

    public class AttachRemoteJavaProcessAction extends AnAction {
        public AttachRemoteJavaProcessAction() {
            super("Attach", "Attach process", MyIcons.Actions.ATTACH);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            try {
                assert projectConfig != null;
                if (projectConfig.getSshId() == null || projectConfig.getPid() == null) {
                    return;
                }
                hotReloadService.attachRemoteJavaProcess(getSshjConnection(), projectConfig.getPid(), getHttpPort());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}