package tech.lin2j.idea.plugin.ssh;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;

public class ConsoleCommandLog implements CommandLog {
    private final ConsoleView console;

    public ConsoleCommandLog(ConsoleView console) {
        this.console = console;
    }

    @Override
    public ConsoleView getConsole() {
        return console;
    }

    @Override
    public void print(String msg, ConsoleViewContentType contentType) {
        console.print(msg, contentType);
    }
}
