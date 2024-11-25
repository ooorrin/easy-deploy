package tech.lin2j.idea.plugin.ssh;

public interface CommandLog {

    void info(String msg);

    void error(String msg);
}
