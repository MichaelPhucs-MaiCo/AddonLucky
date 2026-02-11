package maico.addonbuu.utils.quick_access_server.protocol;

import maico.addonbuu.utils.quick_access_server.LobbyManager;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.orbit.EventHandler;
import java.util.concurrent.CompletableFuture;

/**
 * LobbyDataSync - Đảm bảo dữ liệu sảnh luôn được làm mới khi vào game.
 * (Bản tạm thời phục vụ hệ thống Quick Access)
 */
public class LobbyDataSync {

    public static void start() {
        // Chỉ đơn giản là nạp lại dữ liệu từ file local
        CompletableFuture.runAsync(LobbyDataSync::refreshLocalCache);
        MeteorClient.EVENT_BUS.subscribe(new LobbyDataSync());
    }

    @EventHandler
    private void onGameJoin(GameJoinedEvent event) {
        refreshLocalCache();
    }

    private static void refreshLocalCache() {
        // Gọi hàm load của LobbyManager để đồng bộ list server
        LobbyManager.load();
    }
}
