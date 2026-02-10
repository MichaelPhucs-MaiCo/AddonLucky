package maico.addonbuu.modules.FairyPrion;

import maico.addonbuu.AddonBuu;
import maico.addonbuu.utils.ChatUtils;
import maico.addonbuu.utils.MovementController;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Nuker;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.Vec3d;

public class AutoWarpFP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgGui = settings.createGroup("GUI & Click Settings");
    private final SettingGroup sgPostScript = settings.createGroup("Post-Script Modules");
    private final SettingGroup sgScript = settings.createGroup("Script WASD");

    public enum MineArea {
        Mine_A("2:{minecraft:custom_name=>empty[siblings=[literal{Khu vực: }"),
        Mine_I("13:{minecraft:custom_name=>empty[siblings=[literal{Khu vực: }"),
        Mine_J("14:{minecraft:custom_name=>empty[siblings=[literal{Khu vực: }"),
        Mine_K("15:{minecraft:custom_name=>empty[siblings=[literal{Khu vực: }"),
        Mine_L("16:{minecraft:custom_name=>empty[siblings=[literal{Khu vực: }"),
        Mine_M("19:{minecraft:custom_name=>empty[siblings=[literal{Khu vực: }"),
        Mine_N("20:{minecraft:custom_name=>empty[siblings=[literal{Khu vực: }"),
        Mine_O("21:{minecraft:custom_name=>empty[siblings=[literal{Khu vực: }"),
        Mine_P("22:{minecraft:custom_name=>empty[siblings=[literal{Khu vực: }"),
        Mine_Q("23:{minecraft:custom_name=>empty[siblings=[literal{Khu vực: }"),
        Mine_R("24:{minecraft:custom_name=>empty[siblings=[literal{Khu vực: }"),
        Mine_S("25:{minecraft:custom_name=>empty[siblings=[literal{Khu vực: }"),
        Mine_T("28:{minecraft:custom_name=>empty[siblings=[literal{Khu vực: }"),
        Mine_U("29:{minecraft:custom_name=>empty[siblings=[literal{Khu vực: }"),
        Mine_V("30:{minecraft:custom_name=>empty[siblings=[literal{Khu vực: }"),
        Mine_W("31:{minecraft:custom_name=>empty[siblings=[literal{Khu vực: }"),
        Mine_X("32:{minecraft:custom_name=>empty[siblings=[literal{Khu vực: }"),
        Mine_Y("33:{minecraft:custom_name=>empty[siblings=[literal{Khu vực: }"),
        Mine_Z("34:{minecraft:custom_name=>empty[siblings=[literal{Khu vực: }"),
        Mine_CS1("37:{minecraft:custom_name=>empty[siblings=[literal{Khu vực: }"),
        Mine_CS2("38:{minecraft:custom_name=>empty[siblings=[literal{Khu vực: }"),
        Mine_CS3("39:{minecraft:custom_name=>empty[siblings=[literal{Khu vực: }"),
        Mine_CS4("40:{minecraft:custom_name=>empty[siblings=[literal{Khu vực: }"),
        Mine_CS5("41:{minecraft:custom_name=>empty[siblings=[literal{Khu vực: }"),
        Mine_CS6("42:{minecraft:custom_name=>empty[siblings=[literal{Khu vực: }"),
        Mine_CS7("43:{minecraft:custom_name=>empty[siblings=[literal{Khu vực: }"),
        Custom("");

        public final String data;
        MineArea(String data) { this.data = data; }
    }

    public enum CmdType { Warp, Mine }

    // --- GENERAL ---
    private final Setting<String> targetCoords = sgGeneral.add(new StringSetting.Builder()
        .name("toa-do-check")
        .description("Tọa độ X Y Z mục tiêu để bắt đầu gửi lệnh mở GUI.")
        .defaultValue("0 52 0")
        .build()
    );

    private final Setting<Double> offset = sgGeneral.add(new DoubleSetting.Builder()
        .name("do-sai-lech")
        .description("Khoảng cách cho phép sai lệch so với tọa độ đích.")
        .defaultValue(1.0)
        .min(0.1)
        .build()
    );

    // --- GUI & CLICK ---
    private final Setting<CmdType> cmdType = sgGui.add(new EnumSetting.Builder<CmdType>()
        .name("loai-lenh")
        .description("Chọn lệnh muốn gửi để mở Menu Mine.")
        .defaultValue(CmdType.Mine)
        .build()
    );

    private final Setting<MineArea> targetMine = sgGui.add(new EnumSetting.Builder<MineArea>()
        .name("khu-vuc-mine")
        .description("Chọn khu vực Mine bạn muốn tự động di chuyển tới.")
        .defaultValue(MineArea.Mine_A)
        .build()
    );

    private final Setting<String> customData = sgGui.add(new StringSetting.Builder()
        .name("custom-slot-comp")
        .description("Dữ liệu slot:component khi chọn Custom (Ví dụ: 2:{minecraft:custom_name...}).")
        .defaultValue("")
        .visible(() -> targetMine.get() == MineArea.Custom)
        .build()
    );

    private final Setting<Integer> clickDelay = sgGui.add(new IntSetting.Builder()
        .name("delay-truoc-click")
        .description("Thời gian chờ (ticks) sau khi thấy GUI rồi mới thực hiện click.")
        .defaultValue(20)
        .min(0)
        .build()
    );

    private final Setting<Double> postClickDelaySeconds = sgGui.add(new DoubleSetting.Builder()
        .name("delay-sau-click")
        .description("Thời gian chờ (giây) sau khi click xong rồi mới bắt đầu chạy Script.")
        .defaultValue(1.0)
        .min(0)
        .build()
    );

    // --- POST-SCRIPT MODULES ---
    private final Setting<Boolean> enableSpamScript = sgPostScript.add(new BoolSetting.Builder()
        .name("bat-SpamScriptFP")
        .description("Tự động kích hoạt module SpamScriptFP sau khi kết thúc Script WASD.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> enableAutoSell = sgPostScript.add(new BoolSetting.Builder()
        .name("bat-AutoSellFP")
        .description("Tự động kích hoạt module AutoSellFP sau khi kết thúc Script WASD.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> enableNukerFP = sgPostScript.add(new BoolSetting.Builder()
        .name("bat-NukerFP")
        .description("Tự động kích hoạt module NukerFP sau khi kết thúc Script WASD.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> enableNukerVanilla = sgPostScript.add(new BoolSetting.Builder()
        .name("bat-Nuker-Goc")
        .description("Tự động kích hoạt module Nuker (Gốc) sau khi kết thúc Script WASD.")
        .defaultValue(false)
        .build()
    );

    // Cài đặt mới để bật FarmMineFP
    private final Setting<Boolean> enableFarmMineFP = sgPostScript.add(new BoolSetting.Builder()
        .name("bat-FarmMineFP")
        .description("Tự động kích hoạt module FarmMineFP sau khi kết thúc Script WASD.")
        .defaultValue(true)
        .build()
    );

    private final MovementController moveControl = new MovementController(sgScript, "script");

    private enum State { CHECKING, WAITING_GUI, WAITING_BEFORE_CLICK, WAITING_POST_CLICK, RUNNING_SCRIPT }
    private State currentState = State.CHECKING;
    private int timer = 0;
    private int targetSlotToClick = -1;
    private int guiTimeout = 0;

    public AutoWarpFP() {
        super(AddonBuu.FAIRY_PRISON, "auto-warp-fp", "Workflow: Mine -> Click -> Script -> Enable Modules.");
    }

    @Override
    public void onActivate() { reset(); }

    private void reset() {
        currentState = State.CHECKING;
        timer = 0;
        targetSlotToClick = -1;
        guiTimeout = 0;
        moveControl.stop();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        switch (currentState) {
            case CHECKING -> {
                if (isAtTarget()) {
                    ChatUtils.sendPlayerMsg(cmdType.get() == CmdType.Warp ? "/warp" : "/mine");
                    currentState = State.WAITING_GUI;
                    guiTimeout = 100;
                    ChatUtils.debug("§eĐã đến tọa độ, gửi lệnh... 📩");
                }
            }
            case WAITING_GUI -> {
                if (guiTimeout > 0) {
                    guiTimeout--;
                    if (mc.currentScreen instanceof GenericContainerScreen screen) {
                        String rawTarget = (targetMine.get() == MineArea.Custom) ? customData.get() : targetMine.get().data;
                        targetSlotToClick = findSlotByComponent(screen, rawTarget);

                        if (targetSlotToClick != -1) {
                            ChatUtils.debug("§aKhớp Component tại slot " + targetSlotToClick + "! 🎯");
                            currentState = State.WAITING_BEFORE_CLICK;
                            timer = clickDelay.get();
                        }
                    }
                } else {
                    ChatUtils.error("Không thấy GUI hoặc dữ liệu không khớp. Reset...");
                    currentState = State.CHECKING;
                }
            }
            case WAITING_BEFORE_CLICK -> {
                if (timer > 0) timer--;
                else executeClick();
            }
            case WAITING_POST_CLICK -> {
                if (timer > 0) timer--;
                else {
                    currentState = State.RUNNING_SCRIPT;
                    moveControl.start();
                }
            }
            case RUNNING_SCRIPT -> {
                moveControl.tick();
                if (!moveControl.isActive()) finishWorkflow();
            }
        }
    }

    private int findSlotByComponent(GenericContainerScreen screen, String rawTarget) {
        if (!rawTarget.contains(":")) return -1;
        try {
            String[] parts = rawTarget.split(":", 2);
            int slotId = Integer.parseInt(parts[0].trim());
            String targetComp = parts[1].trim();

            var handler = screen.getScreenHandler();
            if (slotId < handler.slots.size()) {
                ItemStack stack = handler.getSlot(slotId).getStack();
                if (stack.isEmpty()) return -1;

                String fullComp = stack.getComponents().toString();
                if (fullComp.contains(targetComp)) return slotId;
            }
        } catch (Exception ignored) {}
        return -1;
    }

    private void executeClick() {
        if (mc.currentScreen instanceof GenericContainerScreen screen && targetSlotToClick != -1) {
            mc.interactionManager.clickSlot(screen.getScreenHandler().syncId, targetSlotToClick, 0, SlotActionType.PICKUP, mc.player);
            mc.player.closeHandledScreen();

            currentState = State.WAITING_POST_CLICK;
            timer = (int) (postClickDelaySeconds.get() * 20);
            ChatUtils.debug("§aClick xong! Đang chờ Script... 🖱️");
        } else {
            currentState = State.CHECKING;
        }
    }

    private void finishWorkflow() {
        ChatUtils.addModMessage("§6Hoàn tất Script! Bật các module🔥");
        toggleModuleState(SpamScriptFP.class, enableSpamScript.get());
        toggleModuleState(AutoSellFP.class, enableAutoSell.get());
        toggleModuleState(NukerFP.class, enableNukerFP.get());
        toggleModuleState(Nuker.class, enableNukerVanilla.get());

        // Kích hoạt FarmMineFP nếu tùy chọn được bật
        toggleModuleState(FarmMineFP.class, enableFarmMineFP.get());

        currentState = State.CHECKING;
    }

    private void toggleModuleState(Class<? extends Module> klass, boolean shouldBeActive) {
        Module m = Modules.get().get(klass);
        if (m != null && m.isActive() != shouldBeActive) m.toggle();
    }

    private boolean isAtTarget() {
        try {
            String[] p = targetCoords.get().split(" ");
            return mc.player.getPos().isInRange(new Vec3d(Double.parseDouble(p[0]), Double.parseDouble(p[1]), Double.parseDouble(p[2])), offset.get());
        } catch (Exception e) { return false; }
    }
}
