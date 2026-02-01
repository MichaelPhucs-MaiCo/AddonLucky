package maico.addonbuu.modules.FairyPrion;

import maico.addonbuu.AddonBuu;
import maico.addonbuu.utils.ChatUtils;
import meteordevelopment.meteorclient.events.game.GameLeftEvent; // Thêm import này
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting; // Thêm import này
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.DisconnectedScreen; // Thêm import này
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.screen.slot.SlotActionType;

public class AutoSellFP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    // Tạo Group riêng cho các tính năng an toàn 🛡️
    private final SettingGroup sgSafety = settings.createGroup("Safety");

    // --- SETTINGS GENERAL ---
    private final Setting<Integer> sellInterval = sgGeneral.add(new IntSetting.Builder()
        .name("thoi-gian-ban")
        .description("Tự động gửi /sell sau mỗi X giây.")
        .defaultValue(60)
        .min(1)
        .sliderMax(300)
        .build()
    );

    private final Setting<Integer> clickDelay = sgGeneral.add(new IntSetting.Builder()
        .name("delay-click")
        .description("Thời gian chờ trước khi click (20 ticks = 1 giây).")
        .defaultValue(10)
        .min(0)
        .sliderMax(100)
        .build()
    );

    private final Setting<Integer> targetSlot = sgGeneral.add(new IntSetting.Builder()
        .name("slot-click")
        .description("Slot click trong GUI.")
        .defaultValue(20)
        .min(0)
        .sliderMax(54)
        .build()
    );

    // --- SETTINGS SAFETY (Group riêng biệt) ---
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

    private long lastSellTime = 0;
    private boolean waitingForGui = false;
    private int timer = -1;

    public AutoSellFP() {
        super(AddonBuu.FAIRY_PRISON, "auto-sell-fp", "Tự động /sell💰 kèm bảo hiểm chống lỗi.");
    }

    @Override
    public void onActivate() {
        lastSellTime = System.currentTimeMillis();
        waitingForGui = false;
        timer = -1;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSellTime >= sellInterval.get() * 1000L) {
            sendSellCommand();
        }

        if (waitingForGui && timer >= 0) {
            if (timer == 0) {
                executeClick();
                timer = -1;
            } else {
                timer--;
            }
        }
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof OverlayMessageS2CPacket packet) {
            String text = packet.text().getString().toLowerCase();

            if (text.contains("kho đồ của bạn đã đầy")) {
                ChatUtils.debug(this, "📦 Kho đầy! Gửi /sell...");
                sendSellCommand();
            }
        }
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        // --- Giữ nguyên logic cũ ---
        if (waitingForGui && event.screen instanceof GenericContainerScreen) {
            timer = clickDelay.get();
        }

        // --- Logic mới: Tự tắt khi mất kết nối ---
        if (disableOnDisconnect.get() && event.screen instanceof DisconnectedScreen) {
            if (isActive()) toggle();
        }
    }

    // --- Logic mới: Tự tắt khi thoát game/server ---
    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (disableOnLeave.get()) {
            if (isActive()) toggle();
        }
    }

    private void sendSellCommand() {
        if (mc.player == null) return;

        ChatUtils.sendPlayerMsg("/sell");
        waitingForGui = true;
        lastSellTime = System.currentTimeMillis();
    }

    private void executeClick() {
        if (mc.currentScreen instanceof GenericContainerScreen screen) {
            var handler = screen.getScreenHandler();
            int slotId = targetSlot.get();

            if (slotId < handler.slots.size()) {
                ChatUtils.debug(this, "§6[AutoSell] §fHết thời gian chờ. Click Slot §e" + slotId + "§f ngay! 🖱️");
                mc.interactionManager.clickSlot(handler.syncId, slotId, 0, SlotActionType.PICKUP, mc.player);
            }
        }
        waitingForGui = false;
    }
}
