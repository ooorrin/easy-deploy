package tech.lin2j.idea.plugin.file.filter;

import com.intellij.util.PathUtil;
import tech.lin2j.idea.plugin.ssh.CommandLog;

import java.util.regex.Pattern;

/**
 * @author linjinjia
 * @date 2024/11/30 13:21
 */
public class RegexFileFilter implements FileFilter {
    private final Pattern pattern;
    private final String regex;
    private final CommandLog commandLog;

    public RegexFileFilter(String regex, CommandLog commandLog) {
        this.regex = regex;
        this.pattern = Pattern.compile(regex);
        this.commandLog = commandLog;
    }

    @Override
    public boolean accept(String filename) {
        String name = PathUtil.getFileName(filename);
        boolean matches = pattern.matcher(name).matches();
        if (matches) {
            commandLog.info("[" + filename + "] matches pattern '" + regex + "'");
        }
        return matches;
    }
}