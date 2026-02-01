package maico.addonbuu.modules.FairyPrion;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalBlock;
import maico.addonbuu.AddonBuu;
import maico.addonbuu.settings.StringAreaSetting;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.DisconnectedScreen;

import java.util.List;

public class SpamScriptFP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    // Tạo thêm một group riêng cho mấy cái logic an toàn và vòng lặp nè
    private final SettingGroup sgSafety = settings.createGroup("Safety & Loop");

    // --- General Settings ---
    private final Setting<List<String>> script = sgGeneral.add(new StringAreaSetting.Builder()
        .name("script")
        .description("Danh sách lệnh: 'goto x y z' hoặc 'delay seconds'")
        .defaultValue("goto 100 64 100", "delay 5", "goto 200 64 200", "delay 2")
        .build()
    );

    // --- Safety & Loop Settings (Nằm trong group riêng) ---
    private final Setting<Boolean> loop = sgSafety.add(new BoolSetting.Builder()
        .name("loop")
        .description("Tự động lặp lại script khi kết thúc.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> disableOnDisconnect = sgSafety.add(new BoolSetting.Builder()
        .name("disable-on-disconnect")
        .description("Tự tắt module khi bị kick hoặc mất kết nối.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> disableOnLeave = sgSafety.add(new BoolSetting.Builder()
        .name("disable-on-leave")
        .description("Tự tắt module khi bạn chủ động thoát server.")
        .defaultValue(true)
        .build()
    );

    private int currentIndex = 0;
    private int delayTicks = 0;

    public SpamScriptFP() {
        super(AddonBuu.FAIRY_PRISON, "spam-script-fp", "Baritone Script FP điều khiển theo kịch bản.");
    }

    @Override
    public void onActivate() {
        currentIndex = 0;
        delayTicks = 0;
    }

    @Override
    public void onDeactivate() {
        BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (script.get().isEmpty()) return;

        if (delayTicks > 0) {
            delayTicks--;
            return;
        }

        if (currentIndex >= script.get().size()) {
            if (loop.get()) {
                currentIndex = 0;
            } else {
                toggle();
                return;
            }
        }

        executeLine(script.get().get(currentIndex));
        currentIndex++;
    }

    private void executeLine(String line) {
        if (line == null || line.trim().isEmpty()) return;

        String[] parts = line.trim().toLowerCase().split("\\s+");
        String command = parts[0];

        try {
            switch (command) {
                case "goto" -> {
                    if (parts.length >= 4) {
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2]);
                        int z = Integer.parseInt(parts[3]);

                        BaritoneAPI.getProvider().getPrimaryBaritone()
                            .getCustomGoalProcess()
                            .setGoalAndPath(new GoalBlock(x, y, z));
                    }
                }
                case "delay" -> {
                    if (parts.length >= 2) {
                        double seconds = Double.parseDouble(parts[1]);
                        delayTicks = (int) (seconds * 20);
                    }
                }
            }
        } catch (Exception e) {
            info("Lỗi cú pháp tại dòng " + (currentIndex + 1) + ": " + line);
        }
    }

    @EventHandler
    private void onScreenOpen(OpenScreenEvent event) {
        if (disableOnDisconnect.get() && event.screen instanceof DisconnectedScreen) {
            if (isActive()) toggle();
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (disableOnLeave.get()) {
            if (isActive()) toggle();
        }
    }
}
