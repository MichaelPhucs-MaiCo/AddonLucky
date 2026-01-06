package maico.addonbuu.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Formatting;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileLogger {
    private static File logFile;
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Khởi tạo hệ thống log, tìm file addonbuu-x.log phù hợp.
     */
    public static void init() {
        MinecraftClient mc = MinecraftClient.getInstance();
        // 1. Tìm thư mục .minecraft/addonbuu/log
        File logFolder = new File(mc.runDirectory, "addonbuu/log");

        if (!logFolder.exists()) {
            logFolder.mkdirs();
        }

        // 2. Logic đánh số thông minh: addonbuu-1.log, addonbuu-2.log...
        int index = 1;
        while (true) {
            File potentialFile = new File(logFolder, "addonbuu-" + index + ".log");
            if (!potentialFile.exists()) {
                logFile = potentialFile;
                break;
            }
            index++;
        }

        log("--- KHỞI TẠO SESSION MỚI [" + index + "] ---");
    }

    /**
     * Ghi một dòng log vào file.
     */
    public static void log(String message) {
        if (logFile == null) init();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            // Xóa mã màu Minecraft (§) trước khi ghi vào file cho đỡ rác
            String cleanMsg = Formatting.strip(message);
            String timestamp = LocalDateTime.now().format(DATE_TIME_FORMAT);

            writer.write("[" + timestamp + "] " + cleanMsg);
            writer.newLine();
            writer.flush(); // Đảm bảo ghi ngay lập tức
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
