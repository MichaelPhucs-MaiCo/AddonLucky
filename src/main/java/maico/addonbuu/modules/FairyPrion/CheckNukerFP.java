package maico.addonbuu.modules.FairyPrion;

import maico.addonbuu.AddonBuu;
import maico.addonbuu.utils.ChatUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Nuker;
import meteordevelopment.orbit.EventHandler;

public class CheckNukerFP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // --- SETTINGS TỌA ĐỘ (Dạng String để bỏ thanh kéo) ---
    private final Setting<String> x1 = sgGeneral.add(new StringSetting.Builder()
        .name("x1")
        .defaultValue("10102")
        .build()
    );
    private final Setting<String> z1 = sgGeneral.add(new StringSetting.Builder()
        .name("z1")
        .defaultValue("5741")
        .build()
    );
    private final Setting<String> x2 = sgGeneral.add(new StringSetting.Builder()
        .name("x2")
        .defaultValue("10052")
        .build()
    );
    private final Setting<String> z2 = sgGeneral.add(new StringSetting.Builder()
        .name("z2")
        .defaultValue("5791")
        .build()
    );

    public enum Priority { NukerFP, NukerGoc }

    private final Setting<Priority> priority = sgGeneral.add(new EnumSetting.Builder<Priority>()
        .name("uu-tien-bat")
        .description("Chọn module sẽ được tự động bật lại.")
        .defaultValue(Priority.NukerFP)
        .build()
    );

    // Chuyển luôn delay sang String cho đồng bộ "không thanh kéo" nha Mai Cồ
    private final Setting<String> checkDelay = sgGeneral.add(new StringSetting.Builder()
        .name("thoi-gian-cho (s)")
        .description("Số giây chờ trước khi tự động bật lại.")
        .defaultValue("5")
        .build()
    );

    private int stuckTicks = 0;

    public CheckNukerFP() {
        super(AddonBuu.FAIRY_PRISON, "check-nuker-fp", "Tự động bật lại Nuker nếu bị tắt trong vùng chỉ định 🛡️");
    }

    @Override
    public void onActivate() {
        stuckTicks = 0;
        ChatUtils.info(this, "§aĐang canh chừng Nuker trong vùng chỉ định... 👀");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        if (isInRange()) {
            boolean nukerFPActive = Modules.get().isActive(NukerFP.class);
            boolean nukerVanillaActive = Modules.get().isActive(Nuker.class);

            if (!nukerFPActive && !nukerVanillaActive) {
                stuckTicks++;

                try {
                    int delaySeconds = Integer.parseInt(checkDelay.get().trim());
                    if (stuckTicks % 20 == 0) {
                        int remaining = delaySeconds - (stuckTicks / 20);
                        if (remaining > 0) ChatUtils.debug(this, "§ePhát hiện Nuker đang tắt! Kích hoạt sau: §f" + remaining + "s");
                    }

                    if (stuckTicks >= delaySeconds * 20) {
                        activateNuker();
                        stuckTicks = 0;
                    }
                } catch (NumberFormatException e) {
                    stuckTicks = 0; // Reset nếu nhập sai định dạng số
                }
            } else {
                stuckTicks = 0;
            }
        } else {
            stuckTicks = 0;
        }
    }

    private boolean isInRange() {
        try {
            double px = mc.player.getX();
            double pz = mc.player.getZ();

            // Parse từ String sang Integer để tính toán
            int ix1 = Integer.parseInt(x1.get().trim());
            int iz1 = Integer.parseInt(z1.get().trim());
            int ix2 = Integer.parseInt(x2.get().trim());
            int iz2 = Integer.parseInt(z2.get().trim());

            int minX = Math.min(ix1, ix2);
            int maxX = Math.max(ix1, ix2);
            int minZ = Math.min(iz1, iz2);
            int maxZ = Math.max(iz1, iz2);

            return px >= minX && px <= maxX && pz >= minZ && pz <= maxZ;
        } catch (NumberFormatException e) {
            return false; // Trả về false nếu tọa độ nhập vào không phải là số
        }
    }

    private void activateNuker() {
        Module target = (priority.get() == Priority.NukerFP)
            ? Modules.get().get(NukerFP.class)
            : Modules.get().get(Nuker.class);

        if (target != null && !target.isActive()) {
            target.toggle();
            ChatUtils.addModMessage("⚡ §6§lCẢNH BÁO! §fPhát hiện treo đào, đã tự động bật lại §e" + target.title);
        }
    }
}
