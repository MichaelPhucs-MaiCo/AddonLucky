package maico.addonbuu.mixin.show_tt_pet;

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
public abstract class ShowPetInfoButtonMixin extends net.minecraft.client.gui.screen.Screen {
    @Shadow protected int x;
    @Shadow protected int y;
    @Unique private static final Identifier EFFECT_BG = Identifier.ofVanilla("container/inventory/effect_background_small");

    protected ShowPetInfoButtonMixin(Text title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        // Kiểm tra nếu là đúng GUI Triệu Hồi Linh Thú mới hiện nút
        if (!this.title.getString().contains("ᴛʀɪệᴜ ʜồɪ ʟɪɴʜ ᴛʜú")) return;

        // Đặt nút Show dưới nút Idx (cách thêm 20 pixel chiều dọc)
        ButtonWidget showBtn = new ButtonWidget(this.x - 32, this.y + 26, 30, 18, Text.of(""),
            button -> AddonBuu.showPetInfo = !AddonBuu.showPetInfo,
            (supplier) -> (MutableText) supplier.get()) {
            @Override
            public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
                context.drawGuiTexture(RenderLayer::getGuiTextured, EFFECT_BG, this.getX(), this.getY(), 30, 18, 0xFFFFFFFF);
                String text = (AddonBuu.showPetInfo ? "§a" : "§c") + "§lShow";
                int textWidth = client.textRenderer.getWidth(text);
                context.drawText(client.textRenderer, text, this.getX() + (30 - textWidth) / 2, this.getY() + 5, 0xFFFFFFFF, false);
                if (this.isHovered()) context.fill(this.getX(), this.getY(), this.getX() + 30, this.getY() + 18, 0x40FFFFFF);
            }
        };
        this.addDrawableChild(showBtn);
    }
}
