package maico.addonbuu.hud;

import maico.addonbuu.modules.SaveTarget;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class SaveTargetHud {
    public static void init() {
        // Đăng ký sự kiện vẽ HUD
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.world == null) return;

            SaveTarget module = Modules.get().get(SaveTarget.class);
            if (module != null && module.isActive()) {
                // Cập nhật dữ liệu mới nhất
                SaveTarget.updateTarget();

                if (SaveTarget.targetPos != null) {
                    // Format: [Khoảng cách] | [Tọa độ gốc của block]
                    String msg = String.format("§bTarget: §f%d %d %d §7| §aDistance: §f%.2f block",
                        SaveTarget.targetPos.getX(),
                        SaveTarget.targetPos.getY(),
                        SaveTarget.targetPos.getZ(),
                        SaveTarget.distance);

                    // Gửi tin nhắn dạng Overlay (Action Bar)
                    mc.player.sendMessage(Text.literal(msg), true);
                }
            }
        });
    }
}
