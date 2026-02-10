package maico.addonbuu.modules.autofish;

import maico.addonbuu.AddonBuu;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;

public class AutoFish extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgMcmmo = settings.createGroup("mcMMO Settings");

    private final Setting<Double> validRange = sgGeneral.add(new DoubleSetting.Builder().name("vung-nhan-dien").defaultValue(1.5).min(0.1).build());
    private final Setting<Integer> castDelay = sgGeneral.add(new IntSetting.Builder().name("delay-quang").defaultValue(15).min(0).build());

    private final AutoFishRodSelector rodSelector = new AutoFishRodSelector(sgGeneral);
    private final FishingSpotManager spotManager = new FishingSpotManager(sgMcmmo);

    private int castTimer = 0;

    public AutoFish() {
        super(AddonBuu.ADDONBUU, "AutoFish-Wurst", "Logic câu cá từ Wurst Client 🎣");
    }

    @Override
    public void onActivate() {
        castTimer = 0;
        spotManager.reset();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.interactionManager == null) return;

        if (castTimer > 0) {
            castTimer--;
            return;
        }

        // --- FIX LỖI 1.21.4: Sử dụng fishHook thay vì fishObject ---
        FishingBobberEntity bobber = mc.player.fishHook;

        if (bobber == null) {
            if (!rodSelector.update()) return;
            if (spotManager.onCast()) {
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                castTimer = castDelay.get();
            }
        }
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        if (event.packet instanceof PlaySoundS2CPacket packet && mc.player != null) {
            if (packet.getSound().value().equals(SoundEvents.ENTITY_FISHING_BOBBER_SPLASH)) {
                // --- FIX LỖI 1.21.4: Sử dụng fishHook ---
                FishingBobberEntity bobber = mc.player.fishHook;
                if (bobber != null) {
                    double dist = bobber.getPos().distanceTo(new net.minecraft.util.math.Vec3d(packet.getX(), packet.getY(), packet.getZ()));
                    if (dist <= validRange.get()) {
                        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                        spotManager.onBite(bobber);
                        castTimer = castDelay.get();
                    }
                }
            }
        }
    }
}
