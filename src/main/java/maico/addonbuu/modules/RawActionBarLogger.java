package maico.addonbuu.modules;

import maico.addonbuu.AddonBuu;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class RawActionBarLogger extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder().name("interval (ms)").defaultValue(500).min(50).build());
    private long lastTime = 0;

    public RawActionBarLogger() {
        super(AddonBuu.ADDONBUU, "raw-action-bar-log", "Ghi log thô hệ ms 🌈");
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        String content = null;
        if (event.packet instanceof OverlayMessageS2CPacket p) content = p.text().getString();
        else if (event.packet instanceof GameMessageS2CPacket p && p.overlay()) content = p.content().getString();

        if (content != null) {
            long now = System.currentTimeMillis();
            if (now - lastTime >= delay.get()) {
                writeRawLog(content);
                lastTime = now;
            }
        }
    }

    private void writeRawLog(String text) {
        try {
            File file = new File(mc.runDirectory, "addonbuu/raw_action_bar.log");
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                writer.write("[" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")) + "] " + text);
                writer.newLine();
                writer.flush();
            }
        } catch (IOException ignored) {}
    }
}
