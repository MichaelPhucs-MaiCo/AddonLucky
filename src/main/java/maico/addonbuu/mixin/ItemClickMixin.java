package maico.addonbuu.mixin;

import maico.addonbuu.AddonBuu;
import maico.addonbuu.utils.ChatUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class ItemClickMixin {

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        // 1. Chỉ chạy khi chế độ .copy on đang bật và là click chuột trái (button 0)
        if (AddonBuu.itemClickCopy && button == 0) {
            HandledScreen<?> screen = (HandledScreen<?>) (Object) this;

            // 2. Dùng Accessor cậu đã có để lấy slot đang trỏ vào
            Slot focusedSlot = ((HandledScreenAccessor) screen).getFocusedSlot();

            if (focusedSlot != null && focusedSlot.hasStack()) {
                ItemStack stack = focusedSlot.getStack();
                String data = stack.getComponents().toString();

                // 3. Copy vào Clipboard
                if (MinecraftClient.getInstance().keyboard != null) {
                    MinecraftClient.getInstance().keyboard.setClipboard(data);
                    ChatUtils.addModMessage("📋 §a§lĐÃ COPY! §fComponent của: §e" + stack.getName().getString());
                }

                // 4. Hủy sự kiện click để item không bị bốc lên (tránh làm phiền lúc soi đồ)
                cir.setReturnValue(true);
            }
        }
    }
}
