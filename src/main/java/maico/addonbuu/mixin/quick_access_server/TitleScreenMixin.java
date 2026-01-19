package maico.addonbuu.mixin.quick_access_server;

import maico.addonbuu.utils.quick_access_server.LobbyManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.MultiplayerServerListPinger; // Class shipper lấy dữ liệu nè
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends net.minecraft.client.gui.screen.Screen {
    @Unique private static final Identifier JOIN_ICON = Identifier.ofVanilla("server_list/join_highlighted");
    @Unique private static final Map<String, Identifier> TEXTURE_CACHE = new HashMap<>();

    // Hệ thống quản lý dữ liệu Server live
    @Unique private final MultiplayerServerListPinger pinger = new MultiplayerServerListPinger();
    @Unique private final Map<String, ServerInfo> liveServerData = new HashMap<>();
    @Unique private long lastClickTime;

    protected TitleScreenMixin(Text title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) { LobbyManager.load(); }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        // Luôn chạy pinger để cập nhật dữ liệu online/số người chơi
        pinger.tick();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        List<LobbyManager.ServerEntry> servers = LobbyManager.getServers();
        if (servers.isEmpty()) return;

        int x = this.width / 2 - 150;
        int y = this.height - 150;

        for (int i = 0; i < servers.size(); i++) {
            LobbyManager.ServerEntry server = servers.get(i);
            int currentY = y - (i * 36);
            boolean hovered = mouseX >= x && mouseX <= x + 300 && mouseY >= currentY && mouseY <= currentY + 32;

            // --- 1. Xử lý Ping dữ liệu Live ---
            ServerInfo info = liveServerData.computeIfAbsent(server.ip, ip -> {
                ServerInfo newInfo = new ServerInfo(server.name, ip, ServerInfo.ServerType.OTHER);
                try {
                    // Ra lệnh cho shipper pinger đi lấy thông tin số người chơi
                    pinger.add(newInfo, () -> {}, () -> {});
                } catch (Exception ignored) {}
                return newInfo;
            });

            // Vẽ Box nền
            context.fill(x, currentY, x + 300, currentY + 32, hovered ? 0xA0555555 : 0x80000000);
            context.drawBorder(x, currentY, 300, 32, 0xFFFFFFFF);

            // Vẽ Icon Server
            Identifier iconId = getServerIcon(server);
            if (iconId != null) {
                context.drawTexture(RenderLayer::getGuiTextured, iconId, x + 2, currentY + 2, 0, 0, 28, 28, 28, 28);
            } else {
                context.fill(x + 2, currentY + 2, x + 30, currentY + 30, 0xFF000000);
            }

            // Nút Play khi hover
            if (hovered && mouseX >= x && mouseX <= x + 32) {
                context.drawGuiTexture(RenderLayer::getGuiTextured, JOIN_ICON, x, currentY, 32, 32);
            }

            // Vẽ Tên và IP
            context.drawText(this.textRenderer, "§e" + server.name, x + 35, currentY + 4, 0xFFFFFFFF, true);
            context.drawText(this.textRenderer, "§7" + server.ip, x + 35, currentY + 16, 0xFFFFFFFF, true);

            // --- 2. VẼ TRẠNG THÁI ONLINE & SỐ NGƯỜI CHƠI ---
            // Chữ "Online" góc trên bên phải của Box
            context.drawText(this.textRenderer, "§aOnline", x + 255, currentY + 4, 0xFFFFFFFF, true);

            // Hiện số người chơi (Current/Max) ngay dưới Online như Mai Cồ muốn nè!
            if (info.playerCountLabel != null) {
                Text countText = info.playerCountLabel;
                // Căn lề phải cho đẹp: Lấy độ rộng text để lùi lại
                int countWidth = this.textRenderer.getWidth(countText);
                context.drawText(this.textRenderer, countText, x + 295 - countWidth, currentY + 16, 0xFFFFFFFF, true);
            } else {
                context.drawText(this.textRenderer, "§8Ping...", x + 255, currentY + 16, 0xFFFFFFFF, true);
            }
        }
    }

    @Override
    public void removed() {
        super.removed();
        pinger.cancel(); // Hủy ping khi rời màn hình để đỡ tốn mạng
    }

    @Unique
    private Identifier getServerIcon(LobbyManager.ServerEntry entry) {
        if (entry.icon == null || entry.icon.isEmpty()) return null;
        if (TEXTURE_CACHE.containsKey(entry.ip)) return TEXTURE_CACHE.get(entry.ip);
        try {
            byte[] bytes = Base64.getDecoder().decode(entry.icon);
            NativeImage image = NativeImage.read(new ByteArrayInputStream(bytes));
            NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
            Identifier id = Identifier.of("addonbuu", "server_icon/" + entry.ip.replace(".", "_"));
            client.getTextureManager().registerTexture(id, texture);
            TEXTURE_CACHE.put(entry.ip, id);
            return id;
        } catch (Exception e) { return null; }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            List<LobbyManager.ServerEntry> servers = LobbyManager.getServers();
            int x = this.width / 2 - 150;
            int y = this.height - 150;

            for (int i = 0; i < servers.size(); i++) {
                int currentY = y - (i * 36);
                if (mouseX >= x && mouseX <= x + 300 && mouseY >= currentY && mouseY <= currentY + 32) {
                    long now = Util.getMeasuringTimeMs();
                    LobbyManager.ServerEntry s = servers.get(i);

                    if ((mouseX <= x + 32) || (now - lastClickTime < 250)) {
                        // Sử dụng dữ liệu info đã ping sẵn cho mượt
                        ServerInfo info = liveServerData.getOrDefault(s.ip, new ServerInfo(s.name, s.ip, ServerInfo.ServerType.OTHER));
                        ConnectScreen.connect(new MultiplayerScreen(this), this.client, ServerAddress.parse(s.ip), info, false, null);
                        return true;
                    }
                    lastClickTime = now;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
