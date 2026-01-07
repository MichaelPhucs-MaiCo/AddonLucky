package maico.addonbuu.modules.auto_luckyvn;

import maico.addonbuu.AddonBuu;
import maico.addonbuu.utils.ChatUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AutoClickCustom extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // --- SETTINGS ---
    private final Setting<List<String>> targets = sgGeneral.add(new StringListSetting.Builder()
        .name("danh-sach-target")
        .description("Dinh dang: slot:component lay truc tiep tu module CopyDataComp")
        .defaultValue(List.of(" "))
        .build()
    );

    private final Setting<Integer> matchLength = sgGeneral.add(new IntSetting.Builder()
        .name("do-dai-so-sanh")
        .description("So luong ky tu dau tien cua component dung de kiem tra.")
        .defaultValue(150)
        .min(1)
        .sliderMax(500)
        .build()
    );

    private final Setting<Integer> clickDelay = sgGeneral.add(new IntSetting.Builder()
        .name("delay-click")
        .description("Thoi gian cho giua cac lan click 20=1s.")
        .defaultValue(20)
        .min(0)
        .sliderMax(40)
        .build()
    );

    // --- LOGIC BIẾN ---
    private int timer = 0;
    private final Set<Integer> handledSlots = new HashSet<>();
    private int lastScreenId = -1;

    public AutoClickCustom() {
        super(AddonBuu.CLICK_SLOT_CUSTOM, "auto-click-custom", "Tu dong click vao slot co component khop voi cau hinh 🎯");
    }

    @Override
    public void onActivate() {
        timer = 0;
        handledSlots.clear();
        lastScreenId = -1;
        ChatUtils.addModMessage("§aAutoClickCustom đã §a§lBẬT ✅");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!(mc.currentScreen instanceof GenericContainerScreen screen)) {
            handledSlots.clear();
            lastScreenId = -1;
            return;
        }

        var handler = screen.getScreenHandler();

        if (handler.syncId != lastScreenId) {
            handledSlots.clear();
            lastScreenId = handler.syncId;
        }

        if (timer > 0) {
            timer--;
            return;
        }

        for (String entry : targets.get()) {
            try {
                if (!entry.contains(":")) continue;

                String[] parts = entry.split(":", 2);
                int slotId = Integer.parseInt(parts[0].trim());
                String targetComp = parts[1].trim();

                if (handledSlots.contains(slotId)) continue;

                if (slotId < handler.slots.size()) {
                    ItemStack stack = handler.getSlot(slotId).getStack();
                    if (stack.isEmpty()) continue;

                    // 1. Lấy chuỗi component thực tế của item
                    String fullComp = stack.getComponents().toString();

                    // 2. CẮT CHUỖI THỰC TẾ (truncatedComp)
                    String truncatedComp = fullComp.length() > matchLength.get()
                        ? fullComp.substring(0, matchLength.get())
                        : fullComp;

                    // 3. CẮT LUÔN CHUỖI MỤC TIÊU (targetTruncated) ĐỂ SO SÁNH KHỚP NHAU
                    String targetTruncated = targetComp.length() > matchLength.get()
                        ? targetComp.substring(0, matchLength.get())
                        : targetComp;

                    // 4. KIỂM TRA KHỚP
                    if (truncatedComp.contains(targetTruncated)) {
                        ChatUtils.debug("§6[AutoClick] §fKhớp slot §e" + slotId + "§f. Click ngay! 🖱️");

                        mc.interactionManager.clickSlot(handler.syncId, slotId, 0, SlotActionType.PICKUP, mc.player);

                        handledSlots.add(slotId);
                        timer = clickDelay.get();
                        break;
                    }
                }
            } catch (Exception ignored) {}
        }
    }
}
