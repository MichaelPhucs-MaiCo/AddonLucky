package maico.addonbuu.utils;

import maico.addonbuu.hud.ModHudRenderer;
import net.minecraft.client.MinecraftClient;
import maico.addonbuu.*;
import meteordevelopment.meteorclient.systems.modules.Module; // <--- NHỚ THÊM IMPORT NÀY NHÉ

/**
 * ChatUtils – Hệ thống thông báo độc quyền của AddonBuu. 🚀
 * Đã nâng cấp tính năng hiển thị tên Module riêng biệt! 🎭
 */
public class ChatUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private static final String PREFIX = "§d[AddonBuu] §f";
    private static final String DEBUG_PREFIX = "§a[Debug ⚙️] §7";
    private static final String ERROR_PREFIX = "§c[Lỗi ❌] §f";

    // --- HÀM GỬI LỆNH/CHAT ---
    public static void sendPlayerMsg(String message) {
        if (mc.player == null || mc.player.networkHandler == null || message == null) return;
        if (message.startsWith("#")) {
            mc.player.networkHandler.sendChatMessage(message);
        } else if (message.startsWith("/")) {
            mc.player.networkHandler.sendChatCommand(message.substring(1));
        } else {
            mc.player.networkHandler.sendChatMessage(message);
        }
    }

    // ============================================================
    // DẠNG 1: LOG CHUNG (Hiện [AddonBuu]) - Giữ nguyên như cũ
    // ============================================================
    public static void addModMessage(String message) {
        logToAll(PREFIX + message);
    }

    public static void addErrorMessage(String message) {
        logToAll(ERROR_PREFIX + message);
    }

    public static void debug(String message) {
        logToAll(DEBUG_PREFIX + message);
    }

    // ============================================================
    // DẠNG 2: LOG THEO MODULE (Hiện [TênModule]) - TÍNH NĂNG MỚI ✨
    // ============================================================

    /**
     * Log thông tin kèm tên Module.
     * Cách dùng: ChatUtils.info(this, "Thông báo nè");
     */
    public static void info(Module module, String message) {
        // Định dạng: [TênModule] Nội dung (Màu hồng cho tên module cho nó nổi)
        String modulePrefix = "§7[§d" + module.title + "§7] §f";
        logToAll(modulePrefix + message);
    }

    /**
     * Log lỗi kèm tên Module.
     */
    public static void error(Module module, String message) {
        String modulePrefix = "§7[§c" + module.title + " ❌§7] §f";
        logToAll(modulePrefix + message);
    }

    /**
     * Log Debug kèm tên Module.
     */
    public static void debug(Module module, String message) {
        String modulePrefix = "§7[§a" + module.title + " ⚙️§7] §7";
        logToAll(modulePrefix + message);
    }

    // ============================================================
    // DẠNG 3: DÀNH CHO MIXIN (Truyền tên bằng String) - Cực linh hoạt! 🎭
    // ============================================================

    /**
     * Dùng cho Mixin hoặc những nơi không có đối tượng Module cụ thể.
     * Cách dùng: ChatUtils.info("ItemCopy", "Đã copy thành công!");
     */
    public static void info(String prefixName, String message) {
        String fullPrefix = "§7[§d" + prefixName + "§7] §f";
        logToAll(fullPrefix + message);
    }

    public static void error(String prefixName, String message) {
        String fullPrefix = "§7[§c" + prefixName + " ❌§7] §f";
        logToAll(fullPrefix + message);
    }

    public static void debug(String prefixName, String message) {
        String fullPrefix = "§7[§a" + prefixName + " ⚙️§7] §7";
        logToAll(fullPrefix + message);
    }

    // Hàm phụ trợ để tránh lặp code ghi file và HUD
    private static void logToAll(String fullMsg) {
        ModHudRenderer.addNotification(fullMsg);
        FileLogger.log(fullMsg);
    }

    public static void error(String message) {
        addErrorMessage(message);
    }
}
