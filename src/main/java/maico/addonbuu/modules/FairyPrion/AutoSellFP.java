package maico.addonbuu.modules.FairyPrion;

import maico.addonbuu.AddonBuu;
import maico.addonbuu.utils.ChatUtils;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules; // Cần thiết để lấy module khác
import meteordevelopment.meteorclient.systems.modules.world.Nuker; // Import Nuker gốc
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.screen.slot.SlotActionType;

public class AutoSellFP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSafety = settings.createGroup("Safety");
    private final SettingGroup sgToggle = settings.createGroup("Module Control");

    // --- SETTINGS GENERAL ---
    private final Setting<Integer> sellInterval = sgGeneral.add(new IntSetting.Builder()
        .name("thoi-gian-ban")
        .description("Tự động gửi /sell sau mỗi X giây.")
        .defaultValue(60)
        .min(1)
        .sliderMax(300)
        .build()
    );

    private final Setting<Integer> fullInvCooldown = sgGeneral.add(new IntSetting.Builder()
        .name("cooldown-full-kho")
        .description("Chống spam sell liên tục khi kho đầy (giây).")
        .defaultValue(5)
        .min(1)
        .sliderMax(30)
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

    // --- MODULE CONTROL ---
    private final Setting<Boolean> toggleNukerFP = sgToggle.add(new BoolSetting.Builder()
        .name("tat-NukerFP")
        .description("Tắt NukerFP khi chuẩn bị sell và bật lại sau khi xong.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> toggleNukerVanilla = sgToggle.add(new BoolSetting.Builder()
        .name("tat-Nuker-Goc")
        .description("Tắt Nuker gốc khi chuẩn bị sell và bật lại sau khi xong.")
        .defaultValue(false)
        .build()
    );

    // --- SETTINGS SAFETY ---
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
    private long lastFullInvTime = 0;
    private boolean waitingForGui = false;
    private int timer = -1;

    // Biến ghi nhớ trạng thái để bật lại
    private boolean nukerFPWasActive = false;
    private boolean nukerVanillaWasActive = false;

    public AutoSellFP() {
        super(AddonBuu.FAIRY_PRISON, "auto-sell-fp", "Tự động /sell");
    }

    @Override
    public void onActivate() {
        lastSellTime = System.currentTimeMillis();
        lastFullInvTime = 0;
        waitingForGui = false;
        timer = -1;
        nukerFPWasActive = false;
        nukerVanillaWasActive = false;
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
                long now = System.currentTimeMillis();
                // Chỉ sell nếu đã vượt qua thời gian cooldown chống spam
                if (now - lastFullInvTime > fullInvCooldown.get() * 1000L) {
                    ChatUtils.debug(this, "📦 Kho đầy! Gửi sell...");
                    sendSellCommand();
                    lastFullInvTime = now;
                }
            }
        }
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (waitingForGui && event.screen instanceof GenericContainerScreen) {
            timer = clickDelay.get();
        }

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

    private void sendSellCommand() {
        if (mc.player == null) return;

        // 1. Tạm thời tắt các module Nuker nếu được cấu hình
        handleModules(false);

        // 2. Gửi lệnh sell
        ChatUtils.sendPlayerMsg("/sell");
        waitingForGui = true;
        lastSellTime = System.currentTimeMillis();
    }

    private void executeClick() {
        if (mc.currentScreen instanceof GenericContainerScreen screen) {
            var handler = screen.getScreenHandler();
            int slotId = targetSlot.get();

            if (slotId < handler.slots.size()) {
                ChatUtils.debug(this, "§6[AutoSell] §fĐang click Slot §e" + slotId + "§f để xác nhận sell.");
                mc.interactionManager.clickSlot(handler.syncId, slotId, 0, SlotActionType.PICKUP, mc.player);
            }
        }

        // Sau khi click (hoặc nếu GUI bị đóng), bật lại các module
        handleModules(true);
        waitingForGui = false;
    }

    /**
     * Hàm quản lý bật/tắt module Nuker
     * @param activate true để bật lại, false để tắt đi
     */
    private void handleModules(boolean activate) {
        // Xử lý NukerFP
        if (toggleNukerFP.get()) {
            Module m = Modules.get().get(NukerFP.class);
            if (m != null) {
                if (!activate && m.isActive()) {
                    nukerFPWasActive = true;
                    m.toggle();
                } else if (activate && nukerFPWasActive && !m.isActive()) {
                    m.toggle();
                    nukerFPWasActive = false;
                }
            }
        }

        // Xử lý Nuker gốc
        if (toggleNukerVanilla.get()) {
            Module m = Modules.get().get(Nuker.class);
            if (m != null) {
                if (!activate && m.isActive()) {
                    nukerVanillaWasActive = true;
                    m.toggle();
                } else if (activate && nukerVanillaWasActive && !m.isActive()) {
                    m.toggle();
                    nukerVanillaWasActive = false;
                }
            }
        }
    }
}
