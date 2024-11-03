package tech.lin2j.idea.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.model.ConfigHelper;
import tech.lin2j.idea.plugin.service.IHotReloadService;
import tech.lin2j.idea.plugin.ssh.SshConnectionManager;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ssh.sshj.SshjConnection;

import java.io.IOException;

/**
 *
 * @author linjinjia
 * @date 2024/10/20 15:31
 */
public class ClassHotReloadAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        IHotReloadService hotReloadService = ServiceManager.getService(IHotReloadService.class);
        SshServer server = ConfigHelper.getSshServerById(1);
        try {
            SshjConnection sshjConnection = SshConnectionManager.makeSshjConnection(server);
//            hotReloadService.uploadArthasPack(sshjConnection);
//            System.out.println(hotReloadService.listJavaProcess(sshjConnection));
            PsiFile data = e.getData(CommonDataKeys.PSI_FILE);
            if (data instanceof PsiJavaFile) {
                System.out.println("== " + hotReloadService.compileAndUploadClass(sshjConnection, (PsiJavaFile) data, e.getProject()));
            }
//            hotReloadService.attachRemoteJavaProcess(sshjConnection, 4702, "arthas-test-1.0-SNAPSHOT.jar");
//
            String targetClass = "arthas-test/org/example/PonyUser.class";
            hotReloadService.requestArthasRetransform(sshjConnection, targetClass);

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (InterruptedException ignored) {

        }
    }
}
