package maico.addonbuu.utils;

import maico.addonbuu.hud.ModHudRenderer;
import net.minecraft.client.MinecraftClient;
import maico.addonbuu.*;

/**
 * ChatUtils – Hệ thống thông báo độc quyền của AddonBuu. 🚀
 * Giờ đây đã có thêm tính năng ghi Log vào file! 📝
 */
public class ChatUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private static final String PREFIX = "§d[AddonBuu] §f";
    private static final String DEBUG_PREFIX = "§a[Debug ⚙️] §7";
    private static final String ERROR_PREFIX = "§c[Lỗi ❌] §f";

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

    /**
     * Thông báo thông thường - Hiện HUD & Ghi File.
     */
    public static void addModMessage(String message) {
        String fullMsg = PREFIX + message;
        ModHudRenderer.addNotification(fullMsg);
        FileLogger.log(fullMsg); // <--- Ghi vào file 📝
    }

    /**
     * Thông báo lỗi - Hiện HUD & Ghi File.
     */
    public static void addErrorMessage(String message) {
        String fullMsg = ERROR_PREFIX + message;
        ModHudRenderer.addNotification(fullMsg);
        FileLogger.log(fullMsg); // <--- Ghi vào file 📝
    }

    /**
     * Thông báo Debug - Hiện HUD & Ghi File.
     */
    public static void debug(String message) {
        String fullMsg = DEBUG_PREFIX + message;
        ModHudRenderer.addNotification(fullMsg);
        FileLogger.log(fullMsg); // <--- Ghi vào file 📝
    }
}
