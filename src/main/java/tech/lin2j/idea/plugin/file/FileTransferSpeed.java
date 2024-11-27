package tech.lin2j.idea.plugin.file;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author linjinjia
 * @date 2024/11/24 20:49
 */
public class FileTransferSpeed {
    private static final int UPDATE_INTERVAL = 1000;
    private long lastTransferred;
    private long lastUpdateTime;
    private String speed = "0 B/s";

    public FileTransferSpeed() {
        lastUpdateTime = System.currentTimeMillis();
        lastTransferred = 0;
    }

    public void start() {
        lastUpdateTime = System.currentTimeMillis();
        lastTransferred = 0;
    }

    public String accept(long transferred) {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastUpdateTime >= UPDATE_INTERVAL) {
            long deltaBytes = transferred - lastTransferred;
            long elapsedTime = currentTime - lastUpdateTime;

            double speed = deltaBytes / (elapsedTime / 1000.0); // B/s
            this.speed = formatSpeed(speed);

            lastUpdateTime = currentTime;
            lastTransferred = transferred;
        }

        return speed;
    }

    private String formatSpeed(double speed) {
        final String[] units = {"B/s", "KB/s", "MB/s", "GB/s", "TB/s"};
        int unitIndex = 0;

        while (speed >= 1024 && unitIndex < units.length - 1) {
            speed /= 1024;
            unitIndex++;
        }

        // 如果速度为 0 或非常小，直接返回 "0 B/s"
        if (speed < 1e-6) {
            return "0 B/s";
        }

        return String.format("%.2f %s", speed, units[unitIndex]);
    }
}