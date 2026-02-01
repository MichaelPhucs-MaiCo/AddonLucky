package maico.addonbuu.modules.auto_luckyvn;

import maico.addonbuu.AddonBuu;
import maico.addonbuu.utils.ChatUtils;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Formatting;

public class TheoDoiCraft extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // --- CẤU HÌNH ---
    public enum Mode { Luyen_Dan, Che_Tao }

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("theo-doi")
        .defaultValue(Mode.Luyen_Dan)
        .build()
    );

    private final Setting<Integer> timeoutSeconds = sgGeneral.add(new IntSetting.Builder()
        .name("thoi-gian-cho (giay)")
        .description("Sau bao nhieu giay khong thay thong bao se gui /spawn.")
        .defaultValue(30)
        .min(5)
        .sliderMax(300)
        .build()
    );

    // --- BIẾN LOGIC ---
    private int ticksPassed = 0;

    public TheoDoiCraft() {
        // Dat trong category LUCKYVN nhu cau yeu cau
        super(AddonBuu.LUCKYVN, "theo-doi-craft", "Tu dong /spawn khi khong craft");
    }

    @Override
    public void onActivate() {
        ticksPassed = 0;
        ChatUtils.info(this, "§aBắt đầu theo dõi: §e" + mode.get().name());
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        ticksPassed++;

        // Kiem tra neu vuot qua thoi gian cho
        if (ticksPassed >= timeoutSeconds.get() * 20) {
            ChatUtils.error(this, "§cQuá " + timeoutSeconds.get() + "s không có phản hồi! Về /spawn...");
            ChatUtils.sendPlayerMsg("/spawn");

            // Reset de tiep tuc vong lap kiem tra moi
            ticksPassed = 0;
        }
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        String msg = Formatting.strip(event.getMessage().getString());
        if (msg == null) return;

        boolean success = false;

        // Logic check theo tung che do
        if (mode.get() == Mode.Luyen_Dan) {
            if (msg.contains("Luyện đan dược thành công")) success = true;
        } else {
            if (msg.contains("Chế tạo vật phẩm thành công")) success = true;
        }

        // Neu thay thong bao thanh cong thi reset dong ho ve 0
        if (success) {
            if (ticksPassed > 100) { // Chi log debug neu da cho mot khoang thoi gian nhat dinh
            }
            ticksPassed = 0;
        }
    }
}
