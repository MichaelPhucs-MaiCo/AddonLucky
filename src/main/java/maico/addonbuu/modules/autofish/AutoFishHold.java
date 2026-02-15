package maico.addonbuu.modules.autofish;

import maico.addonbuu.AddonBuu;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;

public class AutoFishHold extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgMcmmo = settings.createGroup("mcMMO Settings");

    private final Setting<Double> textRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("khoang-cach-chu")
        .description("Khoảng cách tối đa giữa TextDisplay và phao câu.")
        .defaultValue(0.8)
        .min(0.1)
        .build()
    );

    private final Setting<Integer> castDelay = sgGeneral.add(new IntSetting.Builder()
        .name("delay-quang")
        .defaultValue(20)
        .min(0)
        .build()
    );

    private final AutoFishRodSelector rodSelector = new AutoFishRodSelector(sgGeneral);
    private final FishingSpotManager spotManager = new FishingSpotManager(sgMcmmo);

    private int castTimer = 0;
    private boolean isHolding = false;

    public AutoFishHold() {
        super(AddonBuu.ADDONBUU, "AutoFish-Hold", "Tự động câu cá phong cách 'gồng' chuột cho Mai Cồ 🎣");
    }

    @Override
    public void onActivate() {
        castTimer = 0;
        isHolding = false;
        releaseRightClick();
        spotManager.reset();
    }

    @Override
    public void onDeactivate() {
        releaseRightClick();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.interactionManager == null) return;

        if (castTimer > 0) {
            castTimer--;
            return;
        }

        FishingBobberEntity bobber = mc.player.fishHook;

        // Nếu không có phao -> Tìm cần câu tốt nhất và quăng
        if (bobber == null) {
            if (isHolding) releaseRightClick();

            if (!rodSelector.update()) return;
            if (spotManager.onCast()) {
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                castTimer = castDelay.get();
            }
        }
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        if (mc.player == null || mc.world == null) return;
        FishingBobberEntity bobber = mc.player.fishHook;

        // 1. Nhận diện qua TextDisplay (EntityTrackerUpdate)
        if (event.packet instanceof EntityTrackerUpdateS2CPacket packet && bobber != null) {
            Entity entity = mc.world.getEntityById(packet.id());
            if (entity != null) {
                // Kiểm tra xem TextDisplay có nằm ngay sát phao câu của mình không
                double dist = entity.getPos().distanceTo(bobber.getPos());
                if (dist <= textRange.get()) {
                    String rawData = packet.toString().toLowerCase();

                    // Nếu hiện chữ "câu được cá" -> Giữ chuột
                    if (rawData.contains("ngươi đã câu được cá")) {
                        holdRightClick();
                    }
                    // Nếu hiện chữ "làm tốt lắm" -> Thả chuột
                    else if (rawData.contains("làm tốt lắm")) {
                        releaseRightClick();
                        spotManager.onBite(bobber);
                        castTimer = castDelay.get();
                    }
                }
            }
        }

        // 2. Dự phòng (Fallback): Nếu nghe tiếng splash mà chưa giữ chuột thì click/giữ luôn
        if (event.packet instanceof PlaySoundS2CPacket packet && bobber != null) {
            if (packet.getSound().value().equals(SoundEvents.ENTITY_FISHING_BOBBER_SPLASH)) {
                double dist = bobber.getPos().distanceTo(new net.minecraft.util.math.Vec3d(packet.getX(), packet.getY(), packet.getZ()));
                if (dist <= 1.5 && !isHolding) {
                    holdRightClick();
                }
            }
        }
    }

    private void holdRightClick() {
        if (!isHolding) {
            mc.options.useKey.setPressed(true);
            isHolding = true;
        }
    }

    private void releaseRightClick() {
        if (isHolding) {
            mc.options.useKey.setPressed(false);
            isHolding = false;
        }
    }
}
