package maico.addonbuu.modules;

import maico.addonbuu.AddonBuu;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;

import java.util.concurrent.ThreadLocalRandom;

public class AutoClickerCS extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // --- SETTINGS CHUNG ---
    private final Setting<Boolean> inScreens = sgGeneral.add(new BoolSetting.Builder()
        .name("while-in-screens")
        .description("Click ngay cả khi đang mở GUI.")
        .defaultValue(true)
        .build()
    );

    // --- LEFT CLICK SETTINGS ---
    private final Setting<Mode> leftClickMode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode-left")
        .description("Chế độ click chuột trái.")
        .defaultValue(Mode.Press)
        .build()
    );

    private final Setting<Integer> minDelayLeft = sgGeneral.add(new IntSetting.Builder()
        .name("min-delay-left (ms)")
        .description("Thời gian chờ tối thiểu giữa các lần click trái.")
        .defaultValue(100)
        .min(1)
        .sliderMax(1000)
        .visible(() -> leftClickMode.get() == Mode.Press)
        .build()
    );

    private final Setting<Integer> maxDelayLeft = sgGeneral.add(new IntSetting.Builder()
        .name("max-delay-left (ms)")
        .description("Thời gian chờ tối đa giữa các lần click trái.")
        .defaultValue(200)
        .min(1)
        .sliderMax(1000)
        .visible(() -> leftClickMode.get() == Mode.Press)
        .build()
    );

    // --- RIGHT CLICK SETTINGS ---
    private final Setting<Mode> rightClickMode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode-right")
        .description("Chế độ click chuột phải.")
        .defaultValue(Mode.Disabled)
        .build()
    );

    private final Setting<Integer> minDelayRight = sgGeneral.add(new IntSetting.Builder()
        .name("min-delay-right (ms)")
        .description("Thời gian chờ tối thiểu giữa các lần click phải.")
        .defaultValue(100)
        .min(1)
        .sliderMax(1000)
        .visible(() -> rightClickMode.get() == Mode.Press)
        .build()
    );

    private final Setting<Integer> maxDelayRight = sgGeneral.add(new IntSetting.Builder()
        .name("max-delay-right (ms)")
        .description("Thời gian chờ tối đa giữa các lần click phải.")
        .defaultValue(200)
        .min(1)
        .sliderMax(1000)
        .visible(() -> rightClickMode.get() == Mode.Press)
        .build()
    );

    // --- BIẾN LOGIC ---
    private long lastLeftTime, lastRightTime;
    private long currentTargetLeft, currentTargetRight;

    public AutoClickerCS() {
        // Đưa vào Category ADDONBUU của Mai Cồ nhé [cite: 1866]
        super(AddonBuu.ADDONBUU, "auto-clicker-cs", "Tự động click với độ trễ random tính bằng ms (Human-like) 🖱️");
    }

    @Override
    public void onActivate() {
        lastLeftTime = System.currentTimeMillis();
        lastRightTime = System.currentTimeMillis();

        // Tạo mục tiêu ban đầu
        currentTargetLeft = getRandomDelay(minDelayLeft.get(), maxDelayLeft.get());
        currentTargetRight = getRandomDelay(minDelayRight.get(), maxDelayRight.get());

        mc.options.attackKey.setPressed(false);
        mc.options.useKey.setPressed(false);
    }

    @Override
    public void onDeactivate() {
        mc.options.attackKey.setPressed(false);
        mc.options.useKey.setPressed(false);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!inScreens.get() && mc.currentScreen != null) return;

        long now = System.currentTimeMillis();

        // LOGIC CHUỘT TRÁI
        switch (leftClickMode.get()) {
            case Hold -> mc.options.attackKey.setPressed(true);
            case Press -> {
                if (now - lastLeftTime >= currentTargetLeft) {
                    Utils.leftClick();
                    lastLeftTime = now;
                    // Sau mỗi lần click, chọn lại một con số ngẫu nhiên mới cho lần sau
                    currentTargetLeft = getRandomDelay(minDelayLeft.get(), maxDelayLeft.get());
                }
            }
            default -> {}
        }

        // LOGIC CHUỘT PHẢI
        switch (rightClickMode.get()) {
            case Hold -> mc.options.useKey.setPressed(true);
            case Press -> {
                if (now - lastRightTime >= currentTargetRight) {
                    Utils.rightClick();
                    lastRightTime = now;
                    // Tương tự cho chuột phải
                    currentTargetRight = getRandomDelay(minDelayRight.get(), maxDelayRight.get());
                }
            }
            default -> {}
        }
    }

    private long getRandomDelay(int min, int max) {
        if (min >= max) return min;
        return ThreadLocalRandom.current().nextLong(min, max + 1);
    }

    public enum Mode {
        Disabled,
        Hold,
        Press
    }
}
