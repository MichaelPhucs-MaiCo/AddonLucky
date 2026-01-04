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

    // Các Setting sẽ được khởi tạo thông qua constructor
    public final Setting<Mode> mode;
    public final Setting<String> baritoneTarget;
    public final Setting<List<String>> wasdCommands;

    // Biến điều khiển WASD nội bộ
    private int wasdIndex = 0;
    private int wasdTickCounter = 0;
    private int wasdPauseTimer = 0;
    private boolean isActive = false;

    public MovementController(SettingGroup group, String prefix) {
        mode = group.add(new EnumSetting.Builder<Mode>()
            .name(prefix + "-mode")
            .description("Chon kieu di chuyen: Baritone (Thong minh) hoac WASD (Co dien).")
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
            .description("Danh sach lenh WASD. VD: up 3s, down 2s, left 1s, right 1s.")
            .defaultValue(List.of("up 2s", "left 1s"))
            .visible(() -> mode.get() == Mode.WASD)
            .build()
        );
    }

    public void tick() {
        if (!isActive || mc.player == null) return;

        if (mode.get() == Mode.Baritone) {
            handleBaritone();
        } else {
            handleWASD();
        }
    }

    private void handleBaritone() {
        BlockPos pos = parsePos(baritoneTarget.get());
        if (pos != null && !BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing()) {
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(pos));
        }
    }

    private void handleWASD() {
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
                mc.options.forwardKey.setPressed(action.equals("up"));
                mc.options.backKey.setPressed(action.equals("down"));
                mc.options.leftKey.setPressed(action.equals("left"));
                mc.options.rightKey.setPressed(action.equals("right"));
                wasdTickCounter++;
            } else {
                resetKeys();
                wasdTickCounter = 0;
                wasdIndex++;
                wasdPauseTimer = 5;
            }
        } catch (Exception e) { wasdIndex++; }
    }

    public void start() {
        isActive = true;
        wasdIndex = 0;
        wasdTickCounter = 0;
    }

    public void stop() {
        isActive = false;
        resetKeys();
        if (mode.get() == Mode.Baritone) {
            BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
        }
    }

    private void resetKeys() {
        mc.options.forwardKey.setPressed(false);
        mc.options.backKey.setPressed(false);
        mc.options.leftKey.setPressed(false);
        mc.options.rightKey.setPressed(false);
    }

    private BlockPos parsePos(String s) {
        try {
            String[] p = s.split(" ");
            return new BlockPos(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]));
        } catch (Exception e) { return null; }
    }
}


//Hướng dẫn sử dụng

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
