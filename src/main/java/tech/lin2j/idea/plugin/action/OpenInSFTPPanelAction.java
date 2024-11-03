package tech.lin2j.idea.plugin.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.util.ReflectionUtil;
import com.jediterm.terminal.ProcessTtyConnector;
import com.jediterm.terminal.model.LinesBuffer;
import com.jediterm.terminal.model.TerminalLine;
import com.jediterm.terminal.model.TerminalTextBuffer;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.terminal.ShellTerminalWidget;
import org.jetbrains.plugins.terminal.TerminalTabState;
import org.jetbrains.plugins.terminal.action.TerminalSessionContextMenuActionBase;
import tech.lin2j.idea.plugin.ssh.sshj.SshjTtyConnector;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author linjinjia
 * @date 2024/8/25 17:54
 */
public class OpenInSFTPPanelAction extends TerminalSessionContextMenuActionBase {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent,
                                @NotNull ToolWindow toolWindow,
                                @Nullable Content content) {
    }

    private LinesBuffer getScreenBuffer(ShellTerminalWidget shellTerminalWidget) throws IllegalAccessException {
        TerminalTextBuffer terminalTextBuffer = shellTerminalWidget.getTerminalTextBuffer();
        Field myScreenBuffer = ReflectionUtil.getDeclaredField(TerminalTextBuffer.class, "myScreenBuffer");
        myScreenBuffer.setAccessible(true);
        return (LinesBuffer) myScreenBuffer.get(terminalTextBuffer);
    }

    private List<TerminalLine> getTerminalLines(LinesBuffer linesBuffer) throws IllegalAccessException {
        Field myLines = ReflectionUtil.getDeclaredField(linesBuffer.getClass(), "myLines");
        myLines.setAccessible(true);
        return (List<TerminalLine>) myLines.get(linesBuffer);
    }
}