package maico.addonbuu.mixin.show_tt_pet;

import maico.addonbuu.AddonBuu;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(HandledScreen.class)
public abstract class PetInfoRenderMixin {
    @Shadow protected int x;
    @Shadow protected int y;
    @Shadow protected ScreenHandler handler;

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Ép kiểu 'this' sang Screen để gọi getTitle() an toàn, tránh AbstractMethodError
        Screen screen = (Screen) (Object) this;

        if (AddonBuu.showPetInfo && screen.getTitle().getString().contains("ᴛʀɪệᴜ ʜồɪ ʟɪɴʜ ᴛʜú")) {
            // Tăng xOffset lên 180 để các bảng cách xa nhau, không bị đè
            drawPetInfo(context, 14, -180); // Bảng trái
            drawPetInfo(context, 15, 0);    // Bảng giữa
            drawPetInfo(context, 16, 180);  // Bảng phải
        }
    }

    private void drawPetInfo(DrawContext context, int slotId, int xOffset) {
        if (slotId >= handler.slots.size()) return;
        ItemStack stack = handler.getSlot(slotId).getStack();
        if (stack.isEmpty()) return;

        TextRenderer renderer = MinecraftClient.getInstance().textRenderer;

        // 1. Lấy Tên hiển thị (Tên + Giá linh thú) và Lore thông số
        Text itemName = stack.getName();
        LoreComponent lore = stack.get(DataComponentTypes.LORE);
        if (lore == null) return;
        List<Text> lines = lore.lines();

        // 2. Tính toán kích thước bảng
        int maxWidth = renderer.getWidth(itemName);
        for (Text line : lines) {
            maxWidth = Math.max(maxWidth, renderer.getWidth(line));
        }

        int boardWidth = maxWidth + 12;
        int boardHeight = (lines.size() + 1) * 10 + 10;

        // 3. Tính toán tọa độ vẽ
        int startX = this.x + 88 + xOffset - (boardWidth / 2);
        int startY = this.y - boardHeight - 20;

        // 4. Vẽ nền & Viền Overlay (Màu tím đen chuẩn Tooltip Minecraft)
        // Nền 0xCC100010 cho độ trong suốt vừa phải, không bị đen đặc
        context.fill(startX, startY, startX + boardWidth, startY + boardHeight, 0xCC100010);

        // Vẽ viền border màu tím đặc trưng
        context.fill(startX - 1, startY - 1, startX + boardWidth + 1, startY, 0xFF2D054B); // Trên
        context.fill(startX - 1, startY + boardHeight, startX + boardWidth + 1, startY + boardHeight + 1, 0xFF2D054B); // Dưới
        context.fill(startX - 1, startY, startX, startY + boardHeight, 0xFF2D054B); // Trái
        context.fill(startX + boardWidth, startY, startX + boardWidth + 1, startY + boardHeight, 0xFF2D054B); // Phải

        // 5. Vẽ Tên và Giá Pet (Dòng đầu tiên)
        int currentY = startY + 6;
        context.drawText(renderer, itemName, startX + 6, currentY, 0xFFFFFFFF, true);

        // 6. Vẽ các dòng Lore (Sát thương, Bạo kích, v.v.)
        currentY += 12; // Cách tên một khoảng nhỏ cho đẹp
        for (Text line : lines) {
            context.drawText(renderer, line, startX + 6, currentY, 0xFFFFFFFF, true);
            currentY += 10;
        }
    }
}
