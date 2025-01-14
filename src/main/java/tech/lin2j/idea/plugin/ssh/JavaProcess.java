package tech.lin2j.idea.plugin.ssh;

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author linjinjia
 * @date 2024/11/7 21:48
 */
public class JavaProcess implements Comparable<JavaProcess> {

    private final int pid;
    private final String name;
    private final String process;

    public JavaProcess(String process) {
        Objects.requireNonNull(process);

        this.process = StringUtil.trim(process);

        String[] ss = process.split(" ");
        pid = Integer.parseInt(ss[0]);
        name = StringUtil.trim(ss[1]);
    }

    public int getPid() {
        return pid;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return process;
    }

    @Override
    public int compareTo(@NotNull JavaProcess o) {
        return this.name.compareTo(o.name);
    }
}