package tech.lin2j.idea.plugin.ui;

import com.intellij.openapi.help.WebHelpProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author linjinjia
 * @date 2024/11/23 18:15
 */
public class HotReloadWebHelper extends WebHelpProvider {

    @Override
    public @Nullable String getHelpPageUrl(@NotNull String s) {
        return "https://www.lin2j.tech/md/easy-deploy/brief.html";
    }
}