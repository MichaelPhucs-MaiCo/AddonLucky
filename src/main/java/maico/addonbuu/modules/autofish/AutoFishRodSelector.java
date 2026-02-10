package maico.addonbuu.modules.autofish;

import maico.addonbuu.utils.ChatUtils;
import meteordevelopment.meteorclient.settings.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent; // Thêm 's' vào Component
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Hand;

public class AutoFishRodSelector {
    private final MinecraftClient mc = MinecraftClient.getInstance();
    public final Setting<Boolean> stopWhenOutOfRods;

    public AutoFishRodSelector(SettingGroup group) {
        stopWhenOutOfRods = group.add(new BoolSetting.Builder().name("dung-khi-het-can").defaultValue(true).build());
    }

    public boolean update() {
        if (mc.player == null) return false;
        int bestSlot = -1;
        int bestValue = -1;

        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            int val = getRodValue(stack);
            if (val > bestValue) {
                bestValue = val;
                bestSlot = i;
            }
        }

        if (bestSlot == -1) {
            if (stopWhenOutOfRods.get()) ChatUtils.error("AutoFish", "§cĐã hết cần câu!");
            return false;
        }

        if (mc.player.getInventory().selectedSlot != bestSlot) {
            mc.player.getInventory().selectedSlot = bestSlot;
        }
        return true;
    }

    private int getRodValue(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof FishingRodItem)) return -1;

        // --- FIX LỖI: ItemEnchantmentsComponent (số nhiều) ---
        ItemEnchantmentsComponent enchants = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (enchants == null) return 0;

        var registry = mc.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);

        int luck = enchants.getLevel(registry.getOrThrow(Enchantments.LUCK_OF_THE_SEA));
        int lure = enchants.getLevel(registry.getOrThrow(Enchantments.LURE));
        int unbreaking = enchants.getLevel(registry.getOrThrow(Enchantments.UNBREAKING));
        int mending = enchants.getLevel(registry.getOrThrow(Enchantments.MENDING)) > 0 ? 1 : 0;

        return luck * 9 + lure * 9 + unbreaking * 2 + mending;
    }
}
