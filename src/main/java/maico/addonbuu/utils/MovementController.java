package maico.addonbuu.utils;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalBlock;
import meteordevelopment.meteorclient.settings.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import java.util.List;

public class MovementController {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    public enum Mode { Baritone, WASD }

    public final Setting<Mode> mode;
    public final Setting<String> baritoneTarget;
    public final Setting<List<String>> wasdCommands;

    public int wasdIndex = 0;
    public int wasdTickCounter = 0;
    public int wasdPauseTimer = 0;

    // --- ĐÂY NÈ: Khai báo biến active để lưu trạng thái ---
    private boolean active = false;

    // Hàm này để các Module khác (như AutoWarp) gọi moveControl.isActive()
    public boolean isActive() {
        return active;
    }

    public MovementController(SettingGroup group, String prefix) {
        mode = group.add(new EnumSetting.Builder<Mode>()
            .name(prefix + "-mode")
            .description("Chon kieu di chuyen: Baritone hoac WASD.")
            .defaultValue(Mode.Baritone)
            .build()
        );

        baritoneTarget = group.add(new StringSetting.Builder()
            .name(prefix + "-target")
            .description("Toa do dich cho Baritone (X Y Z).")
            .defaultValue("")
            .visible(() -> mode.get() == Mode.Baritone)
            .build()
        );

        wasdCommands = group.add(new StringListSetting.Builder()
            .name(prefix + "-wasd-list")
            .description("Danh sach lenh WASD. VD: up 3s, delay 2s, left 1s.")
            .defaultValue(List.of("up 2s", "delay 3s", "left 1s"))
            .visible(() -> mode.get() == Mode.WASD)
            .build()
        );
    }

    public void tick() {
        // Kiểm tra biến active
        if (!active || mc.player == null) return;

        if (mode.get() == Mode.Baritone) {
            handleBaritone();
        } else {
            handleWASD();
        }
    }

    public void handleBaritone() {
        BlockPos pos = parsePos(baritoneTarget.get());
        if (pos != null && !BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing()) {
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(pos));
        }
    }

    public void handleWASD() {
        List<String> cmds = wasdCommands.get();
        if (wasdIndex >= cmds.size()) {
            stop();
            return;
        }

        if (wasdPauseTimer > 0) {
            resetKeys();
            wasdPauseTimer--;
            return;
        }

        try {
            String[] parts = cmds.get(wasdIndex).toLowerCase().split(" ");
            String action = parts[0];
            int duration = (int) (Double.parseDouble(parts[1].replace("s", "")) * 20);

            if (wasdTickCounter < duration) {
                if (action.equals("delay")) {
                    resetKeys();
                } else {
                    mc.options.forwardKey.setPressed(action.equals("up"));
                    mc.options.backKey.setPressed(action.equals("down"));
                    mc.options.leftKey.setPressed(action.equals("left"));
                    mc.options.rightKey.setPressed(action.equals("right"));
                }
                wasdTickCounter++;
            } else {
                resetKeys();
                wasdTickCounter = 0;
                wasdIndex++;
                wasdPauseTimer = 5;
            }
        } catch (Exception e) {
            resetKeys();
            wasdIndex++;
        }
    }

    public void start() {
        active = true; // Bật trạng thái
        wasdIndex = 0;
        wasdTickCounter = 0;
        wasdPauseTimer = 0;
    }

    public void stop() {
        active = false; // Tắt trạng thái
        resetKeys();
        if (mode.get() == Mode.Baritone) {
            BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
        }
    }

    public void resetKeys() {
        if (mc.options == null) return;
        mc.options.forwardKey.setPressed(false);
        mc.options.backKey.setPressed(false);
        mc.options.leftKey.setPressed(false);
        mc.options.rightKey.setPressed(false);
    }

    public BlockPos parsePos(String s) {
        try {
            String[] p = s.split(" ");
            return new BlockPos(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]));
        } catch (Exception e) { return null; }
    }
}


//Hướng dẫn sử dụng :))

//public class ModuleMau extends Module {
//    private final SettingGroup sgFarm = settings.createGroup("Cau hinh Movement");
//
//    // Khai báo "động cơ" di chuyển
//    private final MovementController moveControl = new MovementController(sgFarm, "farm");
//
//    @Override
//    public void onActivate() {
//        moveControl.start(); // Kích hoạt khi bật module
//    }
//
//    @EventHandler
//    private void onTick(TickEvent.Post event) {
//        // Chỉ cần gọi đúng 1 dòng này, mọi logic Baritone/WASD tự chạy
//        moveControl.tick();
//    }
//
//    @Override
//    public void onDeactivate() {
//        moveControl.stop(); // Tắt di chuyển khi tắt module
//    }
//}
