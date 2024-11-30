package tech.lin2j.idea.plugin.file.filter;

import org.jetbrains.annotations.NotNull;
import tech.lin2j.idea.plugin.ssh.CommandLog;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author linjinjia
 * @date 2024/11/27 22:23
 */
public class ConsoleFileFilter implements ListableFileFilter {
    private final CommandLog commandLog;
    private final LinkedList<FileFilter> filters = new LinkedList<>();

    public ConsoleFileFilter(CommandLog commandLog) {
        this.commandLog = commandLog;
    }

    public ConsoleFileFilter(@NotNull FileFilter fileFilter, @NotNull CommandLog commandLog) {
        this.commandLog = commandLog;
        filters.add(fileFilter);
    }

    @Override
    public boolean accept(String filename) {
        for (FileFilter filter : filters) {
            if (!filter.accept(filename)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void accept(String filename, FileAction<Boolean> action) throws Exception {
        boolean accept = accept(filename);
        action.execute(accept);
    }

    @Override
    public void addFilter(FileFilter filter) {
        filters.add(filter);
    }

    @Override
    public void removeLast() {
        filters.removeLast();
    }

    @Override
    public void remove(FileFilter filter) {
        filters.remove(filter);
    }

    @Override
    public List<FileFilter> getFilters() {
        return filters;
    }
}