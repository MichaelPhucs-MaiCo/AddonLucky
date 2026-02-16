package maico.addonbuu.modules.auto_luckyvn;

import maico.addonbuu.AddonBuu;
import maico.addonbuu.utils.ChatUtils;
import maico.addonbuu.utils.MovementController;
// THÊM CÁC IMPORT MỚI NÈ MAI CỒ ✨
import maico.addonbuu.modules.FairyPrion.SpamScriptFP;
import meteordevelopment.meteorclient.systems.modules.Modules;
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
    // 1. TẠO GROUP MỚI CHO CÁC MODULE HẬU SCRIPT
    private final SettingGroup sgPostScript = settings.createGroup("Post-Script Modules");

    public enum CmdType { Warp, Mine }
    public enum WarpTarget { chetao, luyendan, Custom }

    // --- SETTINGS ---
    private final Setting<CmdType> cmdType = sgGeneral.add(new EnumSetting.Builder<CmdType>()
        .name("loai-lenh")
        .description("Chọn loại lệnh muốn gửi: /warp hoặc /mine.")
        .defaultValue(CmdType.Warp)
        .build()
    );

    private final Setting<WarpTarget> warpTarget = sgGeneral.add(new EnumSetting.Builder<WarpTarget>()
        .name("lenh")
        .description("Chọn tên warp hoặc khu mine.")
        .defaultValue(WarpTarget.chetao)
        .build()
    );

    private final Setting<String> customWarpName = sgGeneral.add(new StringSetting.Builder()
        .name("custom-lenh")
        .description("Nhập tên lệnh nếu chọn Custom.")
        .defaultValue("")
        .visible(() -> warpTarget.get() == WarpTarget.Custom)
        .build()
    );

    private final Setting<String> targetCoords = sgGeneral.add(new StringSetting.Builder()
        .name("toa-do-check")
        .description("Tọa độ XYZ để check. Nếu đúng sẽ thực hiện lệnh.")
        .defaultValue("-1 65 1")
        .build()
    );

    private final Setting<Double> offset = sgGeneral.add(new DoubleSetting.Builder()
        .name("do-sai-lech")
        .description("Độ sai lệch tọa độ check.")
        .defaultValue(1.0)
        .min(0.1)
        .sliderMax(10.0)
        .build()
    );

    private final Setting<Integer> postWarpDelay = sgGeneral.add(new IntSetting.Builder()
        .name("delay-sau-lenh")
        .description("Thời gian chờ (giây) sau khi gửi lệnh.")
        .defaultValue(5)
        .min(0)
        .max(30)
        .sliderMax(30)
        .build()
    );

    private final Setting<Boolean> autoJump = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-jump")
        .description("Tự động nhảy mượt mà trước khi va vào block (Smart Jump).")
        .defaultValue(true)
        .build()
    );

    // MỤC CẬU YÊU CẦU ĐÂY: SETTING ĐỂ BẬT SPAM SCRIPT
    private final Setting<Boolean> enableSpamScript = sgPostScript.add(new BoolSetting.Builder()
        .name("bat-SpamScriptFP")
        .description("Tự động kích hoạt module SpamScriptFP sau khi kết thúc Script WASD.")
        .defaultValue(true)
        .build()
    );

    private final MovementController moveControl = new MovementController(sgScript, "script");

    private enum State { CHECKING, WAITING_DELAY, RUNNING_SCRIPT }
    private State currentState = State.CHECKING;
    private int timer = 0;

    public AutoWarp() {
        super(AddonBuu.LUCKYVN, "auto-warp", "Module thực thi script: Check tọa độ -> /warp hoặc /mine -> Chạy WASD 🚀");
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

        handleSmartAutoJump();

        switch (currentState) {
            case CHECKING -> {
                if (timer > 0) {
                    timer--;
                    return;
                }

                if (isAtTarget()) {
                    String basePrefix = cmdType.get() == CmdType.Warp ? "/warp" : "/mine";
                    String name = (warpTarget.get() == WarpTarget.Custom) ? customWarpName.get() : warpTarget.get().name();
                    String fullCmd = name.trim().isEmpty() ? basePrefix : basePrefix + " " + name.trim();

                    ChatUtils.addModMessage("§eĐã đúng tọa độ! Gửi lệnh: §f" + fullCmd);
                    ChatUtils.sendPlayerMsg(fullCmd);

                    currentState = State.WAITING_DELAY;
                    timer = postWarpDelay.get() * 20;
                } else {
                    timer = 20;
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
                    // 2. GỌI HÀM KẾT THÚC ĐỂ XỬ LÝ LOGIC MỚI
                    finishWorkflow();
                }
            }
        }
    }

    // 3. HÀM XỬ LÝ SAU KHI XONG SCRIPT (BẬT MODULE KHÁC VÀ LOOP)
    private void finishWorkflow() {
        ChatUtils.addModMessage("§6Script kết thúc! Đang kiểm tra module cần bật... 🔥");

        // Tự động bật SpamScriptFP nếu option này được bật trong Settings
        Module spamScript = Modules.get().get(SpamScriptFP.class);
        if (enableSpamScript.get() && spamScript != null && !spamScript.isActive()) {
            spamScript.toggle();
        }

        // Quay lại trạng thái check tọa độ để tạo thành vòng lặp
        ChatUtils.addModMessage("§aQuay lại trạng thái chờ tọa độ. 🔄");
        currentState = State.CHECKING;
        timer = 0;
    }

    private void handleSmartAutoJump() {
        if (!autoJump.get() || !mc.player.isOnGround() || mc.player.isSneaking()) return;
        if (mc.player.input.movementForward == 0 && mc.player.input.movementSideways == 0) return;

        Vec3d lookVec = Vec3d.fromPolar(0, mc.player.getYaw()).normalize();
        BlockPos blockAheadFeet = BlockPos.ofFloored(mc.player.getPos().add(lookVec.multiply(1.5)).add(0, 0.1, 0));
        BlockPos blockAheadHead = blockAheadFeet.up();

        BlockState stateFeet = mc.world.getBlockState(blockAheadFeet);
        BlockState stateHead = mc.world.getBlockState(blockAheadHead);

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
