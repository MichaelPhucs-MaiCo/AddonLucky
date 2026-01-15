package maico.addonbuu.mixin.slot_index;

import maico.addonbuu.AddonBuu;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public class SlotIndexMixin {
    @Inject(method = "drawSlot", at = @At("TAIL"))
    private void onDrawSlot(DrawContext context, Slot slot, CallbackInfo ci) {
        if (AddonBuu.showSlotIndex) {
            String indexStr = "§l" + slot.id;
            float scale = 0.5f; // Thu nhỏ lại còn 50% kích thước gốc

            context.getMatrices().push();

            // Đẩy layer lên trước item
            context.getMatrices().translate(0, 0, 300);

            // Thực hiện scale nhỏ lại
            context.getMatrices().scale(scale, scale, 1.0f);

            // Tính toán lại tọa độ: (Tọa độ gốc + offset) / scale
            // +1.5f giúp số cách lề một chút cho đẹp
            float x = (slot.x + 1.5f) / scale;
            float y = (slot.y + 1.5f) / scale;

            context.drawText(
                MinecraftClient.getInstance().textRenderer,
                indexStr,
                (int) x,
                (int) y,
                0x99000000,
                false
            );

            context.getMatrices().pop();
        }
    }
}
