package maico.addonbuu.modules;

import maico.addonbuu.AddonBuu;
import maico.addonbuu.utils.ChatUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.util.Formatting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SkillBridgeLog extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> logInterval = sgGeneral.add(new IntSetting.Builder()
        .name("delay-ghi-log (ms)")
        .description("Thời gian chờ giữa mỗi lần cập nhật file log (ms).")
        .defaultValue(500)
        .min(0)
        .sliderMax(2000)
        .build()
    );

    private long lastLogTime = 0;

    public SkillBridgeLog() {
        super(AddonBuu.ADDONBUU, "skill-bridge-log", "Bắt Action Bar Tu Tiên (Hệ ms) ⚡");
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        String content = null;
        if (event.packet instanceof OverlayMessageS2CPacket p) {
            content = p.text().getString();
        } else if (event.packet instanceof GameMessageS2CPacket p && p.overlay()) {
            content = p.content().getString();
        }

        if (content != null) {
            long currentTime = System.currentTimeMillis();
            // So sánh trực tiếp bằng ms, không nhân 1000 nữa
            if (currentTime - lastLogTime >= logInterval.get()) {
                saveToFile(Formatting.strip(content));
                lastLogTime = currentTime;
            }
        }
    }

    private void saveToFile(String content) {
        try {
            File file = new File(mc.runDirectory, "addonbuu/action_bar_tutienrpg.log");
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
                writer.write(content);
                writer.flush();
            }
        } catch (IOException e) {
            ChatUtils.error("Lỗi ghi file SkillBridge!");
        }
    }
}
