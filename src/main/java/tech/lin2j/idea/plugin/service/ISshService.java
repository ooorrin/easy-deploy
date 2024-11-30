package tech.lin2j.idea.plugin.service;

import net.schmizz.sshj.xfer.TransferListener;
import tech.lin2j.idea.plugin.file.filter.FileFilter;
import tech.lin2j.idea.plugin.ssh.CommandLog;
import tech.lin2j.idea.plugin.ssh.SshServer;
import tech.lin2j.idea.plugin.ssh.SshStatus;
import tech.lin2j.idea.plugin.ssh.sshj.SshjConnection;

/**
 * @author linjinjia
 * @date 2023/12/21 22:20
 */
public interface ISshService {

    /**
     * test whether the server information is available
     *
     * @param sshServer server information
     * @return execution result
     */
    SshStatus isValid(SshServer sshServer);

    /**
     * block if the command is not finished,
     * so do not execute command like "tail -f"
     * because it will block the thread
     *
     * @param sshServer server information
     * @param command   command
     * @return execution result
     */
    SshStatus execute(SshServer sshServer, String command);

    void executeAsync(CommandLog commandLog, SshServer sshServer, String command);

    void executeAsync(CommandLog commandLog, SshjConnection connection, String command);

    SshStatus upload(FileFilter filter, SshServer server, String localFile,
                     String remoteDir, TransferListener listener);

    boolean upload(FileFilter filter, SshjConnection connection, String localFile, String remoteDir, CommandLog commandLog);

    /**
     * get file from remote server
     *
     * @param sshServer  server information
     * @param remoteFile remote file absolute path
     * @param localFile  local file absolute path
     * @return download result
     */
    SshStatus download(SshServer sshServer, String remoteFile, String localFile);

    /**
     * test whether the remote target directory is existed.
     *
     * @param server          ssh server information
     * @param remoteTargetDir remote target directory
     * @return return true if the remote target directory is existed, or return false
     */
    SshStatus isDirExist(SshServer server, String remoteTargetDir);

}