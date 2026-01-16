package maico.addonbuu.modules.treo_pho_ban;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalBlock;
import maico.addonbuu.AddonBuu;
import maico.addonbuu.utils.ChatUtils;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class TreoPhoBan extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgGui = settings.createGroup("Cau hinh GUI");
    private final SettingGroup sgFarm = settings.createGroup("Cau hinh Farm");

    // --- ENUMS ---
    public enum Area { Huyen_Anh_Bi_Canh_Slot9, Thi_Luyen_Dao_Trang_Slot18, Thien_Uyen_Cam_Dia_Slot27, Thanh_Vuc_Tu_Linh_Slot36, Custom }
    public enum HuyenAnhDungeon { Sa_Mac_Hoa_Linh_Slot13, Bang_Cung_Tuyet_Dia_Slot14, Co_Mot_Huyet_Toc_Slot15, Hai_Vuc_Tham_Uyen_Slot16, Than_Moc_Lam_Slot21, Di_Gioi_Khong_Gian_Slot22, Chien_Truong_Thuong_Co_Slot24, Dong_Phu_Van_Quy_Slot25, Dam_Lay_An_Mon_Slot26, Hoang_Thanh_Tu_Chan_Slot29, Mo_Linh_Thach_Do_Hoang_Slot30, Phe_Tich_Than_Toc_Slot31, Song_Vong_Xuyen_Slot32, Phong_Bao_Hon_Nguyen_Slot33, Dinh_Con_Lon_Gia_Slot34, Custom }
    public enum ThiLuyenDungeon { Dao_Trang_Linh_Thach_Slot11, Dao_Trang_Khoang_Thach_Slot12, Dao_Trang_Vien_Ngoc_Slot20, Custom }
    public enum ThienUyenDungeon { Giang_Sinh_1_Slot11, Giang_Sinh_2_Slot12, Giang_Sinh_3_Slot13, Custom }
    public enum FarmMode { Baritone, WASD }
    public enum AutoCutMode { Luon_Gui, Gui_1_Lan }

    // --- SETTINGS ---
    private final Setting<Boolean> autoCut = sgGeneral.add(new BoolSetting.Builder()
        .name("tu-cat-do")
        .description("Tu dong gui lenh cat do vao kho.")
        .defaultValue(true)
        .build()
    );

    private final Setting<AutoCutMode> autoCutMode = sgGeneral.add(new EnumSetting.Builder<AutoCutMode>()
        .name("che-do-cat-do")
        .description("Chon cach thuc gui lenh /tucatdo.")
        .defaultValue(AutoCutMode.Luon_Gui)
        .visible(autoCut::get)
        .build()
    );

    private final Setting<Area> area = sgGui.add(new EnumSetting.Builder<Area>()
        .name("khu-vuc")
        .description("Chon khu vuc pho ban muon vao.")
        .defaultValue(Area.Huyen_Anh_Bi_Canh_Slot9)
        .build()
    );

    private final Setting<Integer> customAreaSlot = sgGui.add(new IntSetting.Builder()
        .name("custom-area-slot")
        .description("Nhap slot id cho Khu Vuc neu chon che do Custom.")
        .defaultValue(9)
        .min(0)
        .visible(() -> area.get() == Area.Custom)
        .build()
    );

    private final Setting<HuyenAnhDungeon> huyenAnh = sgGui.add(new EnumSetting.Builder<HuyenAnhDungeon>()
        .name("pho-ban-huyen-anh")
        .description("Chon pho ban trong Huyen Anh Bi Canh.")
        .defaultValue(HuyenAnhDungeon.Bang_Cung_Tuyet_Dia_Slot14)
        .visible(() -> area.get() == Area.Huyen_Anh_Bi_Canh_Slot9)
        .build()
    );

    private final Setting<ThiLuyenDungeon> thiLuyen = sgGui.add(new EnumSetting.Builder<ThiLuyenDungeon>()
        .name("pho-ban-thi-luyen")
        .description("Chon pho ban trong Thi Luyen Dao Trang.")
        .defaultValue(ThiLuyenDungeon.Dao_Trang_Linh_Thach_Slot11)
        .visible(() -> area.get() == Area.Thi_Luyen_Dao_Trang_Slot18)
        .build()
    );

    private final Setting<ThienUyenDungeon> thienUyen = sgGui.add(new EnumSetting.Builder<ThienUyenDungeon>()
        .name("pho-ban-thien-uyen")
        .description("Chon pho ban trong Thien Uyen Cam Dia.")
        .defaultValue(ThienUyenDungeon.Giang_Sinh_1_Slot11)
        .visible(() -> area.get() == Area.Thien_Uyen_Cam_Dia_Slot27)
        .build()
    );

    private final Setting<Integer> customDungeonSlot = sgGui.add(new IntSetting.Builder()
        .name("custom-slot")
        .description("Nhap slot id neu chon che do Custom.")
        .defaultValue(14)
        .min(0)
        .visible(this::isCustomVisible)
        .build()
    );

    private final Setting<Integer> globalDelay = sgGeneral.add(new IntSetting.Builder()
        .name("delay-thao-tac")
        .description("Thoi gian cho giua cac buoc thao tac GUI.")
        .defaultValue(30)
        .min(10)
        .build()
    );

    private final Setting<FarmMode> farmMode = sgFarm.add(new EnumSetting.Builder<FarmMode>()
        .name("farm-mode")
        .description("Chon Baritone hoac WASD.")
        .defaultValue(FarmMode.Baritone)
        .build()
    );

    private final Setting<String> targetCoords = sgFarm.add(new StringSetting.Builder()
        .name("toa-do-farm")
        .description("Toa do dich den cho Baritone (X Y Z).")
        .defaultValue(" ")
        .visible(() -> farmMode.get() == FarmMode.Baritone)
        .build()
    );

    private final Setting<List<String>> wasdCommands = sgFarm.add(new StringListSetting.Builder()
        .name("wasd-commands")
        .description("Danh sach lenh WASD. VD: up 3s, down 2s, left 1s, right 1s.")
        .defaultValue(new ArrayList<>(List.of("up 3s", "down 2s", "left 1s", "right 4s")))
        .visible(() -> farmMode.get() == FarmMode.WASD)
        .build()
    );

    // --- LOGIC BIEN ---
    private enum State {
        IDLE, STARTING, WARPING, GOING_TO_PORTAL,
        SELECTING_AREA, SELECTING_DUNGEON,
        WAITING_FOR_DIMENSION, FARMING
    }
    private State currentState = State.IDLE;
    private int timer = 0;
    private boolean isInDungeon = false;
    private boolean autoCutDoneOnce = false;
    private int wasdIndex = 0;
    private int wasdTickCounter = 0;
    private int wasdPauseTimer = 0;

    private final Vec3d LOBBY_EXIT = new Vec3d(24, 64, -36);
    private final Vec3d SPAWN_LT = new Vec3d(28, 64, -37);

    public TreoPhoBan() { super(AddonBuu.TREOPHOBAN, "treo-pho-ban", "Auto Treo Pho Ban🐾🚀"); }

    @Override
    public void onActivate() {
        resetLogic();
        autoCutDoneOnce = false;
        currentState = State.STARTING;
        ChatUtils.addModMessage("Bắt đầu treo phó bản! ⏳");
    }

    private void resetLogic() {
        timer = 0; isInDungeon = false;
        wasdIndex = 0; wasdTickCounter = 0;
        wasdPauseTimer = 0;
        stopMovement();
    }

    @Override
    public void onDeactivate() { stopMovement(); BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything(); }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;
        String currentDim = mc.world.getRegistryKey().getValue().getPath();

        if (isInDungeon && currentDim.equals("overworld") && mc.player.getPos().distanceTo(LOBBY_EXIT) < 2.0) {
            ChatUtils.addModMessage("Đã rời phó bản. Bắt đầu loop mới...");
            resetLogic(); currentState = State.STARTING; return;
        }

        switch (currentState) {
            case STARTING -> {
                if (autoCut.get()) {
                    if (autoCutMode.get() == AutoCutMode.Luon_Gui && timer == 0) ChatUtils.sendPlayerMsg("/tucatdo");
                    else if (autoCutMode.get() == AutoCutMode.Gui_1_Lan && !autoCutDoneOnce && timer == 0) {
                        ChatUtils.sendPlayerMsg("/tucatdo");
                        autoCutDoneOnce = true;
                    }
                }
                if (timer >= globalDelay.get()) { ChatUtils.sendPlayerMsg("/warp linhthuvien"); currentState = State.WARPING; timer = 0; }
                timer++;
            }
            case WARPING -> {
                if (mc.player.getPos().distanceTo(SPAWN_LT) < 2.0 && timer >= 20) {
                    currentState = State.GOING_TO_PORTAL;
                    ChatUtils.addModMessage("Tới spawn Linh Thú Viên. Chạy vô cổng..");
                }
                timer++;
            }
            case GOING_TO_PORTAL -> {
                if (mc.options.forwardKey != null) mc.options.forwardKey.setPressed(true);
                if (mc.player.getY() >= 64.0 && mc.player.isOnGround()) mc.player.jump();
            }
            case SELECTING_AREA -> {
                if (mc.currentScreen instanceof GenericContainerScreen && timer >= globalDelay.get()) {
                    ChatUtils.debug("Đang chọn Khu Vực: " + area.get().name());
                    clickSlot(getAreaSlotId());
                    currentState = State.SELECTING_DUNGEON;
                    timer = 0;
                }
                timer++;
            }
            case SELECTING_DUNGEON -> {
                if (mc.currentScreen instanceof GenericContainerScreen && timer >= globalDelay.get()) {
                    ChatUtils.debug("Đang chọn Phó Bản slot: " + getDungeonSlotId());
                    clickSlot(getDungeonSlotId());
                    currentState = State.WAITING_FOR_DIMENSION;
                    timer = 0;
                }
                timer++;
            }
            case WAITING_FOR_DIMENSION -> {
                if (currentDim.contains("dungeon")) {
                    isInDungeon = true;
                    currentState = State.FARMING;
                    ChatUtils.addModMessage("Đã vào phó bản! Bắt đầu di chuyển...");
                }
            }
            case FARMING -> {
                if (farmMode.get() == FarmMode.Baritone) handleBaritoneMode();
                else handleWasdMode();
            }
        }
    }

    private void handleBaritoneMode() {
        BlockPos target = parseBlockPos(targetCoords.get());
        if (target != null && !BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing()) {
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(target));
        }
    }

    private void handleWasdMode() {
        List<String> commands = wasdCommands.get();
        if (wasdIndex >= commands.size()) {
            stopMovement();
            currentState = State.IDLE;
            ChatUtils.addModMessage("Đã hoàn thành Mode WASD!.");
            return;
        }

        if (wasdPauseTimer > 0) {
            stopMovement();
            wasdPauseTimer--;
            return;
        }

        String currentCmd = commands.get(wasdIndex).toLowerCase();
        try {
            String[] split = currentCmd.split(" ");
            String action = split[0];
            int durationTicks = (int) (Double.parseDouble(split[1].replace("s", "")) * 20);

            if (wasdTickCounter < durationTicks) {
                mc.options.forwardKey.setPressed(action.equals("up"));
                mc.options.backKey.setPressed(action.equals("down"));
                mc.options.leftKey.setPressed(action.equals("left"));
                mc.options.rightKey.setPressed(action.equals("right"));
                wasdTickCounter++;
            } else {
                stopMovement();
                wasdTickCounter = 0;
                wasdIndex++;
                wasdPauseTimer = 5;
                if (wasdIndex < commands.size()) ChatUtils.debug("Chuyển sang: " + commands.get(wasdIndex));
            }
        } catch (Exception e) { stopMovement(); wasdIndex++; }
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (event.screen instanceof GenericContainerScreen && currentState == State.GOING_TO_PORTAL) {
            stopMovement();
            currentState = State.SELECTING_AREA;
            timer = 0;
            ChatUtils.debug("Đã mở GUI.");
        }
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        String msg = event.getMessage().getString().toLowerCase();
        if (msg.contains("tự động cất vật phẩm vào kho đã tắt") || msg.contains("đã tắt tính năng tự gửi đồ")) {
            ChatUtils.sendPlayerMsg("/tucatdo");
        }
    }

    // --- HELPER METHODS ---
    private int getAreaSlotId() {
        return switch (area.get()) {
            case Huyen_Anh_Bi_Canh_Slot9 -> 9;
            case Thi_Luyen_Dao_Trang_Slot18 -> 18;
            case Thien_Uyen_Cam_Dia_Slot27 -> 27;
            case Thanh_Vuc_Tu_Linh_Slot36 -> 36;
            case Custom -> customAreaSlot.get();
        };
    }

    private int getDungeonSlotId() {
        return switch (area.get()) {
            case Huyen_Anh_Bi_Canh_Slot9 -> switch (huyenAnh.get()) {
                case Sa_Mac_Hoa_Linh_Slot13 -> 13; case Bang_Cung_Tuyet_Dia_Slot14 -> 14; case Co_Mot_Huyet_Toc_Slot15 -> 15;
                case Hai_Vuc_Tham_Uyen_Slot16 -> 16; case Than_Moc_Lam_Slot21 -> 21; case Di_Gioi_Khong_Gian_Slot22 -> 22;
                case Chien_Truong_Thuong_Co_Slot24 -> 24; case Dong_Phu_Van_Quy_Slot25 -> 25; case Dam_Lay_An_Mon_Slot26 -> 26;
                case Hoang_Thanh_Tu_Chan_Slot29 -> 29; case Mo_Linh_Thach_Do_Hoang_Slot30 -> 30; case Phe_Tich_Than_Toc_Slot31 -> 31;
                case Song_Vong_Xuyen_Slot32 -> 32; case Phong_Bao_Hon_Nguyen_Slot33 -> 33; case Dinh_Con_Lon_Gia_Slot34 -> 34;
                case Custom -> customDungeonSlot.get();
            };
            case Thi_Luyen_Dao_Trang_Slot18 -> switch (thiLuyen.get()) {
                case Dao_Trang_Linh_Thach_Slot11 -> 11; case Dao_Trang_Khoang_Thach_Slot12 -> 12; case Dao_Trang_Vien_Ngoc_Slot20 -> 20;
                case Custom -> customDungeonSlot.get();
            };
            case Thien_Uyen_Cam_Dia_Slot27 -> switch (thienUyen.get()) {
                case Giang_Sinh_1_Slot11 -> 11; case Giang_Sinh_2_Slot12 -> 12; case Giang_Sinh_3_Slot13 -> 13;
                case Custom -> customDungeonSlot.get();
            };
            case Thanh_Vuc_Tu_Linh_Slot36, Custom -> customDungeonSlot.get();
        };
    }

    private boolean isCustomVisible() {
        if (area.get() == Area.Huyen_Anh_Bi_Canh_Slot9) return huyenAnh.get() == HuyenAnhDungeon.Custom;
        if (area.get() == Area.Thi_Luyen_Dao_Trang_Slot18) return thiLuyen.get() == ThiLuyenDungeon.Custom;
        if (area.get() == Area.Thien_Uyen_Cam_Dia_Slot27) return thienUyen.get() == ThienUyenDungeon.Custom;
        return area.get() == Area.Thanh_Vuc_Tu_Linh_Slot36 || area.get() == Area.Custom;
    }

    private void clickSlot(int slotId) {
        if (mc.currentScreen instanceof GenericContainerScreen screen)
            mc.interactionManager.clickSlot(screen.getScreenHandler().syncId, slotId, 0, SlotActionType.PICKUP, mc.player);
    }

    private void stopMovement() {
        if (mc.options != null) {
            mc.options.forwardKey.setPressed(false);
            mc.options.backKey.setPressed(false);
            mc.options.leftKey.setPressed(false);
            mc.options.rightKey.setPressed(false);
        }
    }

    private BlockPos parseBlockPos(String s) {
        try {
            String[] p = s.split(" ");
            return new BlockPos(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]));
        } catch (Exception e) { return null; }
    }
}
