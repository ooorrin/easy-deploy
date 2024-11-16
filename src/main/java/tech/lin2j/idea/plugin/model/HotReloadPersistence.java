package tech.lin2j.idea.plugin.model;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.apache.commons.lang3.RandomUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * @author linjinjia
 * @date 2024/11/7 22:28
 */
@State(
        name = "EasyDeployHotReload",
        storages = {@Storage("workspace.xml")}
)
@Service
public final class HotReloadPersistence implements PersistentStateComponent<HotReloadPersistence> {

    private Integer sshId;

    private Integer pid;

    private Integer httpPort;

    public Integer getSshId() {
        return sshId;
    }

    public void setSshId(Integer sshId) {
        this.sshId = sshId;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
    }

    @Override
    public @Nullable HotReloadPersistence getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull HotReloadPersistence hotReloadPersistence) {
        XmlSerializerUtil.copyBean(hotReloadPersistence, this);
    }
}