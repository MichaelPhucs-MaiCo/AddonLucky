package maico.addonbuu.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Formatting;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileLogger {
    private static File logFile;
    private static LocalDate lastDateChecked; // Biến canh gác thời gian 🕒
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FILE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    /**
     * Khởi tạo hệ thống log theo ngày dd-MM-yyyy.
     */
    public static void init() {
        MinecraftClient mc = MinecraftClient.getInstance();
        File logFolder = new File(mc.runDirectory, "addonbuu/log");

        if (!logFolder.exists()) {
            logFolder.mkdirs();
        }

        // 1. Xác định ngày lúc bắt đầu bật game
        lastDateChecked = LocalDate.now();
        String fileName = "addonbuu_" + lastDateChecked.format(DATE_FILE_FORMAT) + ".log";
        logFile = new File(logFolder, fileName);

        // 2. Ghi một dòng mở đầu phiên cho nó chuyên nghiệp
        logRaw("--- PHIÊN LOG MỚI BẮT ĐẦU [" + LocalDateTime.now().format(DATE_TIME_FORMAT) + "] ---");
    }

    /**
     * Kiểm tra xem có phải đã sang ngày mới (00:00) chưa để kẻ vạch phân cách.
     */
    private static void checkDayTransition() {
        LocalDate today = LocalDate.now();
        if (lastDateChecked == null) lastDateChecked = today;

        if (!today.equals(lastDateChecked)) {
            logRaw("\n==================================================");
            logRaw(">> HỆ THỐNG: BƯỚC SANG NGÀY MỚI: " + today.format(DATE_FILE_FORMAT) + " <<");
            logRaw("==================================================\n");

            lastDateChecked = today;
            // Lưu ý: Nếu treo máy qua đêm, tớ vẫn ghi tiếp vào file cũ nhưng có vạch ngăn nhé!
        }
    }

    /**
     * Ghi một dòng log có timestamp và dọn dẹp mã màu Minecraft.
     */
    public static void log(String message) {
        if (logFile == null) init();

        // Luôn kiểm tra xem đã qua nửa đêm chưa trước khi ghi
        checkDayTransition();

        String timestamp = LocalDateTime.now().format(DATE_TIME_FORMAT);
        String cleanMsg = Formatting.strip(message); // Xóa mấy cái ký tự § cho file log nó "sạch"

        logRaw("[" + timestamp + "] " + cleanMsg);
    }

    /**
     * Hàm ghi thô vào file (dùng nội bộ để tránh lặp timestamp khi kẻ vạch).
     */
    private static void logRaw(String line) {
        if (logFile == null) return;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write(line);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
