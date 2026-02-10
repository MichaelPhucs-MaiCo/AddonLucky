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
import meteordevelopment.meteorclient.systems.modules.world.Nuker;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;

public class FarmMineFP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgScript = settings.createGroup("Kịch bản Script");
    private final SettingGroup sgMining = settings.createGroup("Cấu hình Đào & Về");
    private final SettingGroup sgAutoSell = settings.createGroup("Cấu hình Tự động bán"); // Group mới
    private final SettingGroup sgSafety = settings.createGroup("An toàn");

    // --- GENERAL ---
    private final Setting<List<Block>> whitelist = sgGeneral.add(new BlockListSetting.Builder()
        .name("whitelist-block")
        .description("Danh sách block quặng để kiểm tra.")
        .defaultValue(Collections.emptyList())
        .build()
    );

    public enum NukerMode { NukerFP, NukerGoc }
    private final Setting<NukerMode> nukerMode = sgGeneral.add(new EnumSetting.Builder<NukerMode>()
        .name("loai-nuker")
        .description("Chọn module Nuker sẽ bật khi đào.")
        .defaultValue(NukerMode.NukerFP)
        .build()
    );

    // --- SCRIPT ---
    private final Setting<List<String>> coordsList = sgScript.add(new StringAreaSetting.Builder()
        .name("danh-sach-toa-do")
        .description("Định dạng: X Y Z (Mỗi dòng 1 tọa độ)")
        .defaultValue("10023 81 3581")
        .build()
    );

    private final Setting<Boolean> loop = sgScript.add(new BoolSetting.Builder()
        .name("lap-lai-vong")
        .description("Tự động quay lại tọa độ đầu tiên sau khi hết danh sách.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> scanDelay = sgScript.add(new IntSetting.Builder()
        .name("delay-quet-toa-do")
        .description("Thời gian nghỉ (tick) trước khi check dòng tiếp theo trong script.")
        .defaultValue(10)
        .min(0)
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
        .description("Nghỉ tại chỗ (Y=31) sau khi đào xong rồi mới gửi /mine (tick).")
        .defaultValue(20)
        .min(0)
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
        .description("Đợi bao nhiêu tick sau khi GUI mở rồi mới Click slot (Delay sau /mine).")
        .defaultValue(20)
        .min(0)
        .build()
    );

    private final Setting<Integer> postTaskDelay = sgMining.add(new IntSetting.Builder()
        .name("delay-sau-nhiem-vu")
        .description("Nghỉ sau khi đã Teleport về spawn để ổn định vị trí (tick).")
        .defaultValue(60)
        .min(0)
        .build()
    );

    // --- AUTO SELL (NEW GROUP) ---
    private final Setting<Boolean> controlAutoSell = sgAutoSell.add(new BoolSetting.Builder()
        .name("quan-ly-autosell")
        .description("Tự động bật/tắt AutoSellFP theo cao độ.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> autoSellOffY = sgAutoSell.add(new IntSetting.Builder()
        .name("y-tat-autosell")
        .description("Tắt AutoSellFP khi xuống đến cao độ này.")
        .defaultValue(40)
        .min(-64)
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
    private enum State { SCANNING, MOVING, MINING, WAITING_FOR_RETURN, RETURNING, WAITING_GUI_CLICK, WAITING_NEXT }
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
        ChatUtils.info(this, "§aĐã kích hoạt FarmMineFP!");
    }

    @Override
    public void onDeactivate() {
        stopMovementAndMining();
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
            case WAITING_NEXT -> {
                currentIndex++;
                currentState = State.SCANNING;
                timer = scanDelay.get();
            }
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
                ChatUtils.info(this, "§bXong script! Tắt module.");
                toggle();
                return;
            }
        }

        BlockPos pos = parsePos(list.get(currentIndex));
        if (pos == null) {
            currentIndex++;
            return;
        }

        Block block = mc.world.getBlockState(pos).getBlock();
        if (whitelist.get().contains(block)) {
            currentTargetPos = pos;
            currentState = State.MOVING;
            BlockPos movePos = pos.up();
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(movePos));
        } else {
            currentIndex++;
            timer = scanDelay.get();
        }
    }

    private void handleMoving() {
        if (currentTargetPos == null) return;
        BlockPos playerPos = mc.player.getBlockPos();

        if (playerPos.getX() == currentTargetPos.getX() &&
            playerPos.getZ() == currentTargetPos.getZ() &&
            playerPos.getY() == currentTargetPos.getY() + 1) {

            BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();

            // Bật AutoSell khi đã đến tọa độ tiếp theo
            toggleAutoSell(true);

            setNukerState(true);
            currentState = State.MINING;
        }
    }

    private void handleMining() {
        // Kiểm tra cao độ để tắt AutoSell
        if (mc.player.getY() <= autoSellOffY.get()) {
            toggleAutoSell(false);
        }

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
            ChatUtils.error(this, "Không thấy GUI mở. Skip...");
            currentState = State.WAITING_NEXT;
            timer = postTaskDelay.get();
        }
    }

    private void handleWaitingGuiClick() {
        if (mc.currentScreen instanceof GenericContainerScreen screen) {
            mc.interactionManager.clickSlot(screen.getScreenHandler().syncId, guiSlotId.get(), 0, SlotActionType.PICKUP, mc.player);
            ChatUtils.debug(this, "§aĐã Click Slot. Chờ ổn định vị trí... 💤");
            currentState = State.WAITING_NEXT;
            timer = postTaskDelay.get();
        } else {
            currentState = State.WAITING_NEXT;
            timer = postTaskDelay.get();
        }
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (currentState == State.RETURNING && event.screen instanceof GenericContainerScreen) {
            currentState = State.WAITING_GUI_CLICK;
            timer = clickDelay.get();
        }

        if (disableOnDisconnect.get() && event.screen instanceof DisconnectedScreen) {
            if (isActive()) toggle();
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (disableOnLeave.get() && isActive()) toggle();
    }

    private void setNukerState(boolean active) {
        Module nuker = (nukerMode.get() == NukerMode.NukerFP) ? Modules.get().get(NukerFP.class) : Modules.get().get(Nuker.class);
        if (nuker != null && nuker.isActive() != active) nuker.toggle();
    }

    // Helper để bật/tắt AutoSellFP
    private void toggleAutoSell(boolean active) {
        if (!controlAutoSell.get()) return;
        Module autoSell = Modules.get().get(AutoSellFP.class);
        if (autoSell != null && autoSell.isActive() != active) {
            autoSell.toggle();
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
