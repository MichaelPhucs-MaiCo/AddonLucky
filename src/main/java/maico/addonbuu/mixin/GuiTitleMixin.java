package maico.addonbuu.mixin;

import maico.addonbuu.AddonBuu;
import maico.addonbuu.utils.ChatUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class GuiTitleMixin {
    @Inject(method = "setScreen", at = @At("HEAD"))
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        // Chỉ chạy khi tính năng soi title đang bật và screen không phải null
        if (AddonBuu.showGuiTitle && screen != null) {
            String title = screen.getTitle().getString();

            if (!title.isEmpty()) {
                // 1. Hiện log lên HUD và ghi file như cũ
                ChatUtils.addModMessage("§eMở GUI: §f" + title);

                // 2. Tự động copy vế sau (title) vào Clipboard 📋
                // Sử dụng keyboard của MinecraftClient để setClipboard
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.keyboard != null) {
                    client.keyboard.setClipboard(title);

                    // Thông báo nhẹ một cái để Mai Cồ biết là đã copy thành công
                    ChatUtils.debug("§aĐã copy tiêu đề vào Clipboard! 📑");
                }
            }
        }
    }
}
