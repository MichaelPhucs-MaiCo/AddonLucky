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

import java.util.ArrayList; // Nhớ thêm import này
import java.util.List;

@Mixin(HandledScreen.class)
public abstract class InfoComponent {
    @Shadow protected Slot focusedSlot;

    // Phải thêm cancellable = true để có thể setReturnValue
    @Inject(method = "getTooltipFromItem", at = @At("RETURN"), cancellable = true)
    private void onGetTooltip(ItemStack stack, CallbackInfoReturnable<List<Text>> cir) {
        // Chỉ hiện khi tính năng bật và đang lia chuột vào item
        if (AddonBuu.showComponents && focusedSlot != null && focusedSlot.hasStack()) {
            // TẠO BẢN SAO ĐỂ CHỈNH SỬA - ĐÂY LÀ CHÌA KHÓA FIX LỖI 🔑
            List<Text> tooltip = new ArrayList<>(cir.getReturnValue());

            // Duyệt qua từng component để lấy cả Type (tên) và Value (giá trị)
            stack.getComponents().forEach(component -> {
                String typeName = component.type().toString().replace("minecraft:", "");
                String valueDetails = component.value().toString();
                tooltip.add(Text.literal("§b▸ " + typeName + ": §f" + valueDetails));
            });

            // Trả về danh sách mới đã được thêm dòng
            cir.setReturnValue(tooltip);
        }
    }
}
