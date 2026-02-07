package maico.addonbuu.mixin;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalBlock;
import maico.addonbuu.modules.SaveTarget;
import maico.addonbuu.utils.ChatUtils;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class SaveTargetMixin {
    @Inject(method = "doItemUse", at = @At("HEAD"), cancellable = true)
    private void onDoItemUse(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        SaveTarget module = Modules.get().get(SaveTarget.class);

        // Chỉ xử lý khi module đang bật và đang có mục tiêu
        if (module != null && module.isActive() && SaveTarget.targetPos != null) {
            BlockPos target = SaveTarget.targetPos;
            int playerY = mc.player.getBlockPos().getY(); // Lấy tầng Y của player

            int tx = target.getX();
            int ty = playerY;
            int tz = target.getZ();

            // 1. Format nội dung copy: "goto X Y_player Z"
            String copyString = String.format("goto %d %d %d", tx, ty, tz);

            if (mc.keyboard != null) {
                mc.keyboard.setClipboard(copyString);
                ChatUtils.addModMessage("📋 §a§lĐÃ COPY! §fLệnh: §e" + copyString);
            }

            // 2. Gọi API Baritone để set Goal (Tương đương lệnh #goal)
            try {
                // setGoal chỉ đặt mục tiêu lên bản đồ, không tự động bắt đầu di chuyển
                BaritoneAPI.getProvider().getPrimaryBaritone()
                    .getCustomGoalProcess()
                    .setGoal(new GoalBlock(tx, ty, tz));

                ChatUtils.debug("§6[Baritone] §fĐã đặt Goal tại: §b" + tx + " " + ty + " " + tz + " ✨");
            } catch (Exception e) {
                ChatUtils.error("Không tìm thấy Baritone API!");
            }

            // Chặn hành động chuột phải gốc
            ci.cancel();
        }
    }
}
