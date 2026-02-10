package maico.addonbuu.modules.autofish;

import maico.addonbuu.utils.ChatUtils;
import meteordevelopment.meteorclient.settings.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FishingSpotManager {
    private final MinecraftClient mc = MinecraftClient.getInstance();
    public final Setting<Boolean> mcmmoMode;
    public final Setting<Integer> mcmmoLimit;
    public final Setting<Integer> mcmmoRange;

    private final List<FishingSpot> fishingSpots = new ArrayList<>();
    private FishingSpot lastSpot, nextSpot;
    private PositionAndRotation castPosRot;
    private int fishCaughtAtLastSpot;

    public FishingSpotManager(SettingGroup group) {
        mcmmoMode = group.add(new BoolSetting.Builder().name("mcMMO-mode").description("Vòng lặp ổ câu để lách luật mcMMO.").defaultValue(false).build());
        mcmmoRange = group.add(new IntSetting.Builder().name("mcMMO-range").description("Khoảng cách tối thiểu giữa 2 ổ câu.").defaultValue(3).min(1).visible(mcmmoMode::get).build());
        mcmmoLimit = group.add(new IntSetting.Builder().name("mcMMO-limit").description("Số cá tối đa tại 1 ổ trước khi đổi chỗ.").defaultValue(10).min(2).visible(mcmmoMode::get).build());
    }

    public boolean onCast() {
        if (mc.player == null) return false;
        castPosRot = new PositionAndRotation(mc.player);
        if (!mcmmoMode.get()) return true;

        if (lastSpot == null) {
            ChatUtils.info("AutoFish", "§eĐang ghi lại ổ câu đầu tiên...");
            return true;
        }

        if (nextSpot == null && (nextSpot = chooseNextSpot()) == null) {
            ChatUtils.info("AutoFish", "§cCần thêm 1 ổ câu khác! Hãy quay máy hoặc di chuyển rồi quăng cần.");
            return false;
        }

        if (fishCaughtAtLastSpot >= mcmmoLimit.get() - 1) {
            moveToNextSpot();
            return false;
        }
        return true;
    }

    private void moveToNextSpot() {
        if (nextSpot == null || mc.player == null) return;
        PositionAndRotation next = nextSpot.input();

        // Logic di chuyển đơn giản bằng cách set tọa độ và góc nhìn
        if (mc.player.getPos().distanceTo(next.pos()) > 0.2) {
            mc.player.setPosition(next.pos().x, next.pos().y, next.pos().z);
        }
        mc.player.setYaw(next.yaw());
        mc.player.setPitch(next.pitch());

        lastSpot = nextSpot;
        nextSpot = null;
        fishCaughtAtLastSpot = 0;
        ChatUtils.info("AutoFish", "§aĐã chuyển ổ câu! 🚀");
    }

    // Trong file FishingSpotManager.java, tìm đến hàm onBite và sửa dòng này:
    // Tìm đến hàm onBite trong FishingSpotManager.java và sửa đoạn này:
    public void onBite(FishingBobberEntity bobber) {
        boolean sameInput = lastSpot != null && lastSpot.input().isNearlyIdenticalTo(castPosRot);

        if (sameInput) {
            fishCaughtAtLastSpot++;
        } else {
            // --- FIX LỖI 1.21.4: Sử dụng isInOpenWater() thay vì isOpenWaterFishing() ---
            lastSpot = new FishingSpot(castPosRot, bobber.getPos(), bobber.isInOpenWater());
            fishingSpots.add(lastSpot);
            fishCaughtAtLastSpot = 1;
        }
    }

    private FishingSpot chooseNextSpot() {
        return fishingSpots.stream()
            .filter(spot -> spot != lastSpot)
            .filter(spot -> spot.bobberPos().distanceTo(lastSpot.bobberPos()) >= mcmmoRange.get())
            .min(Comparator.comparingDouble(spot -> spot.input().pos().distanceTo(lastSpot.input().pos())))
            .orElse(null);
    }

    public void reset() {
        fishingSpots.clear();
        lastSpot = nextSpot = null;
        fishCaughtAtLastSpot = 0;
    }
}
