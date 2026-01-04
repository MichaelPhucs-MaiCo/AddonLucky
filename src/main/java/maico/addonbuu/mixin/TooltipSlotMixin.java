package maico.addonbuu.mixin;

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
public abstract class TooltipSlotMixin {
    @Shadow protected Slot focusedSlot;

    @Inject(method = "getTooltipFromItem", at = @At("RETURN"))
    private void onGetTooltip(ItemStack stack, CallbackInfoReturnable<List<Text>> cir) {
        if (focusedSlot != null) {
            List<Text> tooltip = cir.getReturnValue();
            tooltip.add(Text.literal("§aslot số " + focusedSlot.id));
        }
    }
}
