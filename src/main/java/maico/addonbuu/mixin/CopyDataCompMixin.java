package maico.addonbuu.mixin;

import maico.addonbuu.modules.auto_luckyvn.CopyDataComp;
import maico.addonbuu.utils.ChatUtils;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class CopyDataCompMixin {

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        // 1. Chỉ chạy khi module CopyDataComp đang bật và là click chuột trái (button 0)
        if (Modules.get().isActive(CopyDataComp.class) && button == 0) {
            HandledScreen<?> screen = (HandledScreen<?>) (Object) this;

            // 2. Sử dụng Accessor để lấy slot đang trỏ vào
            Slot focusedSlot = ((HandledScreenAccessor) screen).getFocusedSlot();

            if (focusedSlot != null && focusedSlot.hasStack()) {
                ItemStack stack = focusedSlot.getStack();

                // 3. Format nội dung: slotId:{components}
                String data = focusedSlot.id + ":" + stack.getComponents().toString();

                // 4. Copy vào Clipboard
                if (MinecraftClient.getInstance().keyboard != null) {
                    MinecraftClient.getInstance().keyboard.setClipboard(data);
                    ChatUtils.addModMessage("📋 §a§lĐÃ COPY SLOT " + focusedSlot.id + "! §fCủa item: §e" + stack.getName().getString());
                }

                // 5. Hủy sự kiện click để tránh bốc item lên
                cir.setReturnValue(true);
            }
        }
    }
}
