package maico.addonbuu.mixin.quick_access_server;

import maico.addonbuu.utils.ChatUtils;
import maico.addonbuu.utils.quick_access_server.LobbyManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Base64;

@Mixin(MultiplayerScreen.class)
public abstract class MultiplayerScreenMixin extends Screen {
    @Shadow private MultiplayerServerListWidget serverListWidget;
    private ButtonWidget addLobbyBtn;
    private ButtonWidget removeLobbyBtn;

    protected MultiplayerScreenMixin(Text title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        LobbyManager.load();

        // Căn chỉnh X = width/2 - 233 tạo khoảng hở 4px chuẩn MC.
        // Y = height - 52 và height - 28 để khít hoàn toàn với nút gốc.
        addLobbyBtn = ButtonWidget.builder(Text.of("§aThêm vào sảnh"), button -> {
            var entry = this.serverListWidget.getSelectedOrNull();
            if (entry instanceof MultiplayerServerListWidget.ServerEntry serverEntry) {
                ServerInfo info = serverEntry.getServer();

                // Lấy Favicon từ ServerInfo và chuyển sang Base64
                String iconBase64 = info.getFavicon() != null ?
                    Base64.getEncoder().encodeToString(info.getFavicon()) : null;

                LobbyManager.addServer(info.name, info.address, iconBase64);
                ChatUtils.addModMessage("§aĐã thêm " + info.name + " vào Quick Access! ⭐");
                updateButtons();
            }
        }).dimensions(this.width / 2 - 233, this.height - 54, 75, 20).build();

        removeLobbyBtn = ButtonWidget.builder(Text.of("§cXóa khỏi sảnh"), button -> {
            var entry = this.serverListWidget.getSelectedOrNull();
            if (entry instanceof MultiplayerServerListWidget.ServerEntry se) {
                LobbyManager.removeServer(se.getServer().address);
                ChatUtils.addModMessage("§eĐã xóa server khỏi sảnh.");
                updateButtons();
            }
        }).dimensions(this.width / 2 - 233, this.height - 30, 75, 20).build();

        this.addDrawableChild(addLobbyBtn);
        this.addDrawableChild(removeLobbyBtn);
        updateButtons();
    }

    @Inject(method = "updateButtonActivationStates", at = @At("TAIL"))
    private void onUpdateButtons(CallbackInfo ci) { updateButtons(); }

    private void updateButtons() {
        if (serverListWidget == null) return;
        var entry = this.serverListWidget.getSelectedOrNull();
        boolean isSelected = entry instanceof MultiplayerServerListWidget.ServerEntry;
        if (addLobbyBtn != null && removeLobbyBtn != null) {
            addLobbyBtn.active = isSelected;
            removeLobbyBtn.active = isSelected;
            if (isSelected) {
                String ip = ((MultiplayerServerListWidget.ServerEntry) entry).getServer().address;
                boolean exists = LobbyManager.isServerInLobby(ip);
                addLobbyBtn.visible = !exists;
                removeLobbyBtn.visible = exists;
            }
        }
    }
}
