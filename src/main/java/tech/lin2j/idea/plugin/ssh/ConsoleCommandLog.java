package tech.lin2j.idea.plugin.ssh;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;

public class ConsoleCommandLog implements CommandLog {
    private ConsoleView console;

    public ConsoleCommandLog(ConsoleView console) {
        this.console = console;
    }

    @Override
    public void info(String msg) {
        console.print(msg, ConsoleViewContentType.NORMAL_OUTPUT);
    }

    @Override
    public void error(String msg) {
        console.print(msg, ConsoleViewContentType.ERROR_OUTPUT);
    }
}
