package maico.addonbuu.modules.treo_pho_ban;

import maico.addonbuu.AddonBuu;
import maico.addonbuu.utils.ChatUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;

public class ChongTreoPhoBan extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> thresholdSeconds = sgGeneral.add(new IntSetting.Builder()
        .name("thoi-gian-cho")
        .description("Thoi gian dung im toi da (giay) truoc khi restart/kich hoat.")
        .defaultValue(60)
        .min(1)
        .sliderMax(300)
        .build()
    );

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("pham-vi-check")
        .description("Ban kinh kiem tra xung quanh toa do.")
        .defaultValue(2.0)
        .min(0.5)
        .sliderMax(5.0)
        .build()
    );

    private int stuckTicks = 0;
    // Tọa độ vàng trong làng treo máy của Mai Cồ
    private final Vec3d LOBBY_EXIT = new Vec3d(24, 64, -36);
    private final Vec3d SPAWN_LT = new Vec3d(28, 64, -37);

    public ChongTreoPhoBan() {
        super(AddonBuu.TREOPHOBAN, "chong-treo-pho-ban", "Auto restart hoac KICH HOAT TreoPhoBan neu bi treo 🛡️");
    }

    @Override
    public void onActivate() {
        stuckTicks = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        boolean atLobby = mc.player.getPos().distanceTo(LOBBY_EXIT) <= range.get();
        boolean atSpawn = mc.player.getPos().distanceTo(SPAWN_LT) <= range.get();

        if (atLobby || atSpawn) {
            stuckTicks++;

            // Hiện log mỗi 1 giây để Mai Cồ theo dõi cho sướng mắt
            if (stuckTicks % 20 == 0) {
                int count = stuckTicks / 20;
                int limit = thresholdSeconds.get();
                if (count < limit) {
                    ChatUtils.debug("§ePhát hiện đang đứng im! Sẽ kích hoạt sau: " + (limit - count) + "s ⏳");
                }
            }

            if (stuckTicks >= thresholdSeconds.get() * 20) {
                restartOrEnableTreoPhoBan();
                stuckTicks = 0;
            }
        } else {
            stuckTicks = 0;
        }
    }

    private void restartOrEnableTreoPhoBan() {
        TreoPhoBan module = Modules.get().get(TreoPhoBan.class);
        if (module != null) {
            if (module.isActive()) {
                ChatUtils.addErrorMessage("⚠️ PHÁT HIỆN TREO! Đang khởi động lại TreoPhoBan... 🔄");
                module.toggle();
                module.toggle();
            } else {
                ChatUtils.addModMessage("§6💡 PHÁT HIỆN CHƯA BẬT! Đang tự động KÍCH HOẠT TreoPhoBan... 🚀");
                module.toggle();
            }
        }
    }
}
