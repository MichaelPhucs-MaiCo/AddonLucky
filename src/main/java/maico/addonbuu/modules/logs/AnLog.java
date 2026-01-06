package maico.addonbuu.modules.logs;

import maico.addonbuu.AddonBuu;
import maico.addonbuu.hud.ModHudRenderer;
import maico.addonbuu.utils.ChatUtils;
import meteordevelopment.meteorclient.systems.modules.Module;

public class AnLog extends Module {
    public AnLog() {
        // Đưa vào Category ADDONBUU cho các công cụ tiện ích
        super(AddonBuu.ADDONBUU, "an-log", "Tu dong an cac thong bao noi tren HUD khi kich hoat.");
    }

    @Override
    public void onActivate() {
        // Tương đương với lệnh .anlog
        ModHudRenderer.showNotifications = false;
        ChatUtils.addModMessage("§eĐã ẩn thông báo nổi!.");
    }

    @Override
    public void onDeactivate() {
        // Tương đương với lệnh .hienlog
        ModHudRenderer.showNotifications = true;
        ChatUtils.addModMessage("§aĐã hiện lại thông báo nổi.");
    }
}
