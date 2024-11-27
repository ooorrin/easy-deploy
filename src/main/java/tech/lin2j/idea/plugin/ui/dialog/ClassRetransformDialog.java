package tech.lin2j.idea.plugin.ui.dialog;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import icons.MyIcons;
import org.apache.commons.lang3.RandomUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.lin2j.idea.plugin.action.NewUpdateThreadAction;
import tech.lin2j.idea.plugin.exception.IllegalFileTypeException;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.model.HotReloadPersistence;
import tech.lin2j.idea.plugin.service.IHotReloadService;
import tech.lin2j.idea.plugin.service.impl.PluginNotificationService;
import tech.lin2j.idea.plugin.ssh.JavaProcess;
import tech.lin2j.idea.plugin.ssh.SshConnectionManager;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ssh.exception.RemoteSdkException;
import tech.lin2j.idea.plugin.ssh.sshj.SshjConnection;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author linjinjia
 * @date 2024/11/23 21:49
 */
public class ClassRetransformDialog extends DialogWrapper {
    public static final Key<SshjConnection> SSHJ_CONNECTION = Key.create("EDHotReload.SSHJ_CONNECTION");
    public static final Key<List<JavaProcess>> JAVA_PROCESSES = Key.create("EDHotReload.JAVA_PROCESSES");

    private final Project project;
    private final AnActionEvent event;

    private final HotReloadPersistence projectConfig;
    private final IHotReloadService hotReloadService;
    private final PluginNotificationService notificationService;

    private Integer sshId;
    private SshjConnection sshjConnection;
    private List<JavaProcess> javaProcesses;

    private final JPanel root;
    private final JBLabel attachedStatus = new JBLabel();
    private ComboBox<SshServer> serverComboBox;
    private ComboBox<JavaProcess> javaProcessComboBox;
    private JSpinner arthasHttpPortInput;
    private JBTextField targetClassFile;
    private JBTextField arthasPackLocation; // arthasPackLocation
    private JPanel processRefreshContainer;
    private JPanel processBindContainer;
    private JPanel arthasPackContainer;
    private boolean arthasPackExist;

    public ClassRetransformDialog(@NotNull Project project, AnActionEvent event) {
        super(project);

        this.project = project;
        this.event = event;

        this.hotReloadService = ApplicationManager.getApplication().getService(IHotReloadService.class);
        this.notificationService = ApplicationManager.getApplication().getService(PluginNotificationService.class);
        this.projectConfig = this.project.getService(HotReloadPersistence.class).getState();
        assert projectConfig != null;
        this.sshId = projectConfig.getSshId();

        initInput();
        initComboBox();
        initContainer();

        root = FormBuilder.createFormBuilder()
                .setVerticalGap(8)
                .addLabeledComponent("Remote server", serverComboBox)
                .addLabeledComponent("Remote process", processRefreshContainer)
                .addLabeledComponent("Remote arthas pack", arthasPackContainer)
                .addLabeledComponent("Arthas HTTP Port", processBindContainer)
                .addLabeledComponent("Target java class", targetClassFile)
                .getPanel();

        setTitle("Class Retransform");
        setOKButtonText("Retransform");
        setSize(600, 0);
        init();

        setData();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return root;
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{
                super.getHelpAction(),
                new AttachRemoteJavaProcessAction(),
                super.getOKAction()
        };
    }

    @Override
    protected @Nullable String getHelpId() {
        return "hotReloadWebHelper";
    }

    @Override
    protected void doOKAction() {
        if (isArthasPackNotExist()) {
            return;
        }

        try {
            PsiFile data = event.getData(CommonDataKeys.PSI_FILE);
            if (data instanceof PsiJavaFile javaFile) {
                hotReloadService.compileAndRetransformClass(project, getSshjConnection(), javaFile, projectConfig.getHttpPort());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        super.doOKAction();
    }

    private void initInput() {
        arthasPackLocation = new JBTextField();
        arthasPackLocation.setEditable(false);

        SpinnerNumberModel model = new SpinnerNumberModel(35678, 35000, 40000, 1);
        this.arthasHttpPortInput = new JSpinner(model);
        this.arthasHttpPortInput.addChangeListener(e -> {
            projectConfig.setHttpPort(getHttpPort());
        });
        this.arthasHttpPortInput.setEnabled(false);

        targetClassFile = new JBTextField();
        targetClassFile.setEditable(false);
        PsiFile file = event.getData(CommonDataKeys.PSI_FILE);
        if (!(file instanceof PsiJavaFile javaFile)) {
            throw new IllegalFileTypeException();
        }
        String className = javaFile.getName().replace(".java", ".class");
        targetClassFile.setText(javaFile.getPackageName() + "." + className);
    }

    private void initComboBox() {
        serverComboBox = new ComboBox<>();
        javaProcessComboBox = new ComboBox<>();

        serverComboBox.setModel(new CollectionComboBoxModel<>(ConfigHelper.sshServers()));
        // locate server
        if (sshId != null) {
            SshServer selected = ConfigHelper.getSshServerById(sshId);
            serverComboBox.setSelectedItem(selected);
        }
        serverComboBox.addItemListener(e -> {
            SshServer selectedItem = (SshServer) serverComboBox.getSelectedItem();
            if (selectedItem != null) {
                Integer newSshId = selectedItem.getId();
                if (!Objects.equals(newSshId, sshId)) { // server change
                    closePreSshjConnection();
                }
                sshId = newSshId;
                projectConfig.setSshId(sshId);
                checkArthasToolPack();
                refreshJavaProcessComboBox();
                getPortOrSetRandomPort();
                setAttachStatus(); // server ComboBox listener
            }
        });

        // java process comboBox
        javaProcessComboBox.addItemListener(e -> {
            JavaProcess selectedItem = (JavaProcess) javaProcessComboBox.getSelectedItem();
            if (selectedItem != null) {
                projectConfig.setPid(selectedItem.getPid());
                getPortOrSetRandomPort();
                setAttachStatus(); // java process ComboBox listener
            }
        });
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
        processBindContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        processBindContainer.add(arthasHttpPortInput);
        processBindContainer.add(attachedStatus);

        // upload arthas pack
        DefaultActionGroup uploadGroup = new DefaultActionGroup();
        uploadGroup.add(new UploadArthasPackAction(this));
        ActionToolbar uploadToolbar = ActionManager.getInstance()
                .createActionToolbar("ClassHotReloadDialog@Upload", uploadGroup, true);
        uploadToolbar.setTargetComponent(null);
        arthasPackContainer = new JPanel(new GridBagLayout());
        arthasPackContainer.add(arthasPackLocation, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL,
                JBUI.emptyInsets(), 0, 0));
        arthasPackContainer.add(uploadToolbar.getComponent(), new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL,
                JBUI.emptyInsets(), 0, 0));
    }

    public void setData() {
        if (sshId == null) {
            new SelectServerDialog(project).show();
            sshId = projectConfig.getSshId();
            if (sshId == null) {
                return;
            }
        }
        try {
            getSshjConnection();
        } catch (RemoteSdkException | IOException e) {
            notificationService.showHotReloadNotification(project, "Connection Error", e.getMessage());
            return;
        }
        refreshJavaProcessComboBox();
        checkArthasToolPack();
        getPortOrSetRandomPort();
        setAttachStatus(); // setData
    }

    private int getHttpPort() {
        return (int) arthasHttpPortInput.getValue();
    }

    private void setJavaProcessesNull() {
        this.javaProcesses = null;
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

    private void closePreSshjConnection() {
        if (sshjConnection != null) {
            sshjConnection.close();
            sshjConnection = null;
            project.putUserData(SSHJ_CONNECTION, null);
            project.putUserData(JAVA_PROCESSES, null);
        }
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

    private void checkArthasToolPack() {
        try {
            arthasPackExist = hotReloadService.isArthasPackExist(getSshjConnection());
            if (!arthasPackExist) {
                arthasPackLocation.setText("Not Found");
            } else {
                arthasPackLocation.setText(hotReloadService.getArthasBootJar());
            }
        } catch (IOException e) {
            arthasPackLocation.setText("IO Error, try again");
        }
    }

    private boolean isArthasPackNotExist() {
        if (!arthasPackExist) {
            Messages.showInfoMessage(
                    "The server does not have the Arthas toolkit yet. Please upload it first",
                    "Arthas Tool"
            );
            return true;
        }
        return false;
    }

    private void getPortOrSetRandomPort() {
        try {
            if (projectConfig.getPid() != null) {
                Integer port = hotReloadService.getAttachedHttpPort(getSshjConnection(), projectConfig.getPid());
                if (port != null) {
                    projectConfig.setHttpPort(port);
                }
            }
        } catch (Exception ignored) {
        }
        Integer httpPort = projectConfig.getHttpPort();
        if (httpPort == null) {
            httpPort = RandomUtils.nextInt(35000, 40000);
            projectConfig.setHttpPort(httpPort);
        }
        if (arthasHttpPortInput != null) {
            arthasHttpPortInput.setValue(httpPort);
            arthasHttpPortInput.setEnabled(true);
        }
    }

    private void setAttachStatus() {
        try {
            if (projectConfig.getPid() != null) {
                int pid = projectConfig.getPid();
                int httpPort = getHttpPort();
                boolean attached = hotReloadService.isAttached(getSshjConnection(), pid, httpPort);
                attachedStatus.setText("");
                if (attached) {
                    attachedStatus.setIcon(MyIcons.Actions.Connect);
                } else {
                    attachedStatus.setIcon(MyIcons.Actions.LostConnect);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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
                    attachedStatus.setText("Process not found");
                    attachedStatus.setForeground(JBColor.RED);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // inner class
    private class RefreshJavaProcess extends NewUpdateThreadAction {
        public RefreshJavaProcess() {
            super("Refresh", "Refresh java process", MyIcons.Actions.Refresh);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
            setJavaProcessesNull();
            refreshJavaProcessComboBox();
        }
    }

    public class AttachRemoteJavaProcessAction extends AbstractAction {
        public AttachRemoteJavaProcessAction() {
            super("Attach Process");
        }

        @Override
        public void actionPerformed(@NotNull ActionEvent e) {
            if (isArthasPackNotExist()) {
                return;
            }

            try {
                assert projectConfig != null;
                if (projectConfig.getSshId() == null || projectConfig.getPid() == null) {
                    return;
                }
                hotReloadService.attachRemoteJavaProcess(project, getSshjConnection(), projectConfig.getPid(), getHttpPort());
                setAttachStatus(); // AttachRemoteJavaProcessAction
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public class UploadArthasPackAction extends NewUpdateThreadAction {
        private final DialogWrapper dialogWrapper;

        public UploadArthasPackAction(DialogWrapper dialogWrapper) {
            super("Upload", "Upload arthas pack", MyIcons.Actions.UPLOAD);
            this.dialogWrapper = dialogWrapper;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            ProgressManager.getInstance().run(new Task.Backgroundable(project, "Upload arthas pack") {
                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    progressIndicator.setIndeterminate(false);
                    try {
                        hotReloadService.uploadArthasPack(project, getSshjConnection(), progressIndicator);
                        checkArthasToolPack();
                    } catch (IOException ex) {
                        progressIndicator.setFraction(1f);
                    }
                }
            });
            dialogWrapper.close(0);
        }
    }

}