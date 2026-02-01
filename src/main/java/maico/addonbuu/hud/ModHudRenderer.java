package maico.addonbuu.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList; // Sử dụng cái này cho an toàn luồng

public class ModHudRenderer {
    // Thay đổi ArrayList thành CopyOnWriteArrayList để tránh NullPointerException khi render
    private static final List<Notification> activeNotifications = new CopyOnWriteArrayList<>();
    private static final List<Notification> historyLog = new CopyOnWriteArrayList<>();

    private static final int DISPLAY_TIME = 5000;
    private static final long HISTORY_EXPIRE = 10 * 60 * 1000;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static boolean showHistory = false;
    public static boolean showNotifications = true;

    public static void init() {
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            render(drawContext);
        });
    }

    public static void addNotification(String text) {
        if (text == null) return; // Bảo vệ đầu vào

        long now = System.currentTimeMillis();
        String timeStr = "[" + LocalTime.now().format(TIME_FORMAT) + "] ";
        Notification n = new Notification(text, timeStr, now);

        activeNotifications.add(n);
        historyLog.add(n);

        // Giới hạn số lượng (vẫn an toàn với CopyOnWriteArrayList)
        while (historyLog.size() > 20) historyLog.remove(0);
        while (activeNotifications.size() > 5) activeNotifications.remove(0);
    }

    private static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;

        long window = client.getWindow().getHandle();
        boolean isCtrlPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS;
        boolean isShiftPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;

        if (isCtrlPressed && isShiftPressed && GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS) {
            showHistory = !showHistory;
            try { Thread.sleep(200); } catch (Exception ignored) {}
        }

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

        // Thêm kiểm tra n != null cho chắc cú 100%
        activeNotifications.removeIf(n -> n == null || now > n.startTime + DISPLAY_TIME);
        historyLog.removeIf(n -> n == null || now > n.startTime + HISTORY_EXPIRE);

        if (showHistory && !historyLog.isEmpty()) {
            int hX = 10;
            int hY = 10;
            context.drawText(renderer, Text.literal("§e§l--- LỊCH SỬ (▶: Ẩn | Del: Xóa) ---"), hX, hY, 0xFFFFFFFF, true);
            hY += 12;

            for (Notification n : historyLog) {
                if (n == null) continue;
                String fullMsg = "§7" + n.timestamp + "§f" + n.text;
                context.drawText(renderer, Text.literal(fullMsg), hX, hY, 0xFFFFFFFF, true);
                hY += 10;
            }
        }

        if (showNotifications && !activeNotifications.isEmpty()) {
            int y = height - 100;
            for (int i = activeNotifications.size() - 1; i >= 0; i--) {
                Notification n = activeNotifications.get(i);
                if (n == null) continue;

                String msg = n.text;
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
