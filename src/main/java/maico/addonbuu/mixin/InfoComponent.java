package maico.addonbuu.mixin;

import maico.addonbuu.AddonBuu;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(HandledScreen.class)
public abstract class InfoComponent {
    @Shadow protected Slot focusedSlot;

    @Inject(method = "getTooltipFromItem", at = @At("RETURN"))
    private void onGetTooltip(ItemStack stack, CallbackInfoReturnable<List<Text>> cir) {
        // Chỉ hiện khi tính năng bật và đang lia chuột vào item
        if (AddonBuu.showComponents && focusedSlot != null && focusedSlot.hasStack()) {
            List<Text> tooltip = cir.getReturnValue();

            // Duyệt qua từng component để lấy cả Type (tên) và Value (giá trị)
            stack.getComponents().forEach(component -> {
                // Lấy tên component (bỏ chữ minecraft: cho đỡ rác mắt)
                String typeName = component.type().toString().replace("minecraft:", "");

                // Lấy giá trị chi tiết của component đó
                String valueDetails = component.value().toString();

                // Format: ▸ tên_component: giá_trị
                // §b là màu xanh Cyan cho tên, §f là màu trắng cho giá trị chi tiết
                tooltip.add(Text.literal("§b▸ " + typeName + ": §f" + valueDetails));
            });
        }
    }
}
