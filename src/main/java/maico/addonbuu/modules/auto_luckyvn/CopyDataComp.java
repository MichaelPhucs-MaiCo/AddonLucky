package maico.addonbuu.modules.auto_luckyvn;

import maico.addonbuu.AddonBuu;
import maico.addonbuu.utils.ChatUtils;
import meteordevelopment.meteorclient.systems.modules.Module;

public class CopyDataComp extends Module {
    public CopyDataComp() {
        super(AddonBuu.CLICK_SLOT_CUSTOM, "copy-data-comp", "Tu dong copy Slot + Component khi click vao item trong GUI 📋");
    }

    @Override
    public void onActivate() {
        ChatUtils.addModMessage("§a§lĐÃ BẬT! §fMở GUI và Click chuột trái vào item để copy.");
    }

    @Override
    public void onDeactivate() {
        ChatUtils.addModMessage("§c§lĐÃ TẮT! §fClick chuột quay về trạng thái bình thường.");
    }
}
