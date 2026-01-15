package maico.addonbuu.modules;

import maico.addonbuu.AddonBuu;
import maico.addonbuu.utils.ChatUtils;
import meteordevelopment.meteorclient.systems.modules.Module;

public class SlotIndex extends Module {
    public SlotIndex() {
        // Đưa vào Category ADDONBUU - nơi chứa các công cụ tiện ích
        super(AddonBuu.ADDONBUU, "slot-index", "Hien thi so thu tu (Index) cua moi o slot trong GUI.");
    }

    @Override
    public void onActivate() {
        // Đồng bộ với biến toàn cục để Mixin hoạt động
        AddonBuu.showSlotIndex = true;
        ChatUtils.info(this, "§aĐã hiện số thứ tự Slot! ✨");
    }

    @Override
    public void onDeactivate() {
        // Tắt hiển thị
        AddonBuu.showSlotIndex = false;
        ChatUtils.info(this, "§cĐã ẩn số thứ tự Slot.");
    }
}
