package maico.addonbuu.modules.FairyPrion;

import maico.addonbuu.AddonBuu;
import maico.addonbuu.utils.ChatUtils;
import meteordevelopment.meteorclient.events.entity.player.BlockBreakingCooldownEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent; // Thêm import mới
import meteordevelopment.meteorclient.events.game.OpenScreenEvent; // Thêm import mới
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.DisconnectedScreen; // Thêm import mới
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NukerFP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgWhitelist = settings.createGroup("Whitelist");
    private final SettingGroup sgRender = settings.createGroup("Render");
    // Group riêng cho an toàn nè Mai Cồ 🛡️
    private final SettingGroup sgSafety = settings.createGroup("Safety");

    // --- General Settings ---
    private final Setting<Shape> shape = sgGeneral.add(new EnumSetting.Builder<Shape>()
        .name("shape")
        .description("Hình dạng vùng đào.")
        .defaultValue(Shape.Sphere)
        .build()
    );

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("Chế độ đào. Smart sẽ đào xung quanh trước khi đào block dưới chân.")
        .defaultValue(Mode.Smart)
        .build()
    );

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("Tầm xa đào block.")
        .defaultValue(4)
        .min(0)
        .visible(() -> shape.get() != Shape.Cube)
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay giữa các lần đào (tick).")
        .defaultValue(0)
        .build()
    );

    private final Setting<Integer> maxBlocksPerTick = sgGeneral.add(new IntSetting.Builder()
        .name("max-blocks-per-tick")
        .description("Số block tối đa phá trong 1 tick.")
        .defaultValue(1)
        .min(1)
        .sliderRange(1, 10)
        .build()
    );

    private final Setting<SortMode> sortMode = sgGeneral.add(new EnumSetting.Builder<SortMode>()
        .name("sort-mode")
        .description("Thứ tự ưu tiên đào.")
        .defaultValue(SortMode.Closest)
        .build()
    );

    private final Setting<Boolean> packetMine = sgGeneral.add(new BoolSetting.Builder()
        .name("packet-mine")
        .description("Đào bằng gói tin (siêu nhanh).")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Tự động quay đầu về block đang đào.")
        .defaultValue(true)
        .build()
    );

    // --- Safety Settings (Group mới) ---
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

    // --- Whitelist/Blacklist ---
    private final Setting<ListMode> listMode = sgWhitelist.add(new EnumSetting.Builder<ListMode>()
        .name("list-mode")
        .description("Chế độ lọc block.")
        .defaultValue(ListMode.Blacklist)
        .build()
    );

    private final Setting<List<Block>> blacklist = sgWhitelist.add(new BlockListSetting.Builder()
        .name("blacklist")
        .description("Block không muốn đào.")
        .visible(() -> listMode.get() == ListMode.Blacklist)
        .build()
    );

    private final Setting<List<Block>> whitelist = sgWhitelist.add(new BlockListSetting.Builder()
        .name("whitelist")
        .description("Chỉ đào những block này.")
        .visible(() -> listMode.get() == ListMode.Whitelist)
        .build()
    );

    // --- Rendering ---
    private final Setting<Boolean> enableRenderBreaking = sgRender.add(new BoolSetting.Builder()
        .name("render-breaking")
        .description("Hiển thị block đang bị phá.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("Màu mặt của block.")
        .defaultValue(new SettingColor(255, 0, 0, 80))
        .visible(enableRenderBreaking::get)
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("Màu viền của block.")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .visible(enableRenderBreaking::get)
        .build()
    );

    private final List<BlockPos> blocks = new ArrayList<>();
    private final BlockPos.Mutable lastBlockPos = new BlockPos.Mutable();
    private boolean firstBlock;
    private int timer;
    private int noBlockTimer;

    public NukerFP() {
        super(AddonBuu.FAIRY_PRISON, "nuker-fp", "Nuker tối ưu cho Prison với Smart Mode.");
    }

    @Override
    public void onActivate() {
        firstBlock = true;
        timer = 0;
        noBlockTimer = 0;
        ChatUtils.info(this, "§aNukerFP đã sẵn sàng quẩy khu mine! 🚀");
    }

    @EventHandler
    private void onTickPre(TickEvent.Pre event) {
        if (timer > 0) {
            timer--;
            return;
        }

        double pX = mc.player.getX();
        double pY = mc.player.getY();
        double pZ = mc.player.getZ();
        double rangeSq = Math.pow(range.get(), 2);

        BlockPos feetPos = mc.player.getBlockPos().down();

        BlockIterator.register((int) Math.ceil(range.get() + 1), (int) Math.ceil(range.get() + 1), (blockPos, blockState) -> {
            if (shape.get() == Shape.Sphere) {
                if (Utils.squaredDistance(pX, pY, pZ, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5) > rangeSq) return;
            } else {
                if (chebyshevDist(mc.player.getBlockPos().getX(), mc.player.getBlockPos().getY(), mc.player.getBlockPos().getZ(), blockPos.getX(), blockPos.getY(), blockPos.getZ()) >= range.get()) return;
            }

            if (!BlockUtils.canBreak(blockPos, blockState)) return;
            if (mode.get() == Mode.Smart && blockPos.equals(feetPos)) return;
            if (mode.get() == Mode.Flatten && blockPos.getY() < Math.floor(mc.player.getY())) return;

            if (listMode.get() == ListMode.Whitelist && !whitelist.get().contains(blockState.getBlock())) return;
            if (listMode.get() == ListMode.Blacklist && blacklist.get().contains(blockState.getBlock())) return;

            blocks.add(blockPos.toImmutable());
        });

        BlockIterator.after(() -> {
            if (blocks.isEmpty() && mode.get() == Mode.Smart) {
                if (BlockUtils.canBreak(feetPos)) {
                    Block feetBlock = mc.world.getBlockState(feetPos).getBlock();
                    boolean canBreakFeet = true;
                    if (listMode.get() == ListMode.Whitelist && !whitelist.get().contains(feetBlock)) canBreakFeet = false;
                    if (listMode.get() == ListMode.Blacklist && blacklist.get().contains(feetBlock)) canBreakFeet = false;

                    if (canBreakFeet) blocks.add(feetPos.toImmutable());
                }
            }

            if (sortMode.get() == SortMode.TopDown)
                blocks.sort(Comparator.comparingDouble(value -> -value.getY()));
            else if (sortMode.get() != SortMode.None)
                blocks.sort(Comparator.comparingDouble(value -> Utils.squaredDistance(pX, pY, pZ, value.getX() + 0.5, value.getY() + 0.5, value.getZ() + 0.5) * (sortMode.get() == SortMode.Closest ? 1 : -1)));

            if (blocks.isEmpty()) {
                if (noBlockTimer++ >= delay.get()) firstBlock = true;
                return;
            } else {
                noBlockTimer = 0;
            }

            if (!firstBlock && !lastBlockPos.equals(blocks.getFirst())) {
                timer = delay.get();
                firstBlock = false;
                lastBlockPos.set(blocks.getFirst());
                if (timer > 0) return;
            }

            int count = 0;
            for (BlockPos block : blocks) {
                if (count >= maxBlocksPerTick.get()) break;

                boolean canInstaMine = BlockUtils.canInstaBreak(block);

                if (rotate.get()) Rotations.rotate(Rotations.getYaw(block), Rotations.getPitch(block), () -> breakBlock(block));
                else breakBlock(block);

                if (enableRenderBreaking.get()) RenderUtils.renderTickingBlock(block, sideColor.get(), lineColor.get(), ShapeMode.Both, 0, 8, true, false);
                lastBlockPos.set(block);

                count++;
                if (!canInstaMine && !packetMine.get()) break;
            }

            firstBlock = false;
            blocks.clear();
        });
    }

    private void breakBlock(BlockPos blockPos) {
        if (packetMine.get()) {
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, BlockUtils.getDirection(blockPos)));
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, BlockUtils.getDirection(blockPos)));
        } else {
            BlockUtils.breakBlock(blockPos, true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onBlockBreakingCooldown(BlockBreakingCooldownEvent event) {
        event.cooldown = 0;
    }

    // --- Safety Logic Handlers 🛡️ ---
    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
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

    public enum ListMode { Whitelist, Blacklist }
    public enum Mode { All, Flatten, Smart }
    public enum SortMode { None, Closest, Furthest, TopDown }
    public enum Shape { Cube, Sphere }

    public static int chebyshevDist(int x1, int y1, int z1, int x2, int y2, int z2) {
        return Math.max(Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1)), Math.abs(z2 - z1));
    }
}
