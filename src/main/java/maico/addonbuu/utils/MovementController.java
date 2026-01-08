package maico.addonbuu.utils;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalBlock;
import maico.addonbuu.utils.ChatUtils;
import meteordevelopment.meteorclient.settings.*;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.List;

public class MovementController {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    public enum Mode { Baritone, WASD }

    public final Setting<Mode> mode;
    public final Setting<String> baritoneTarget;
    public final Setting<List<String>> wasdCommands;
    // --- SETTING MỚI ĐÂY ---
    public final Setting<Boolean> autoJump;

    public int wasdIndex = 0;
    public int wasdTickCounter = 0;
    public int wasdPauseTimer = 0;
    private boolean active = false;

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

        // Khởi tạo Setting Jump cho Controller
        autoJump = group.add(new BoolSetting.Builder()
            .name(prefix + "-smart-jump")
            .description("Tu dong nhay thong minh khi gap vat can.")
            .defaultValue(true)
            .build()
        );
    }

    public void tick() {
        if (!active || mc.player == null) return;

        // --- TỰ ĐỘNG NHẢY KHI ĐANG DI CHUYỂN ---
        handleSmartAutoJump();

        if (mode.get() == Mode.Baritone) {
            handleBaritone();
        } else {
            handleWASD();
        }
    }

    private void handleSmartAutoJump() {
        if (!autoJump.get() || !mc.player.isOnGround() || mc.player.isSneaking()) return;

        // Chỉ nhảy nếu đang có lệnh di chuyển (đang chạy script hoặc Baritone đang dắt đi)
        boolean isMoving = mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0;
        if (!isMoving) return;

        // Thuật toán nhìn trước 1.5 block
        Vec3d lookVec = Vec3d.fromPolar(0, mc.player.getYaw()).normalize();
        BlockPos blockAheadFeet = BlockPos.ofFloored(mc.player.getPos().add(lookVec.multiply(1.5)).add(0, 0.2, 0));
        BlockPos blockAheadHead = blockAheadFeet.up();

        BlockState stateFeet = mc.world.getBlockState(blockAheadFeet);
        BlockState stateHead = mc.world.getBlockState(blockAheadHead);

        if (stateFeet.isSolidBlock(mc.world, blockAheadFeet) && !stateHead.isSolidBlock(mc.world, blockAheadHead)) {
            mc.player.jump();
        }
    }

    public void handleBaritone() {
        BlockPos pos = parsePos(baritoneTarget.get());
        if (pos == null) { stop(); return; }

        if (mc.player.getBlockPos().equals(pos)) {
            ChatUtils.debug("§aĐã đến tọa độ Baritone! Dừng di chuyển. 🏁");
            stop();
            return;
        }

        if (!BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing()) {
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(pos));
        }
    }

    public void handleWASD() {
        List<String> cmds = wasdCommands.get();
        if (wasdIndex >= cmds.size()) { stop(); return; }

        if (wasdPauseTimer > 0) { resetKeys(); wasdPauseTimer--; return; }

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
        } catch (Exception e) { resetKeys(); wasdIndex++; }
    }

    public void start() {
        active = true;
        wasdIndex = 0;
        wasdTickCounter = 0;
        wasdPauseTimer = 0;
    }

    public void stop() {
        active = false;
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
