package tech.lin2j.idea.plugin.file.filter;

import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.ssh.CommandLog;

/**
 *
 * @author linjinjia
 * @date 2024/11/27 22:23
 */
public class ConsoleFileFilter implements FileFilter {
    private final CommandLog commandLog;
    private final FileFilter filter;

    public ConsoleFileFilter(@NotNull FileFilter fileFilter, @NotNull CommandLog commandLog) {
        this.filter = fileFilter;
        this.commandLog = commandLog;
    }

    @Override
    public boolean accept(String filename) {
        return filter.accept(filename);
    }

    @Override
    public void accept(String filename, FileAction<Boolean> action) throws Exception {
        boolean accept = filter.accept(filename);
        if (!accept) {
            commandLog.info(filename + " exclude\n");
        }
        action.execute(accept);
    }
}