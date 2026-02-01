package maico.addonbuu.modules;

import maico.addonbuu.AddonBuu;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;

public class SavePos extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // Cài đặt phím bấm để Copy
    private final Setting<Keybind> copyKey = sgGeneral.add(new KeybindSetting.Builder()
        .name("phim-copy")
        .description("Bấm phím này để copy tọa độ vào clipboard.")
        .defaultValue(Keybind.fromKey(342))
        .build()
    );

    public SavePos() {
        // Sử dụng Category ADDONBUU như ông yêu cầu nhé
        super(AddonBuu.ADDONBUU, "save-pos", "Copy tọa độ (X Y Z) vào Clipboard 📍");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        // Kiểm tra xem phím gán có được bấm không
        if (copyKey.get().isPressed()) {
            // Lấy tọa độ dạng BlockPos (tự động làm tròn thành số nguyên)
            BlockPos pos = mc.player.getBlockPos();

            // Định dạng chuỗi: "X Y Z"
            String coords = String.format("goto %d %d %d", pos.getX(), pos.getY(), pos.getZ());

            // Copy vào Clipboard của máy tính
            mc.keyboard.setClipboard(coords);

            // Thông báo cho Mai Cồ biết đã xong việc
            info("Đã copy tọa độ: §a" + coords + "✨");
        }
    }
}
