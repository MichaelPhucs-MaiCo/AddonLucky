package maico.addonbuu.utils;

import maico.addonbuu.hud.ModHudRenderer;
import net.minecraft.client.MinecraftClient;

/**
 * ChatUtils – Hệ thống thông báo độc quyền của AddonBuu. 🚀
 * Điều hướng toàn bộ log ra HUD riêng, không làm bẩn kênh chat.
 */
public class ChatUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    // Prefix mang đậm bản sắc AddonBuu 😎
    private static final String PREFIX = "§d[AddonBuu] §f";
    private static final String DEBUG_PREFIX = "§a[Debug ⚙️] §7";
    private static final String ERROR_PREFIX = "§c[Lỗi ❌] §f";

    /**
     * Gửi tin nhắn hoặc lệnh ra Server.
     */
    public static void sendPlayerMsg(String message) {
        if (mc.player == null || mc.player.networkHandler == null || message == null) return;

        if (message.startsWith("#")) {
            // Gửi lệnh cho Baritone (Nếu bạn có dùng)
            mc.player.networkHandler.sendChatMessage(message);
        } else if (message.startsWith("/")) {
            // Gửi lệnh Minecraft chính thống
            mc.player.networkHandler.sendChatCommand(message.substring(1));
        } else {
            // Chat bình thường
            mc.player.networkHandler.sendChatMessage(message);
        }
    }

    /**
     * Thông báo thông thường - Hiện lên HUD trung tâm.
     */
    public static void addModMessage(String message) {
        ModHudRenderer.addNotification(PREFIX + message);
    }

    /**
     * Thông báo lỗi - Màu đỏ rực để Mai Cồ chú ý.
     */
    public static void addErrorMessage(String message) {
        ModHudRenderer.addNotification(ERROR_PREFIX + message);
    }

    /**
     * Thông báo Debug - Giúp soi lỗi lúc đang code.
     */
    public static void debug(String message) {
        ModHudRenderer.addNotification(DEBUG_PREFIX + message);
    }
}
