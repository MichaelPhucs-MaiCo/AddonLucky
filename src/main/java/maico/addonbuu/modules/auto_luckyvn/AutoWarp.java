package maico.addonbuu.modules.auto_luckyvn;

import maico.addonbuu.AddonBuu;
import maico.addonbuu.utils.ChatUtils;
import maico.addonbuu.utils.MovementController;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AutoWarp extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgScript = settings.createGroup("Script WASD");

    public enum CmdType { Warp, Mine }

    // --- SETTINGS ---
    private final Setting<CmdType> cmdType = sgGeneral.add(new EnumSetting.Builder<CmdType>()
        .name("loai-lenh")
        .description("Chon loai lenh muon gui: /warp hoac /mine.")
        .defaultValue(CmdType.Warp)
        .build()
    );

    private final Setting<String> commandName = sgGeneral.add(new StringSetting.Builder()
        .name("lenh")
        .description("Ten warp hoac khu mine: chetao, luyendan,..")
        .defaultValue("chetao")
        .build()
    );

    private final Setting<String> targetCoords = sgGeneral.add(new StringSetting.Builder()
        .name("toa-do-check")
        .description("Toa do XYZ de check. Neu dung dung se thuc hien lenh.")
        .defaultValue("-1 65 1")
        .build()
    );

    private final Setting<Double> offset = sgGeneral.add(new DoubleSetting.Builder()
        .name("do-sai-lech")
        .description("Do sai lech toa do check.")
        .defaultValue(1.0)
        .min(0.1)
        .sliderMax(10.0)
        .build()
    );

    private final Setting<Integer> postWarpDelay = sgGeneral.add(new IntSetting.Builder()
        .name("delay-sau-lenh")
        .description("Thoi gian cho (giay) sau khi gui lenh.")
        .defaultValue(5)
        .min(0)
        .max(30)
        .sliderMax(30)
        .build()
    );

    // Option Auto Jump giống Minecraft
    private final Setting<Boolean> autoJump = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-jump")
        .description("Tu dong nhay muot ma truoc khi va vao block (Smart Jump).")
        .defaultValue(true)
        .build()
    );

    private final MovementController moveControl = new MovementController(sgScript, "script");

    private enum State { CHECKING, WAITING_DELAY, RUNNING_SCRIPT }
    private State currentState = State.CHECKING;
    private int timer = 0;

    public AutoWarp() {
        super(AddonBuu.LUCKYVN, "auto-warp", "Module thuc thi script: Check toa do -> /warp hoac /mine -> Chay WASD 🚀");
    }

    @Override
    public void onActivate() {
        resetLogic();
        ChatUtils.info(this,"§aAutoWarp đã §a§lBẬT ✅🎯");
    }

    private void resetLogic() {
        currentState = State.CHECKING;
        timer = 0;
        moveControl.stop();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        // --- LOGIC AUTO JUMP MỚI (Mượt hơn) ---
        handleSmartAutoJump();

        switch (currentState) {
            case CHECKING -> {
                if (timer > 0) {
                    timer--;
                    return;
                }

                if (isAtTarget()) {
                    String basePrefix = cmdType.get() == CmdType.Warp ? "/warp" : "/mine";
                    String name = commandName.get().trim();
                    String fullCmd = name.isEmpty() ? basePrefix : basePrefix + " " + name;

                    ChatUtils.addModMessage("§eĐã đúng tọa độ! Gửi lệnh: §f" + fullCmd);
                    ChatUtils.sendPlayerMsg(fullCmd);

                    currentState = State.WAITING_DELAY;
                    timer = postWarpDelay.get() * 20;
                } else {
                    timer = 20; // Check mỗi giây 1 lần cho đỡ lag nếu chưa tới đích
                }
            }

            case WAITING_DELAY -> {
                if (timer > 0) {
                    timer--;
                } else {
                    ChatUtils.addModMessage("§bHết delay. Bắt đầu chạy Script WASD! 🏃‍♂️");
                    currentState = State.RUNNING_SCRIPT;
                    moveControl.start();
                }
            }

            case RUNNING_SCRIPT -> {
                moveControl.tick();
                if (!moveControl.isActive()) {
                    ChatUtils.addModMessage("§aScript kết thúc! Quay lại check tọa độ. 🔄");
                    currentState = State.CHECKING;
                    timer = 0;
                }
            }
        }
    }

    // --- HÀM XỬ LÝ NHẢY THÔNG MINH ---
    private void handleSmartAutoJump() {
        // 1. Kiểm tra điều kiện cơ bản: Đang bật, đang đứng trên đất, không phải đang lén nút Shift
        if (!autoJump.get() || !mc.player.isOnGround() || mc.player.isSneaking()) return;

        // 2. Kiểm tra xem người chơi có đang thực sự muốn di chuyển không (đang bấm nút đi)
        if (mc.player.input.movementForward == 0 && mc.player.input.movementSideways == 0) return;

        // 3. Tính toán vị trí "tương lai" ngay trước mặt (cách khoảng 0.8 block theo hướng nhìn)
        // Lấy vector hướng nhìn, chuẩn hóa về độ dài 1, bỏ qua trục Y
        Vec3d lookVec = Vec3d.fromPolar(0, mc.player.getYaw()).normalize();
        // Điểm cần check cách chân người chơi 1.3 block về phía trước
        BlockPos blockAheadFeet = BlockPos.ofFloored(mc.player.getPos().add(lookVec.multiply(1.5)).add(0, 0.1, 0));
        BlockPos blockAheadHead = blockAheadFeet.up(); // Block ngay trên đầu cái block cản chân

        // 4. Lấy trạng thái block
        BlockState stateFeet = mc.world.getBlockState(blockAheadFeet);
        BlockState stateHead = mc.world.getBlockState(blockAheadHead);

        // 5. Logic quyết định nhảy:
        // NẾU block phía trước chân là khối đặc (Solid) VÀ block phía trên đầu nó KHÔNG phải là khối đặc (thoáng)
        // THÌ NHẢY!
        if (stateFeet.isSolidBlock(mc.world, blockAheadFeet) && !stateHead.isSolidBlock(mc.world, blockAheadHead)) {
            mc.player.jump();
        }
    }


    private boolean isAtTarget() {
        try {
            String[] p = targetCoords.get().split(" ");
            double tx = Double.parseDouble(p[0]);
            double ty = Double.parseDouble(p[1]);
            double tz = Double.parseDouble(p[2]);
            double d = offset.get();

            return Math.abs(mc.player.getX() - tx) <= d &&
                Math.abs(mc.player.getY() - ty) <= d &&
                Math.abs(mc.player.getZ() - tz) <= d;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onDeactivate() {
        ChatUtils.info(this, "§aAutoWarp đã §4§lTẮT ❌");
        moveControl.stop();
    }
}
