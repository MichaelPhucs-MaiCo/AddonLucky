package maico.addonbuu.modules.autofish;

import maico.addonbuu.AddonBuu;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.util.List;

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
        super(AddonBuu.ADDONBUU, "AutoFish-Hold", "Tự động câu cá tối ưu 'tận xương' cho Mai Cồ 🎣");
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

        // Nếu không có phao -> Tìm cần câu và quăng
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
        if (bobber == null) return;

        // 1. Nhận diện qua TextDisplay (EntityTrackerUpdate) - TỐI ƯU TRIỆT ĐỂ
        if (event.packet instanceof EntityTrackerUpdateS2CPacket packet) {
            Entity entity = mc.world.getEntityById(packet.id());
            if (entity == null) return;

            // Dùng bình phương khoảng cách để tránh tính Căn Bậc Hai (CPU thích điều này)
            double distSq = entity.getPos().squaredDistanceTo(bobber.getPos());
            double limitSq = textRange.get() * textRange.get();

            if (distSq <= limitSq) {
                List<DataTracker.SerializedEntry<?>> trackedValues = packet.trackedValues();
                if (trackedValues == null) return;

                for (DataTracker.SerializedEntry<?> entry : trackedValues) {
                    // Chỉ kiểm tra nếu entry chứa dữ liệu dạng String/Component
                    Object value = entry.value();
                    if (value == null) continue;

                    String text = value.toString().toLowerCase();

                    if (text.contains("ngươi đã câu được cá")) {
                        holdRightClick();
                        return; // Thoát ngay khi tìm thấy
                    } else if (text.contains("làm tốt lắm")) {
                        releaseRightClick();
                        spotManager.onBite(bobber);
                        castTimer = castDelay.get();
                        return;
                    }
                }
            }
        }

        // 2. Dự phòng (Sound Splash) - Tối ưu bằng squaredDistance
        if (event.packet instanceof PlaySoundS2CPacket packet) {
            if (packet.getSound().value().equals(SoundEvents.ENTITY_FISHING_BOBBER_SPLASH)) {
                Vec3d soundPos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
                if (bobber.getPos().squaredDistanceTo(soundPos) <= 2.25 && !isHolding) { // 1.5 * 1.5 = 2.25
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
