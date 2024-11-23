package tech.lin2j.idea.plugin.service;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiJavaFile;
import tech.lin2j.idea.plugin.ssh.sshj.SshjConnection;

import java.io.IOException;
import java.util.List;

public interface IHotReloadService {

    /**
     * Checks whether the Arthas toolkit exists at the specified location on the server.
     *
     * @param conn the SSH connection to the remote server.
     * @return {@code true} if the Arthas toolkit exists, {@code false} otherwise.
     * @throws IOException if an I/O error occurs during the operation.
     */
    boolean isArthasPackExist(SshjConnection conn) throws IOException;

    /**
     * Uploads the Arthas package to a remote server via SSH connection.
     *
     * @param conn the SSH connection to the remote server
     * @throws IOException if an I/O error occurs during the file upload process
     */
    void uploadArthasPack(Project project, SshjConnection conn, ProgressIndicator indicator) throws IOException;

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
     * Checks whether the given process ID (PID) is bound to the specified HTTP port.
     *
     * @param conn     the SSH connection to the remote server.
     * @param pid      the process ID to check.
     * @param httpPort the HTTP port to verify binding with the process.
     * @return {@code true} if the specified PID is bound to the given HTTP port, {@code false} otherwise.
     * @throws IOException if an I/O error occurs during the operation.
     */
    boolean isAttached(SshjConnection conn, int pid, int httpPort) throws IOException;

    /**
     * Attaches the specified Java process to Arthas and binds it to the given HTTP port.
     * Upon successful binding, a file is created in the plugin directory on the server.
     * The file name is the process ID, and its content is the bound port number,
     * facilitating the functionality of the {@code getHttpPort} method.
     *
     * @param project   the current IDEA project context.
     * @param conn      the SSH connection to the remote server.
     * @param processId the process ID of the Java application to attach.
     * @param httpPort  the HTTP port to bind the Arthas instance to.
     * @throws IOException if an I/O error occurs during the operation.
     */
    void attachRemoteJavaProcess(Project project, SshjConnection conn, int processId, int httpPort) throws IOException;

    /**
     * Retrieves the HTTP port bound to the Arthas instance for the specified process ID (PID).
     * If the process is not bound to any port, {@code null} is returned.
     *
     * @param connection the SSH connection to the remote server.
     * @param pid        the process ID for which to retrieve the Arthas HTTP port.
     * @return the HTTP port number bound to the Arthas instance, or {@code null} if not bound.
     * @throws IOException if an I/O error occurs during the operation.
     */
    Integer getAttachedHttpPort(SshjConnection connection, int pid) throws IOException;

    /**
     * Compiles the specified Java file and, upon successful compilation, uploads the compiled class
     * to a designated directory on the remote server. The uploaded class is then used for hot code
     * replacement via the Arthas toolkit.
     *
     * @param conn     the SSH connection to the remote server.
     * @param psiFile  the Java file to be compiled.
     * @param project  the current IDEA project context.
     * @param httpPort the HTTP port of the Arthas instance for performing the hot code update.
     */
    void compileAndRetransformClass(SshjConnection conn, PsiJavaFile psiFile, Project project, int httpPort);

    /**
     * Requests a hot-retransform (hot update) of the specified Java class on a remote server using
     * the Arthas diagnostic tool via an SSH connection.
     *
     * <p>This method utilizes the Arthas tool to retransform (redefine) the specified class on the
     * remote server, enabling hot code updates without requiring a full application restart. This is
     * useful for applying changes to the Java class dynamically in a production or testing environment.</p>
     *
     * @param project     project
     * @param conn        the SSH connection to the remote server
     * @param targetClass the fully qualified name of the target class to retransform
     * @throws IOException if an I/O error occurs during the retransform request
     */
    void retransform(Project project, SshjConnection conn, String targetClass, int httpPort) throws IOException;

    /**
     * Retrieves the location of the Arthas toolkit JAR file on the server.
     *
     * @return the file path to the Arthas boot JAR on the server.
     */
    String getArthasBootJar();

}
