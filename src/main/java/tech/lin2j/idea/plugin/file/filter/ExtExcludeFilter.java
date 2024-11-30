package tech.lin2j.idea.plugin.file.filter;

import com.intellij.openapi.util.text.StringUtil;
import tech.lin2j.idea.plugin.ssh.CommandLog;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author linjinjia
 * @date 2022/12/9 23:54
 */
public class ExtExcludeFilter implements FileFilter {
    private final String extensions;
    private final Set<String> extensionSet;
    private final CommandLog commandLog;;

    public ExtExcludeFilter(String extensions, CommandLog commandLog) {
        this.commandLog = commandLog;
        if (StringUtil.isEmpty(extensions)) {
            this.extensions = "";
            this.extensionSet = new HashSet<>();
            return;
        }
        this.extensions = extensions;
        extensionSet = Arrays.stream(extensions.split(";"))
                .map(s -> s.replaceAll("\\*?\\.?", ""))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean accept(String f) {
        if (StringUtil.isEmpty(extensions)) {
            return true;
        }

        int dot = f.lastIndexOf('.');
        if (dot == -1) {
            return true;
        }
        String suffix = f.substring(dot + 1);
        boolean contains = extensionSet.contains(suffix);
        if (contains) {
            commandLog.info("[" + f + "] excluded by '" + extensions + "'");
        }
        return !contains;
    }

    public String getExtensions() {
        return extensions;
    }

    public Set<String> getExtensionSet() {
        return extensionSet;
    }
}