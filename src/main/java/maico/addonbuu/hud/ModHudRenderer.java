package maico.addonbuu.hud;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ModHudRenderer {
    private static final List<Notification> activeNotifications = new ArrayList<>();
    private static final List<Notification> historyLog = new ArrayList<>();

    private static final int DISPLAY_TIME = 5000; // 5 giây cho thông báo nổi
    private static final long HISTORY_EXPIRE = 10 * 60 * 1000; // 10 phút tự xóa
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static boolean showHistory = false; // Mặc định là ẩn cho đỡ chướng mắt nhé Mai Cồ

    public static void init() {
        // Đăng ký vẽ HUD
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            render(drawContext);
        });
    }

    public static void addNotification(String text) {
        long now = System.currentTimeMillis();
        String timeStr = "[" + LocalTime.now().format(TIME_FORMAT) + "] ";
        Notification n = new Notification(text, timeStr, now);
        activeNotifications.add(n);
        historyLog.add(n);

        // Giới hạn số lượng hiển thị để không tràn màn hình
        if (historyLog.size() > 20) historyLog.remove(0);
        if (activeNotifications.size() > 5) activeNotifications.remove(0);
    }

    private static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;

        long window = client.getWindow().getHandle();
        boolean isCtrlPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS;
        boolean isShiftPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;

        // --- 1. XỬ LÝ PHÍM TẮT: Ctrl + Shift + Right Arrow (Bật/Tắt Lịch sử) ---
        if (isCtrlPressed && isShiftPressed && GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS) {
            showHistory = !showHistory;
            try { Thread.sleep(200); } catch (Exception ignored) {}
        }

        // --- 2. XỬ LÝ PHÍM TẮT: Ctrl + Shift + Delete (Xóa sạch Lịch sử) 🧹 ---
        if (isCtrlPressed && isShiftPressed && GLFW.glfwGetKey(window, GLFW.GLFW_KEY_DELETE) == GLFW.GLFW_PRESS) {
            historyLog.clear();
            activeNotifications.clear();
            addNotification("§aĐã dọn dẹp sạch sẽ lịch sử! ✨");
            try { Thread.sleep(200); } catch (Exception ignored) {}
        }

        TextRenderer renderer = client.textRenderer;
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        long now = System.currentTimeMillis();

        // TỰ ĐỘNG XÓA LOG SAU 10 PHÚT
        activeNotifications.removeIf(n -> now > n.startTime + DISPLAY_TIME);
        historyLog.removeIf(n -> now > n.startTime + HISTORY_EXPIRE);

        // 1. VẼ LỊCH SỬ (Chỉ hiện khi showHistory = true)
        if (showHistory && !historyLog.isEmpty()) {
            int hX = 10;
            int hY = 10;
            // Cập nhật tiêu đề hiển thị cả phím tắt xóa log luôn cho chuyên nghiệp
            context.drawText(renderer, Text.literal("§e§l--- LỊCH SỬ (▶: Ẩn | Del: Xóa) ---"), hX, hY, 0xFFFFFFFF, true);
            hY += 12;

            for (Notification n : historyLog) {
                String fullMsg = "§7" + n.timestamp + "§f" + n.text;
                context.drawText(renderer, Text.literal(fullMsg), hX, hY, 0xFFFFFFFF, true);
                hY += 10;
            }
        }

        // 2. VẼ THÔNG BÁO NỔI (Nâng cao lên height - 100 theo yêu cầu trước đó)
        if (!activeNotifications.isEmpty()) {
            int y = height - 100;
            for (int i = activeNotifications.size() - 1; i >= 0; i--) {
                String msg = activeNotifications.get(i).text;
                int textWidth = renderer.getWidth(msg);
                int x = (width - textWidth) / 2;
                context.fill(x - 4, y - 2, x + textWidth + 4, y + 10, 0x80000000);
                context.drawText(renderer, Text.literal(msg), x, y, 0xFFFFFFFF, true);
                y -= 12;
            }
        }
    }

    private static class Notification {
        String text;
        String timestamp;
        long startTime;
        Notification(String text, String timestamp, long startTime) {
            this.text = text; this.timestamp = timestamp; this.startTime = startTime;
        }
    }
}
