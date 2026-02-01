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
import net.minecraft.util.Formatting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestHud extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // --- SETTINGS ---
    private final Setting<Integer> logInterval = sgGeneral.add(new IntSetting.Builder()
        .name("delay-ghi-log")
        .description("Thời gian chờ giữa mỗi lần ghi log (giây).")
        .defaultValue(5)
        .min(0)
        .sliderMax(60)
        .build()
    );

    private String lastLogText = "";
    private long lastLogTime = 0;

    private static final DateTimeFormatter DATE_FILE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public TestHud() {
        super(AddonBuu.ADDONBUU, "test-hud-logger", "Ghi log Action Bar vào file📋");
    }

    @Override
    public void onActivate() {
        lastLogText = "";
        lastLogTime = 0;
        ChatUtils.addModMessage("🧐 Log Action Bar đã sẵn sàng! Log sẽ được lưu tại: §eaddonbuu/log_actionbar");
    }



    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof OverlayMessageS2CPacket packet) {
            long currentTime = System.currentTimeMillis();

            // 1. Kiểm tra Cooldown dựa trên Setting
            if (currentTime - lastLogTime < logInterval.get() * 1000L) {
                return;
            }

            String rawText = packet.text().getString();
            String cleanText = Formatting.strip(rawText);

            // 2. Chỉ ghi nếu có nội dung và khác dòng cũ (hoặc nếu cậu muốn ghi đè liên tục thì bỏ check equals)
            if (!rawText.isEmpty() && !rawText.equals(lastLogText)) {
                saveToLogFile(rawText, cleanText);

                lastLogText = rawText;
                lastLogTime = currentTime;
            }
        }
    }

    private void saveToLogFile(String raw, String clean) {
        try {
            // 1. Tạo thư mục log_actionbar
            File folder = new File(mc.runDirectory, "addonbuu/log_actionbar");
            if (!folder.exists()) folder.mkdirs();

            // 2. Đặt tên file theo ngày hiện tại
            String fileName = "actionbar_" + LocalDate.now().format(DATE_FILE_FORMAT) + ".log";
            File logFile = new File(folder, fileName);

            // 3. Ghi dữ liệu kèm timestamp
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
                String timestamp = "[" + LocalDateTime.now().format(TIME_FORMAT) + "] ";
                writer.write(timestamp + clean);
                writer.newLine();
                writer.flush();
            }

            // In debug nhẹ để Mai Cồ biết nó đang chạy ngầm
            ChatUtils.debug("§6[ActionLog] §fĐã lưu: §7" + (clean.length() > 30 ? clean.substring(0, 30) + "..." : clean));

        } catch (IOException e) {
            ChatUtils.error("Lỗi khi ghi log Action Bar!");
            e.printStackTrace();
        }
    }
}
