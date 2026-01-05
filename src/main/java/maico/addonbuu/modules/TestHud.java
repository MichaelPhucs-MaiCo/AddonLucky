package maico.addonbuu.modules;

import maico.addonbuu.AddonBuu;
import maico.addonbuu.utils.ChatUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.util.Formatting;

public class TestHud extends Module {
    private String lastCopiedText = ""; // Lưu nội dung cũ để không copy trùng

    public TestHud() {
        super(AddonBuu.LUCKYVN, "test-hud-copy", "Soi và tự động COPY Action Bar vào Clipboard 📋");
    }

    @Override
    public void onActivate() {
        lastCopiedText = "";
        ChatUtils.addModMessage("🧐 Máy soi đã bật! Thấy dòng mới là tớ tự copy luôn nhé.");
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof OverlayMessageS2CPacket packet) {
            // 1. Lấy nội dung
            String rawText = packet.text().getString();
            String cleanText = Formatting.strip(rawText);

            // 2. Kiểm tra xem có phải dòng mới không để tránh spam Clipboard
            if (!rawText.equals(lastCopiedText)) {

                // COPY VÀO CLIPBOARD (Dùng hàm hệ thống của MC)
                if (mc.keyboard != null) {
                    mc.keyboard.setClipboard(rawText);
                }

                // 3. In log ra để Mai Cồ biết đường mà Ctrl + V
                ChatUtils.debug("--- [ĐÃ COPY DÒNG MỚI] ---");
                ChatUtils.debug("➜ Raw: §f" + rawText);
                ChatUtils.debug("➜ Clean: §7" + cleanText);
                ChatUtils.addModMessage("📋 §a§lĐã copy vào Clipboard! §7Hãy Ctrl+V vào IntelliJ đi.");

                lastCopiedText = rawText;
            }
        }
    }
}
