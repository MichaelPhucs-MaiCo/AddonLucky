package maico.addonbuu.modules.auto_luckyvn;

import maico.addonbuu.AddonBuu;
import maico.addonbuu.utils.ChatUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AutoEnableDanDuoc extends Module {
    private boolean isHandled = false;

    public AutoEnableDanDuoc() {
        super(AddonBuu.LUCKYVN, "AutoEnableDanDuoc", "Tu dong click bat tu dong su dung dan💊");
    }

    @Override
    public void onActivate() {
        isHandled = false;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        // 1. Kiểm tra nếu không mở GUI thì reset flag
        if (!(mc.currentScreen instanceof GenericContainerScreen screen)) {
            isHandled = false;
            return;
        }

        if (isHandled) return;

        var handler = screen.getScreenHandler();

        // 2. Kiểm tra slot 19
        if (handler.slots.size() > 19) {
            ItemStack stack = handler.getSlot(19).getStack();

            if (!stack.isEmpty()) {
                StringBuilder fullData = new StringBuilder();
                fullData.append(stack.getName().getString());

                LoreComponent lore = stack.get(DataComponentTypes.LORE);
                if (lore != null) {
                    for (Text line : lore.lines()) {
                        fullData.append(" ").append(line.getString());
                    }
                }

                String cleanText = normalizeText(fullData.toString());

                // 3. Logic Check và Click
                if (cleanText.contains("trang thai")) {
                    if (cleanText.contains("bat")) {
                        ChatUtils.addModMessage("💊 Tự động sử dụng đan dược đang: §a§lBẬT ✅");
                        isHandled = true;
                    }
                    else if (cleanText.contains("tat")) {
                        ChatUtils.addModMessage("💊 Tự động sử dụng đan dược đang: §c§lTẮT ❌ -> §e§lĐANG BẬT LẠI...");

                        // THỰC HIỆN CLICK VÀO SLOT 19
                        mc.interactionManager.clickSlot(handler.syncId, 19, 0, SlotActionType.PICKUP, mc.player);

                        // IN LOG BÁO ĐÃ BẬT
                        ChatUtils.addModMessage("💊 §a§lĐã bật TỰ ĐỘNG SỬ DỤNG ĐAN DƯỢC thành công! 🚀");

                        isHandled = true; // Click xong rồi thì nghỉ
                    }
                }
            }
        }
    }

    private String normalizeText(String input) {
        if (input == null) return "";
        String result = Formatting.strip(input).toLowerCase();
        return result
            .replace("ᴛ", "t").replace("ʀ", "r").replace("ạ", "a")
            .replace("ɴ", "n").replace("ɢ", "g").replace("ʜ", "h")
            .replace("á", "a").replace("ɪ", "i").replace("ʙ", "b")
            .replace("ậ", "a").replace("ắ", "a");
    }
}
