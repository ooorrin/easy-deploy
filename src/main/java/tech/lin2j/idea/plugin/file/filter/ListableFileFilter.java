package tech.lin2j.idea.plugin.file.filter;

import java.util.List;

/**
 * @author linjinjia
 * @date 2024/11/30 13:40
 */
public interface ListableFileFilter extends FileFilter {

    void addFilter(FileFilter filter);

    void removeLast();

    void remove(FileFilter filter);

    List<FileFilter> getFilters();
}