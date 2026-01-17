package maico.addonbuu.mixin.check_gui;

import maico.addonbuu.AddonBuu;
import maico.addonbuu.utils.ChatUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class CheckGuiMixin {
    @Inject(method = "setScreen", at = @At("HEAD"))
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        // Chỉ chạy khi tính năng soi title đang bật và screen không phải null (tránh log lúc đóng GUI)
        if (AddonBuu.showCheckGui && screen != null) {
            String title = screen.getTitle().getString();

            if (!title.isEmpty()) {
                // In log ra HUD thông qua ChatUtils xịn xò của cậu
                ChatUtils.addModMessage("§e[Soi GUI] §fTiêu đề: §b" + title);

                // Tự động copy vào Clipboard cho Mai Cồ tiện làm việc luôn nè
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.keyboard != null) {
                    client.keyboard.setClipboard(title);
                    ChatUtils.debug("§aĐã copy title vào bộ nhớ tạm! 📋");
                }
            }
        }
    }
}
