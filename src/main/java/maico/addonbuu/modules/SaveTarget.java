package maico.addonbuu.modules;

import maico.addonbuu.AddonBuu;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class SaveTarget extends Module {
    // Biến static để HUD và Mixin có thể truy cập nhanh
    public static BlockPos targetPos = null;
    public static double distance = 0;

    public SaveTarget() {
        super(AddonBuu.ADDONBUU, "save-target", "Lấy tọa độ block và click chuột phải để copy (lấy Y của player) 🎯");
    }

    @Override
    public void onDeactivate() {
        targetPos = null;
        distance = 0;
    }

    // Hàm cập nhật dữ liệu block đang nhìn vào
    public static void updateTarget() {
        var mc = net.minecraft.client.MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        HitResult hit = mc.crosshairTarget;
        if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
            targetPos = ((BlockHitResult) hit).getBlockPos();
            // Tính khoảng cách từ chân player tới tâm (center) của block target
            distance = mc.player.getPos().distanceTo(targetPos.toCenterPos());
        } else {
            targetPos = null;
        }
    }
}
