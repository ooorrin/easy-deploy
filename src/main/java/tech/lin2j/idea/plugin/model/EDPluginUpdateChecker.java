package tech.lin2j.idea.plugin.model;

import com.intellij.ide.plugins.StandalonePluginUpdateChecker;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.components.Service;
import icons.MyIcons;
import tech.lin2j.idea.plugin.enums.Constant;
import tech.lin2j.idea.plugin.uitl.EasyDeployPluginUtil;

/**
 * @author linjinjia
 * @date 2024/12/1 11:17
 */
@Service(Service.Level.APP)
public final class EDPluginUpdateChecker extends StandalonePluginUpdateChecker {

    public EDPluginUpdateChecker() {
        super(EasyDeployPluginUtil.PLUGIN_ID,
                Constant.UPDATE_CHECKER_PROPERTY,
                NotificationGroup.findRegisteredGroup(Constant.EASY_DEPLOY),
                MyIcons.EasyDeploy);
    }

    @Override
    public boolean skipUpdateCheck() {
        return !EasyDeployPluginUtil.isEnabled();
    }
}