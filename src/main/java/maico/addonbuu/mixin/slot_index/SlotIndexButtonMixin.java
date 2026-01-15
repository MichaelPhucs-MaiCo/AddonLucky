package maico.addonbuu.mixin.slot_index;

import maico.addonbuu.AddonBuu;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class SlotIndexButtonMixin extends net.minecraft.client.gui.screen.Screen {

    @Shadow protected int x;
    @Shadow protected int y;

    @Unique
    private static final Identifier EFFECT_BG_SMALL = Identifier.ofVanilla("container/inventory/effect_background_small");

    protected SlotIndexButtonMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        // FIX: Chỉnh tọa độ X thành this.x - 32 để nút rộng 30px nằm hoàn toàn bên ngoài lề trái
        ButtonWidget toggleBtn = new ButtonWidget(this.x - 32, this.y + 6, 30, 18, Text.of(""),
            button -> AddonBuu.showSlotIndex = !AddonBuu.showSlotIndex,
            (supplier) -> (MutableText) supplier.get()) {

            @Override
            public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
                // 1. Vẽ khung bo góc xịn xò
                context.drawGuiTexture(RenderLayer::getGuiTextured, EFFECT_BG_SMALL, this.getX(), this.getY(), 30, 18, 0xFFFFFFFF);

                var textRenderer = MinecraftClient.getInstance().textRenderer;

                if (textRenderer != null) {
                    String text = (AddonBuu.showSlotIndex ? "§a" : "§c") + "§lIdx";

                    // --- LOGIC CĂN GIỮA TỰ ĐỘNG ---
                    // Lấy độ rộng thực tế của chuỗi "Idx" (tính cả in đậm)
                    int textWidth = textRenderer.getWidth(text);

                    // Căn giữa ngang: (Chiều rộng nút - Độ rộng chữ) / 2
                    int xOffset = (30 - textWidth) / 2;

                    // Căn giữa dọc: (Chiều cao nút - Chiều cao font) / 2. Font MC cao 9px.
                    // (18 - 9) / 2 = 4.5 -> Tớ sẽ lấy 5 cho nó nhìn cân đối hơn với viền nút
                    int yOffset = 5;

                    context.drawText(textRenderer, text, this.getX() + xOffset, this.getY() + yOffset, 0xFFFFFFFF, false);
                }

                // 2. Hiệu ứng hover
                if (this.isHovered()) {
                    context.fill(this.getX(), this.getY(), this.getX() + 30, this.getY() + 18, 0x40FFFFFF);
                }
            }
        };

        this.addDrawableChild(toggleBtn);
    }
}
