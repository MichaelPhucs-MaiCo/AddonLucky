package maico.addonbuu.utils.quick_access_server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.MinecraftClient;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LobbyManager {
    private static final File FILE = new File(MinecraftClient.getInstance().runDirectory, "addonbuu/lobby_servers.json");
    private static final Gson GSON = new Gson();
    private static List<ServerEntry> lobbyServers = new ArrayList<>();

    public static class ServerEntry {
        public String name, ip, icon;
        public ServerEntry(String name, String ip, String icon) {
            this.name = name; this.ip = ip; this.icon = icon;
        }
    }

    public static void load() {
        if (!FILE.exists()) return;
        try (Reader reader = new FileReader(FILE)) {
            lobbyServers = GSON.fromJson(reader, new TypeToken<List<ServerEntry>>(){}.getType());
            if (lobbyServers == null) lobbyServers = new ArrayList<>();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void save() {
        if (!FILE.getParentFile().exists()) FILE.getParentFile().mkdirs();
        try (Writer writer = new FileWriter(FILE)) {
            GSON.toJson(lobbyServers, writer);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void addServer(String name, String ip, String icon) {
        if (!isServerInLobby(ip)) {
            lobbyServers.add(new ServerEntry(name, ip, icon));
            save();
        }
    }

    public static void removeServer(String ip) {
        lobbyServers.removeIf(s -> s.ip.equalsIgnoreCase(ip));
        save();
    }

    public static boolean isServerInLobby(String ip) {
        return lobbyServers.stream().anyMatch(s -> s.ip.equalsIgnoreCase(ip));
    }

    public static List<ServerEntry> getServers() { return lobbyServers; }
}
