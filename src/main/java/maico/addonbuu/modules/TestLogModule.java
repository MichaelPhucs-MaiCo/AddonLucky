package maico.addonbuu.modules;

import maico.addonbuu.AddonBuu;
import maico.addonbuu.utils.ChatUtils;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;

public class TestLogModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // Nút bấm ảo ma để test các loại log
    public TestLogModule() {
        super(AddonBuu.ADDONBUU, "test-log", "Module dung de test he thong HUD Notification😎Ctrl+Shift+▶ de an hien");
    }

    @Override
    public void onActivate() {
        ChatUtils.addModMessage("Module Test Log đã được KÍCH HOẠT! 🚀");
    }

    @Override
    public void onDeactivate() {
        ChatUtils.addModMessage("Module Test Log đã TẮT! 💤");
    }

    // Mỗi lần ông bật/tắt module này, nó sẽ bắn log lên HUD để ông check
    // Ông cũng có thể thêm các nút bấm trong phần settings để test Error/Debug nếu muốn nhé!
}
