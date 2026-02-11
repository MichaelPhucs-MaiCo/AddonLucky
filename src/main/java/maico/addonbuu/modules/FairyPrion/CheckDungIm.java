package maico.addonbuu.modules.FairyPrion;

import maico.addonbuu.AddonBuu;
import maico.addonbuu.utils.ChatUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Nuker;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;

public class CheckDungIm extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgControl = settings.createGroup("Quản lý Module");

    // --- GENERAL SETTINGS ---
    private final Setting<String> customCommand = sgGeneral.add(new StringSetting.Builder()
        .name("lệnh-thực-thi")
        .description("Lệnh sẽ gửi khi đứng im quá lâu.")
        .defaultValue("/hub")
        .build()
    );

    private final Setting<Integer> thresholdSeconds = sgGeneral.add(new IntSetting.Builder()
        .name("thời-gian-chờ (s)")
        .description("Số giây đứng im tối đa.")
        .defaultValue(30)
        .min(1)
        .sliderMax(300)
        .build()
    );

    // --- MODULE CONTROL SETTINGS ---
    private final Setting<Boolean> stopAutoSell = sgControl.add(new BoolSetting.Builder()
        .name("tắt-AutoSellFP")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> stopFarmMine = sgControl.add(new BoolSetting.Builder()
        .name("tắt-FarmMineFP")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> stopNukerFP = sgControl.add(new BoolSetting.Builder()
        .name("tắt-NukerFP")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> stopNukerVanilla = sgControl.add(new BoolSetting.Builder()
        .name("tắt-Nuker-Gốc")
        .defaultValue(false)
        .build()
    );

    private BlockPos lastPos = null;
    private int stuckTicks = 0;

    public CheckDungIm() {
        super(AddonBuu.FAIRY_PRISON, "check-dung-im", "Check đứng im + Tự động tắt module & gửi lệnh. 🤖");
    }

    @Override
    public void onActivate() {
        lastPos = null;
        stuckTicks = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        BlockPos currentPos = mc.player.getBlockPos();

        if (lastPos != null && currentPos.equals(lastPos)) {
            stuckTicks++;

            // Đạt ngưỡng thời gian
            if (stuckTicks >= thresholdSeconds.get() * 20) {
                ChatUtils.addModMessage("⚠️ Phát hiện đứng im! Đang dọn dẹp hệ thống... ✨");

                // 1. Tắt các module theo cấu hình
                handleModuleControl();

                // 2. Gửi lệnh
                ChatUtils.sendPlayerMsg(customCommand.get());

                // Reset để bắt đầu chu kỳ mới
                stuckTicks = 0;
            }
        } else {
            lastPos = currentPos;
            stuckTicks = 0;
        }
    }

    private void handleModuleControl() {
        if (stopAutoSell.get()) disableModule(AutoSellFP.class);
        if (stopFarmMine.get()) disableModule(FarmMineFP.class);
        if (stopNukerFP.get()) disableModule(NukerFP.class);
        if (stopNukerVanilla.get()) disableModule(Nuker.class);
    }

    private void disableModule(Class<? extends Module> klass) {
        Module m = Modules.get().get(klass);
        if (m != null && m.isActive()) {
            m.toggle();
            ChatUtils.debug(this, "§7Đã tắt module: §e" + m.title);
        }
    }
}
