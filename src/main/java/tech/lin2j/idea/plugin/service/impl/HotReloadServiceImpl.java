package tech.lin2j.idea.plugin.service.impl;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompilerPaths;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiJavaFile;
import com.intellij.task.ProjectTaskManager;
import org.apache.commons.compress.utils.IOUtils;
import org.codehaus.plexus.util.FileUtils;
import org.jetbrains.concurrency.Promise;
import tech.lin2j.idea.plugin.service.IHotReloadService;
import tech.lin2j.idea.plugin.ssh.SshStatus;
import tech.lin2j.idea.plugin.ssh.sshj.SshjConnection;
import tech.lin2j.idea.plugin.uitl.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author linjinjia
 * @date 2024/10/20 15:28
 */
public class HotReloadServiceImpl implements IHotReloadService {
    static final String ARTHAS_PACK = "arthas.tar.gz";
    static final String ATTACH_PATTERN = "bash -lc 'java -jar %s -attach-only %s --telnet-port %s --http-port %s'";
    static final String ARTHAS_SERVICE_PATTERN = "http://127.0.0.1:%s/api";
    static final String RETRANSFORM_DATA_PATTERN = "{\"action\": \"exec\",\"command\": \"retransform %s\"}";
    static final String CURL_PATTERN = "curl -Ss -XPOST %s -d '%s'";

    @Override
    public void uploadArthasPack(SshjConnection conn) throws IOException {
        conn.mkdirs(getToolDirectory());
        if (isArthasPackExist(conn)) {
            return;
        }
        String localPackPath = extractArthasPack();
        if (localPackPath.isEmpty()) {
            return;
        }
        conn.upload(localPackPath, getToolDirectory());
        SshStatus unpackResult = conn.execute("cd " + getToolDirectory() + "&& tar -xzvf " + getRemotePackPath());
        if (!unpackResult.isSuccess()) {
            Messages.showErrorDialog(unpackResult.getMessage(), "Extract Pack");
        }
    }

    @Override
    public List<String> listJavaProcess(SshjConnection conn) throws IOException {
        SshStatus result = conn.execute("bash -lc 'jps'");
        if (result.isSuccess()) {
            return List.of(result.getMessage().split("\n"));
        }
        Messages.showErrorDialog(result.getMessage(), "Fetch Java Process");
        return List.of();
    }

    @Override
    public void attachRemoteJavaProcess(SshjConnection conn, int processId, String processName) throws IOException {
        String arthasCmd = String.format(ATTACH_PATTERN, getArthasBootJar(), processId, 3658, 8563);
        System.out.println(arthasCmd);
        SshStatus result = conn.execute(arthasCmd);
        if (result.isSuccess()) {
            System.out.println("attach success " + processName);
            return;
        }
        Messages.showErrorDialog(result.getMessage(), "Attach Remote Java Process");
    }

    @Override
    public String compileAndUploadClass(SshjConnection conn, PsiJavaFile psiFile, Project project) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        StringBuilder buffer = new StringBuilder();
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                doCompileAndSend(conn, psiFile, project, buffer);
            } finally {
                latch.countDown();
            }
        });
        latch.await();
        return buffer.toString();
    }

    /**
     * Compiles the specified Java source file and uploads the compiled class file to a remote server.
     * Stores the remote location of the uploaded class file in the provided buffer.
     *
     * <p>This method compiles the Java source code represented by the PsiJavaFile object within the context
     * of the specified project. After successful compilation, the compiled class file is uploaded to the
     * remote server via the SSH connection. The location of the uploaded class file on the remote server
     * is then appended to the provided StringBuilder buffer.</p>
     *
     * @param conn    the SSH connection to the remote server, used for uploading the compiled class file
     * @param psiFile the PsiJavaFile object representing the Java source code to be compiled
     * @param project the Project context in which the compilation occurs
     * @param buffer  the StringBuilder that will contain the remote location of the uploaded class file
     */
    private void doCompileAndSend(SshjConnection conn, PsiJavaFile psiFile, Project project, StringBuilder buffer) {
        ProjectTaskManager instance = ProjectTaskManager.getInstance(project);
        Promise<ProjectTaskManager.Result> promise = instance.compile(psiFile.getVirtualFile());
        promise.then(result -> {
            if (result.hasErrors()) {
                // 编译报错
                return result.hasErrors();
            }
            // class 文件查找
            String classFilename = psiFile.getName().replace(".java", ".class");
            String classPackage = psiFile.getPackageName().replaceAll("\\.", "/");
            String classFilePath = classPackage + "/" + classFilename;
            Module[] modules = ModuleManager.getInstance(project).getModules();
            for (Module module : modules) {
                String outputPath = CompilerPaths.getModuleOutputPath(module, false);
                String classFile = outputPath + "/" + classFilePath;
                File cf = new File(classFile);
                if (!cf.exists()) {
                    continue;
                }
                // 发送文件到服务器
                buffer.append(sendClassFile(conn, module, cf.getAbsolutePath(), classPackage, classFilename));
                break;
            }
            return result.hasErrors();
        });
    }

    @Override
    public void requestArthasRetransform(SshjConnection conn, String targetClass) throws IOException {

        String url = String.format(ARTHAS_SERVICE_PATTERN, 8563);
        String arthasCmd = String.format(RETRANSFORM_DATA_PATTERN, getHotClassDirectory() + targetClass);
        String curlCmd = String.format(CURL_PATTERN, url, arthasCmd);

        System.out.println(curlCmd);
        SshStatus result = conn.execute(curlCmd);
        if (!result.isSuccess()) {
            Messages.showErrorDialog(result.getMessage(), "Arthas Retransform");
        } else {
            System.out.println("curl completed: " + result.getMessage());
        }
    }

    private String getToolDirectory() {
        return "/tmp/EasyDeploy/tool/";
    }

    private String getArthasBootJar() {
        return getToolDirectory() + "arthas-bin/arthas-boot.jar";
    }

    private String getHotClassDirectory() {
        return "/tmp/EasyDeploy/classes/";
    }

    private String getRemotePackPath() {
        return getToolDirectory() + ARTHAS_PACK;
    }

    private String sendClassFile(SshjConnection conn, Module module,
                                 String localClassFilePath, String classPackage, String classFilename) {
        try {
            String remoteDir = getHotClassDirectory() + module.getName() + "/" + classPackage;
            conn.mkdirs(remoteDir);
            conn.upload(localClassFilePath, remoteDir);
            return module.getName() + "/" + classPackage + "/" + classFilename;
        } catch (IOException e) {
            return "";
        }
    }

    private boolean isArthasPackExist(SshjConnection conn) throws IOException {
        SshStatus result = conn.execute("ls " + getRemotePackPath());
        if (!result.isSuccess()) {
            if (result.getMessage().contains("No such file")) {
                return false;
            }
            Messages.showErrorDialog(result.getMessage(), "Check Arthas Pack");
        }
        return true;
    }

    private String extractArthasPack() throws IOException {
        // create directory
        String edDir = FileUtil.getHomeDir() + "/.easy-deploy/";
        File edDirFile = new File(edDir);
        if (!edDirFile.exists()) {
            FileUtils.mkdir(edDir);
        }
        // create zip file
        String localPackPath = edDir + ARTHAS_PACK;
        File localPackFile = new File(localPackPath);
        if (localPackFile.exists()) {
            return localPackPath;
        }
        localPackFile.createNewFile();
        // copy file
        try (InputStream packStream = getClass().getClassLoader().getResourceAsStream(ARTHAS_PACK);
             OutputStream localPackStream = new FileOutputStream(localPackFile)) {
            if (packStream == null) {
                return "";
            }
            IOUtils.copy(packStream, localPackStream, 2048);
            localPackStream.flush();
        }
        return localPackPath;
    }

}
