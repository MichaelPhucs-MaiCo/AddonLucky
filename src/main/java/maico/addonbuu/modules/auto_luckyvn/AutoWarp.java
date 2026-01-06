package maico.addonbuu.modules.auto_luckyvn;

import maico.addonbuu.AddonBuu;
import maico.addonbuu.utils.ChatUtils;
import maico.addonbuu.utils.MovementController;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class AutoWarp extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgScript = settings.createGroup("Script WASD");

    // --- ENUMS ---
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

    // Khai báo "động cơ" di chuyển
    private final MovementController moveControl = new MovementController(sgScript, "script");

    // --- LOGIC BIẾN ---
    private enum State { CHECKING, WAITING_DELAY, RUNNING_SCRIPT }
    private State currentState = State.CHECKING;
    private int timer = 0;

    public AutoWarp() {
        super(AddonBuu.LUCKYVN, "auto-warp", "Module thuc thi script: Check toa do -> /warp hoac /mine -> Chay WASD 🚀");
    }

    @Override
    public void onActivate() {
        resetLogic();
        moveControl.mode.set(MovementController.Mode.WASD);
        ChatUtils.addModMessage("§aAutoWarp đã §a§lBẬT ✅🎯");
    }

    private void resetLogic() {
        currentState = State.CHECKING;
        timer = 0;
        moveControl.stop();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        switch (currentState) {
            case CHECKING -> {
                if (timer > 0) {
                    timer--;
                    return;
                }

                if (isAtTarget()) {
                    // 1. Xác định prefix cơ bản
                    String basePrefix = cmdType.get() == CmdType.Warp ? "/warp" : "/mine";

                    // 2. Lấy tên lệnh và xóa khoảng trắng thừa
                    String name = commandName.get().trim();

                    // 3. Logic "ảo ma": Nếu trống thì chỉ lấy prefix, nếu có chữ thì mới ghép thêm dấu cách và tên
                    String fullCmd = name.isEmpty() ? basePrefix : basePrefix + " " + name;

                    ChatUtils.addModMessage("§eĐã đúng tọa độ! Gửi lệnh: §f" + fullCmd);
                    ChatUtils.sendPlayerMsg(fullCmd);

                    currentState = State.WAITING_DELAY;
                    timer = postWarpDelay.get() * 20;
                } else {
                    timer = 100;
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

                // Kiểm tra xem MovementController đã chạy xong list script chưa
                if (!moveControl.isActive()) {
                    ChatUtils.addModMessage("§aScript kết thúc! Quay lại check tọa độ. 🔄");
                    currentState = State.CHECKING;
                    timer = 0;
                }
            }
        }
    }

    private boolean isAtTarget() {
        try {
            String[] p = targetCoords.get().split(" ");
            double tx = Double.parseDouble(p[0]);
            double ty = Double.parseDouble(p[1]);
            double tz = Double.parseDouble(p[2]);

            double d = offset.get(); // Lấy độ sai lệch từ setting

            return Math.abs(mc.player.getX() - tx) <= d &&
                Math.abs(mc.player.getY() - ty) <= d &&
                Math.abs(mc.player.getZ() - tz) <= d;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onDeactivate() {
        ChatUtils.addModMessage("§aAutoWarp đã §4§lTẮT ❌");
        moveControl.stop();
    }
}
