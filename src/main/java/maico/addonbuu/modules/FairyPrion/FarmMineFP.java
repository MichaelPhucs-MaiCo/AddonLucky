package maico.addonbuu.modules.FairyPrion;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalBlock;
import maico.addonbuu.AddonBuu;
import maico.addonbuu.settings.StringAreaSetting;
import maico.addonbuu.utils.ChatUtils;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.AutoEat;
import meteordevelopment.meteorclient.systems.modules.world.Nuker;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.PickaxeItem;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;

public class FarmMineFP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgScript = settings.createGroup("Kịch bản Script");
    private final SettingGroup sgMining = settings.createGroup("Cấu hình Đào & Về");
    private final SettingGroup sgAutoSell = settings.createGroup("Cấu hình Tự động bán");
    private final SettingGroup sgAutoEat = settings.createGroup("Tự động ăn"); // Group mới nè
    private final SettingGroup sgSafety = settings.createGroup("An toàn");

    // --- GENERAL ---
    private final Setting<List<Block>> whitelist = sgGeneral.add(new BlockListSetting.Builder()
        .name("whitelist-block")
        .defaultValue(Collections.emptyList())
        .build()
    );

    public enum NukerMode { NukerFP, NukerGoc }
    private final Setting<NukerMode> nukerMode = sgGeneral.add(new EnumSetting.Builder<NukerMode>()
        .name("loai-nuker")
        .defaultValue(NukerMode.NukerFP)
        .build()
    );

    // --- SCRIPT ---
    private final Setting<List<String>> coordsList = sgScript.add(new StringAreaSetting.Builder()
        .name("danh-sach-toa-do")
        .defaultValue("10023 81 3581")
        .build()
    );

    private final Setting<Boolean> loop = sgScript.add(new BoolSetting.Builder()
        .name("lap-lai-vong")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> scanDelay = sgScript.add(new IntSetting.Builder()
        .name("delay-quet-toa-do")
        .defaultValue(10)
        .build()
    );

    // --- MINING & RETURN ---
    private final Setting<Integer> stopY = sgMining.add(new IntSetting.Builder()
        .name("cao-do-dung-dao (Y)")
        .defaultValue(31)
        .build()
    );

    private final Setting<Integer> preReturnDelay = sgMining.add(new IntSetting.Builder()
        .name("delay-truoc-khi-ve")
        .defaultValue(20)
        .build()
    );

    private final Setting<String> returnCmd = sgMining.add(new StringSetting.Builder()
        .name("lenh-ve-spawn")
        .defaultValue("/mine")
        .build()
    );

    private final Setting<Integer> guiSlotId = sgMining.add(new IntSetting.Builder()
        .name("slot-click-ve-spawn")
        .defaultValue(37)
        .build()
    );

    private final Setting<Integer> clickDelay = sgMining.add(new IntSetting.Builder()
        .name("delay-click-gui")
        .defaultValue(20)
        .build()
    );

    private final Setting<Integer> postTaskDelay = sgMining.add(new IntSetting.Builder()
        .name("delay-sau-nhiem-vu")
        .defaultValue(60)
        .build()
    );

    // --- AUTO SELL ---
    private final Setting<Boolean> controlAutoSell = sgAutoSell.add(new BoolSetting.Builder()
        .name("quan-ly-autosell")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> autoSellOffY = sgAutoSell.add(new IntSetting.Builder()
        .name("y-tat-autosell")
        .defaultValue(40)
        .build()
    );

    // --- AUTO EAT (NEW) ---
    private final Setting<Boolean> autoEatEnabled = sgAutoEat.add(new BoolSetting.Builder()
        .name("tu-dong-an")
        .description("Tự động bật AutoEat của Meteor khi đói sau khi teleport.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> hungerThreshold = sgAutoEat.add(new IntSetting.Builder()
        .name("nguong-doi")
        .description("Nếu độ đói dưới mức này sẽ bắt đầu ăn.")
        .defaultValue(16)
        .min(1).max(20)
        .build()
    );

    // --- SAFETY ---
    private final Setting<Boolean> disableOnDisconnect = sgSafety.add(new BoolSetting.Builder()
        .name("disable-on-disconnect")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> disableOnLeave = sgSafety.add(new BoolSetting.Builder()
        .name("disable-on-leave")
        .defaultValue(true)
        .build()
    );

    // --- LOGIC BIẾN ---
    private enum State { SCANNING, MOVING, MINING, WAITING_FOR_RETURN, RETURNING, WAITING_GUI_CLICK, WAITING_POST_TELEPORT, CHECK_HUNGER, EATING }
    private State currentState = State.SCANNING;
    private int currentIndex = 0;
    private int timer = 0;
    private BlockPos currentTargetPos = null;

    public FarmMineFP() {
        super(AddonBuu.FAIRY_PRISON, "farm-mine-fp", "Tự động check block, di chuyển và đào theo list tọa độ 🚀");
    }

    @Override
    public void onActivate() {
        currentIndex = 0;
        currentState = State.SCANNING;
        timer = 0;
    }

    @Override
    public void onDeactivate() {
        stopMovementAndMining();
        // Đảm bảo tắt AutoEat nếu module bị tắt đột ngột
        toggleModule(AutoEat.class, false);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;

        if (timer > 0) {
            timer--;
            return;
        }

        switch (currentState) {
            case SCANNING -> handleScanning();
            case MOVING -> handleMoving();
            case MINING -> handleMining();
            case WAITING_FOR_RETURN -> handleWaitingForReturn();
            case RETURNING -> handleReturning();
            case WAITING_GUI_CLICK -> handleWaitingGuiClick();
            case WAITING_POST_TELEPORT -> {
                currentState = State.CHECK_HUNGER;
                timer = 0;
            }
            case CHECK_HUNGER -> handleCheckHunger();
            case EATING -> handleEating();
        }
    }

    private void handleScanning() {
        List<String> list = coordsList.get();
        if (currentIndex >= list.size()) {
            if (loop.get()) {
                currentIndex = 0;
                ChatUtils.info(this, "§eLặp lại vòng mới... 🔄");
                timer = postTaskDelay.get();
            } else {
                toggle();
                return;
            }
        }

        BlockPos pos = parsePos(list.get(currentIndex));
        if (pos == null) { currentIndex++; return; }

        if (whitelist.get().contains(mc.world.getBlockState(pos).getBlock())) {
            currentTargetPos = pos;
            currentState = State.MOVING;
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(pos.up()));
        } else {
            currentIndex++;
            timer = scanDelay.get();
        }
    }

    private void handleMoving() {
        if (currentTargetPos == null) return;
        BlockPos pPos = mc.player.getBlockPos();
        if (pPos.getX() == currentTargetPos.getX() && pPos.getZ() == currentTargetPos.getZ() && pPos.getY() == currentTargetPos.getY() + 1) {
            BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
            toggleAutoSell(true);
            setNukerState(true);
            currentState = State.MINING;
        }
    }

    private void handleMining() {
        if (mc.player.getY() <= autoSellOffY.get()) toggleAutoSell(false);
        if (mc.player.getBlockPos().getY() <= stopY.get()) {
            setNukerState(false);
            currentState = State.WAITING_FOR_RETURN;
            timer = preReturnDelay.get();
        }
    }

    private void handleWaitingForReturn() {
        ChatUtils.sendPlayerMsg(returnCmd.get());
        currentState = State.RETURNING;
        timer = 0;
    }

    private void handleReturning() {
        if (timer++ > 100) {
            currentIndex++;
            currentState = State.SCANNING;
        }
    }

    private void handleWaitingGuiClick() {
        if (mc.currentScreen instanceof GenericContainerScreen screen) {
            mc.interactionManager.clickSlot(screen.getScreenHandler().syncId, guiSlotId.get(), 0, SlotActionType.PICKUP, mc.player);
            // Sau khi click xong, đợi world load ổn định
            currentState = State.WAITING_POST_TELEPORT;
            timer = postTaskDelay.get();
        } else {
            currentIndex++;
            currentState = State.SCANNING;
        }
    }

    // Logic kiểm tra đói
    private void handleCheckHunger() {
        if (autoEatEnabled.get() && mc.player.getHungerManager().getFoodLevel() <= hungerThreshold.get()) {
            ChatUtils.debug(this, "§eĐói quá! Đang bật AutoEat để nạp năng lượng... 🍖");
            toggleModule(AutoEat.class, true);
            currentState = State.EATING;
        } else {
            // Không đói hoặc không bật tính năng ăn -> Đi tiếp
            currentIndex++;
            currentState = State.SCANNING;
            timer = scanDelay.get();
        }
    }

    // Đợi ăn xong
    private void handleEating() {
        if (mc.player.getHungerManager().getFoodLevel() > hungerThreshold.get()) {
            toggleModule(AutoEat.class, false);
            ChatUtils.debug(this, "§aĐã no! Cầm cúp và tiếp tục công việc. ⛏️");

            // Đảm bảo cầm lại cúp
            switchToPickaxe();

            currentIndex++;
            currentState = State.SCANNING;
            timer = scanDelay.get();
        }
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (currentState == State.RETURNING && event.screen instanceof GenericContainerScreen) {
            currentState = State.WAITING_GUI_CLICK;
            timer = clickDelay.get();
        }
        if (disableOnDisconnect.get() && event.screen instanceof DisconnectedScreen && isActive()) toggle();
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (disableOnLeave.get() && isActive()) toggle();
    }

    // --- HELPERS ---
    private void setNukerState(boolean active) {
        Module nuker = (nukerMode.get() == NukerMode.NukerFP) ? Modules.get().get(NukerFP.class) : Modules.get().get(Nuker.class);
        if (nuker != null && nuker.isActive() != active) nuker.toggle();
    }

    private void toggleAutoSell(boolean active) {
        if (!controlAutoSell.get()) return;
        toggleModule(AutoSellFP.class, active);
    }

    private void toggleModule(Class<? extends Module> klass, boolean active) {
        Module m = Modules.get().get(klass);
        if (m != null && m.isActive() != active) m.toggle();
    }

    private void switchToPickaxe() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof PickaxeItem) {
                mc.player.getInventory().selectedSlot = i;
                return;
            }
        }
    }

    private void stopMovementAndMining() {
        BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
        setNukerState(false);
    }

    private BlockPos parsePos(String s) {
        try {
            String[] p = s.trim().split("\\s+");
            return new BlockPos(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]));
        } catch (Exception e) { return null; }
    }
}
