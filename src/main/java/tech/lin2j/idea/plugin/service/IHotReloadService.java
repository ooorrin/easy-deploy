package tech.lin2j.idea.plugin.service;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiJavaFile;
import tech.lin2j.idea.plugin.ssh.sshj.SshjConnection;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface IHotReloadService {

    /**
     * Uploads the Arthas package to a remote server via SSH connection.
     *
     * @param conn the SSH connection to the remote server
     * @throws IOException if an I/O error occurs during the file upload process
     */
    void uploadArthasPack(SshjConnection conn) throws IOException;

    /**
     * Retrieves a list of Java processes running on a remote server via an SSH connection
     * by executing the `jps` command.
     *
     * @param conn the SSH connection to the remote server
     * @return a list of strings, each representing a Java process on the remote server
     * @throws IOException if an I/O error occurs during the execution of the `jps` command
     */
    List<String> listJavaProcess(SshjConnection conn) throws IOException;

    /**
     * Attaches to a remote Java process using the Arthas diagnostic tool via an SSH connection.
     *
     * <p>This method utilizes the Arthas tool package to connect to the specified Java process on a
     * remote server by using its process ID and optionally its process name. This can be used for
     * remote debugging, monitoring, or diagnostics.</p>
     *
     * @param conn        the SSH connection to the remote server
     * @param processId   the ID of the Java process to attach to
     * @throws IOException if an I/O error occurs during the attachment process
     */
    void attachRemoteJavaProcess(SshjConnection conn, int processId, int httpPort) throws IOException;

    /**
     * Compiles a Java class from the given source file and uploads the compiled class to a remote server
     * via an SSH connection.
     *
     * <p>This method takes a PsiJavaFile object representing the Java source code and compiles it. After
     * successful compilation, the resulting class file is uploaded to the specified dir of remote server,
     * allowing the code to be executed or used remotely.</p>
     *
     * @param conn    the SSH connection to the remote server
     * @param psiFile the PsiJavaFile object representing the Java source code to compile
     * @param project the Project context in which the compilation occurs
     * @throws InterruptedException if the compilation or upload process is interrupted
     */
    void compileAndUploadClass(SshjConnection conn, PsiJavaFile psiFile, Project project, int httpPort)
            throws InterruptedException;

    /**
     * Requests a hot-retransform (hot update) of the specified Java class on a remote server using
     * the Arthas diagnostic tool via an SSH connection.
     *
     * <p>This method utilizes the Arthas tool to retransform (redefine) the specified class on the
     * remote server, enabling hot code updates without requiring a full application restart. This is
     * useful for applying changes to the Java class dynamically in a production or testing environment.</p>
     *
     * @param conn        the SSH connection to the remote server
     * @param targetClass the fully qualified name of the target class to retransform
     * @throws IOException if an I/O error occurs during the retransform request
     */
    void requestArthasRetransform(SshjConnection conn, String targetClass, int httpPort) throws IOException;
}
