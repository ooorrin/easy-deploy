package tech.lin2j.idea.plugin.file;

import com.intellij.openapi.util.text.StringUtil;
import net.schmizz.sshj.common.StreamCopier;
import net.schmizz.sshj.xfer.TransferListener;
import tech.lin2j.idea.plugin.ssh.CommandLog;
import tech.lin2j.idea.plugin.uitl.FileTransferSpeed;

import java.util.stream.Stream;

/**
 * @author linjinjia
 * @date 2024/4/13 16:09
 */
public class ConsoleTransferListener implements TransferListener {
    private final String relPath;
    private final CommandLog console;
    private final FileTransferSpeed fileTransferSpeed = new FileTransferSpeed();

    public ConsoleTransferListener(String relPath, CommandLog console) {
        if (!relPath.endsWith("/")) {
            relPath += "/";
        }
        this.relPath = relPath;
        this.console = console;
    }

    @Override
    public TransferListener directory(String name) {
        return new ConsoleTransferListener(relPath + name + "/", console);
    }

    @Override
    public StreamCopier.Listener file(final String name, final long size) {
        String path = relPath + name;
        console.info("Transfer file: " + path + ", Size: " + StringUtil.formatFileSize(size) + "\n");
        return transferred -> {
            String speed = fileTransferSpeed.accept(transferred);

            double fileProgress = 0;
            if (size > 0) {
                fileProgress = transferred / (double) size;
            }
            boolean fileCompleted = Math.abs(1 - fileProgress) < 1e-6;
            printProgress((int) (fileProgress * 100), fileCompleted, speed);

        };
    }

    private void printProgress(int complete, boolean completed, String speed) {
        StringBuilder sb = new StringBuilder("[");
        Stream.generate(() -> '#').limit(complete).forEach(sb::append);
        Stream.generate(() -> '_').limit(100 - complete).forEach(sb::append);
        sb.append("] ");
        if (completed) {
            sb.append("complete, speed: ").append(speed).append("\n");
        } else {
            sb.append(complete).append("% , speed: ").append(speed);
        }
        console.print("\r");
        console.print(sb.toString());
    }
}