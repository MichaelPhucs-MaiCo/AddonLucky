package maico.addonbuu.mixin.quick_access_server;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiplayerServerListWidget.ServerEntry.class)
public abstract class ServerEntryMixin {
    @Shadow @Final private ServerInfo server;
    @Shadow @Final private MultiplayerScreen screen;
    @Shadow @Final private MinecraftClient client;

    // Lấy chính xác texture JOIN_HIGHLIGHTED từ source cậu gửi
    @Unique private static final Identifier PLAY_ICON = Identifier.ofVanilla("server_list/join_highlighted");

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        // Chỉ hiện khi di chuột vào đúng vùng Avatar (32x32)
        if (hovered && mouseX >= x && mouseX <= x + 32) {
            context.fill(x, y, x + 32, y + 32, 0xA0000000);
            context.drawGuiTexture(RenderLayer::getGuiTextured, PLAY_ICON, x, y, 32, 32);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        // Nếu click vào vùng Avatar thì vào thẳng server (Quick Play)
        // Lưu ý: Cậu cần check tọa độ X tương đối trong danh sách
        if (button == 0 && mouseX >= 0 && mouseX <= 40) { // Vùng icon bên trái
            ConnectScreen.connect(this.screen, this.client, ServerAddress.parse(this.server.address), this.server, false, null);
            cir.setReturnValue(true);
        }
    }
}
