package maico.addonbuu.modules.auto_luckyvn;

import maico.addonbuu.AddonBuu;
import maico.addonbuu.utils.ChatUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.util.Formatting;

public class TuCatDo extends Module {
    private static final long DELAY_MS = 10000; // Nghỉ 10 giây
    private long lastActionTime = 0;

    public TuCatDo() {
        super(AddonBuu.LUCKYVN, "tu-cat-do", "Tu dong bat TuCatDo📦");
    }

    @Override
    public void onActivate() {
        lastActionTime = 0;
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof OverlayMessageS2CPacket packet) {
            long currentTime = System.currentTimeMillis();

            // 1. Kiểm tra Cooldown 10s
            if (currentTime - lastActionTime < DELAY_MS) {
                return;
            }

            String rawText = packet.text().getString();
            String cleanText = normalizeText(rawText);

            // 2. Nhận diện dòng "Tự cất đồ"
            if (cleanText.contains("tucatdo")) {

                // KIỂM TRA TRẠNG THÁI TẮT (Check chính xác cụm màu đỏ + dấu X)
                // Dòng cậu gửi: ...ᴆồ:§f §c✘...
                if (rawText.contains("§c✘") || rawText.contains("§c\u2718")) {
                    ChatUtils.addModMessage("⚠️ Trạng thái: §c§lTẮT ❌ §7-> §e§lGỬI LỆNH /tucatdo");

                    if (mc.player != null) {
                        mc.player.networkHandler.sendChatCommand("tucatdo");
                    }

                    lastActionTime = currentTime; // Bắt đầu nghỉ 30s
                }

                // KIỂM TRA TRẠNG THÁI BẬT (Check chính xác cụm màu xanh + dấu tích)
                // Dòng cậu gửi: ...ᴆồ:§f §a✔...
                else if (rawText.contains("§a✔") || rawText.contains("§a\u2714")) {
                    lastActionTime = currentTime; // Đã bật rồi thì cũng nghỉ 10s mới check tiếp cho đỡ lag
                }
            }
        }
    }

    /**
     * Giải mã Small Caps chuẩn từ dữ liệu Mai Cồ cung cấp
     */
    private String normalizeText(String input) {
        if (input == null) return "";
        String result = Formatting.strip(input).toLowerCase();
        return result
            .replace("ᴛ", "t")
            .replace("ự", "u")
            .replace("ᴄ", "c")
            .replace("ấ", "a")
            .replace("ᴆ", "d") // Thêm ký tự ᴆ vào đây
            .replace("ồ", "o");
    }
}
