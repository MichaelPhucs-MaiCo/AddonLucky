package maico.addonbuu.modules.logs;

import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import maico.addonbuu.AddonBuu;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.network.PacketUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.Packet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class PacketLogger extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgOutput = settings.createGroup("Output");

    private final Setting<Set<Class<? extends Packet<?>>>> s2cPackets = sgGeneral.add(new PacketListSetting.Builder()
        .name("S2C-packets")
        .description("Các packet từ Server gửi về Client muốn log.")
        .filter(aClass -> PacketUtils.getS2CPackets().contains(aClass))
        .build()
    );

    private final Setting<Set<Class<? extends Packet<?>>>> c2sPackets = sgGeneral.add(new PacketListSetting.Builder()
        .name("C2S-packets")
        .description("Các packet từ Client gửi lên Server muốn log.")
        .filter(aClass -> PacketUtils.getC2SPackets().contains(aClass))
        .build()
    );

    private final Setting<Boolean> showTimestamp = sgOutput.add(new BoolSetting.Builder()
        .name("show-timestamp")
        .description("Hiện thời gian cho mỗi dòng log.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showPacketData = sgOutput.add(new BoolSetting.Builder()
        .name("show-packet-data")
        .description("Hiện chi tiết dữ liệu packet (toString).")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> showCount = sgOutput.add(new BoolSetting.Builder()
        .name("show-count")
        .description("Hiện số lần packet này đã xuất hiện.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showSummary = sgOutput.add(new BoolSetting.Builder()
        .name("show-summary")
        .description("Hiện tổng kết khi tắt module.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> logToChat = sgOutput.add(new BoolSetting.Builder()
        .name("log-to-chat")
        .description("In log ra khung chat.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> logToFile = sgOutput.add(new BoolSetting.Builder()
        .name("log-to-file")
        .description("Lưu log vào file trong thư mục meteor-client.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> flushInterval = sgOutput.add(new IntSetting.Builder()
        .name("flush-interval")
        .description("Tần suất ghi file (giây).")
        .defaultValue(1)
        .min(1)
        .sliderMax(10)
        .visible(logToFile::get)
        .build()
    );

    private final Setting<Integer> maxFileSizeMB = sgOutput.add(new IntSetting.Builder()
        .name("max-file-size-mb")
        .description("Dung lượng tối đa mỗi file (MB).")
        .defaultValue(10)
        .min(1)
        .sliderMax(100)
        .visible(logToFile::get)
        .build()
    );

    private final Setting<Integer> maxTotalLogsMB = sgOutput.add(new IntSetting.Builder()
        .name("max-total-logs-mb")
        .description("Tổng dung lượng log tối đa (MB). Xóa file cũ nếu vượt quá.")
        .defaultValue(50)
        .min(1)
        .sliderMax(500)
        .visible(logToFile::get)
        .build()
    );

    private static final Path PACKET_LOGS_DIR = MeteorClient.FOLDER.toPath().resolve("packet-logs");
    private static final int LINE_SEPARATOR_BYTES = System.lineSeparator().getBytes(StandardCharsets.UTF_8).length;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final DateTimeFormatter FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private final Reference2IntOpenHashMap<Class<? extends Packet<?>>> packetCounts = new Reference2IntOpenHashMap<>();

    private BufferedWriter fileWriter;
    private long lastFlushMs;
    private long currentFileSizeBytes;
    private int currentFileIndex;
    private LocalDateTime sessionStartTime;

    public PacketLogger() {
        // Tớ cho vào Category ADDONBUU của cậu luôn nhé!
        super(AddonBuu.ADDONBUU, "packet-logger", "Ghi lại các gói tin (packets) để debug. 🔍");
        runInMainMenu = true;
    }

    @Override
    public void onActivate() {
        closeFileWriter();
        packetCounts.clear();
        lastFlushMs = System.currentTimeMillis();
        sessionStartTime = LocalDateTime.now();
        currentFileIndex = 0;
        currentFileSizeBytes = 0;

        if (logToFile.get()) {
            try {
                Files.createDirectories(PACKET_LOGS_DIR);
                cleanupOldLogs();
                openNewLogFile();
            } catch (IOException e) {
                error("Không thể khởi tạo ghi file: %s", e.getMessage());
                fileWriter = null;
            }
        }
    }

    @Override
    public void onDeactivate() {
        if (showSummary.get() && !packetCounts.isEmpty()) {
            logSummary();
        }
        closeFileWriter();
    }

    private void logPacket(String direction, Packet<?> packet) {
        if (!logToChat.get() && !logToFile.get()) return;

        Class<? extends Packet<?>> packetClass = (Class<? extends Packet<?>>) packet.getClass();
        packetCounts.addTo(packetClass, 1);

        StringBuilder msg = new StringBuilder(128);
        if (showTimestamp.get()) msg.append("[").append(LocalDateTime.now().format(TIME_FORMATTER)).append("] ");
        msg.append(direction).append(" ").append(PacketUtils.getName(packetClass));
        if (showCount.get()) msg.append(" (#").append(packetCounts.getInt(packetClass)).append(")");
        if (showPacketData.get()) msg.append("\n  Data: ").append(packet);

        String line = msg.toString();
        if (logToChat.get()) info(line);
        if (logToFile.get()) writeLine(line);
    }

    private void logSummary() {
        int totalPackets = packetCounts.values().intStream().sum();
        List<String> lines = new ArrayList<>();
        lines.add("--- TỔNG KẾT PACKET ---");
        lines.add("Tổng số gói tin đã log: " + totalPackets);

        packetCounts.reference2IntEntrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getIntValue(), a.getIntValue()))
            .forEach(e -> lines.add("  %s: %d".formatted(PacketUtils.getName(e.getKey()), e.getIntValue())));

        for (String line : lines) {
            if (logToChat.get()) info(line);
            if (logToFile.get()) writeLine(line);
        }
    }

    private void writeLine(String line) {
        if (fileWriter == null) return;
        try {
            int lineBytes = line.getBytes(StandardCharsets.UTF_8).length + LINE_SEPARATOR_BYTES;
            if (currentFileSizeBytes + lineBytes > maxFileSizeMB.get() * 1024L * 1024L) openNewLogFile();

            fileWriter.write(line);
            fileWriter.newLine();
            currentFileSizeBytes += lineBytes;

            long now = System.currentTimeMillis();
            if (now - lastFlushMs >= flushInterval.get() * 1000L) {
                fileWriter.flush();
                lastFlushMs = now;
            }
        } catch (IOException e) {
            error("Lỗi ghi file: %s. Đã tắt ghi file.", e.getMessage());
            closeFileWriter();
        }
    }

    private void openNewLogFile() throws IOException {
        if (fileWriter != null) fileWriter.close();
        if (sessionStartTime == null) sessionStartTime = LocalDateTime.now();

        String fileName = "packets-%s-%d.log".formatted(sessionStartTime.format(FILE_NAME_FORMATTER), currentFileIndex++);
        fileWriter = Files.newBufferedWriter(
            PACKET_LOGS_DIR.resolve(fileName),
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE
        );
        currentFileSizeBytes = 0;
        cleanupOldLogs();
    }

    private void closeFileWriter() {
        if (fileWriter != null) {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException ignored) {}
            fileWriter = null;
        }
    }

    private void cleanupOldLogs() throws IOException {
        long maxBytes = maxTotalLogsMB.get() * 1024L * 1024L;
        List<LogFileEntry> logFiles = new ArrayList<>();
        try (var stream = Files.list(PACKET_LOGS_DIR)) {
            for (Path p : stream.toList()) {
                String name = p.getFileName().toString();
                if (!name.startsWith("packets-") || !name.endsWith(".log")) continue;
                try {
                    logFiles.add(new LogFileEntry(p, Files.size(p), Files.getLastModifiedTime(p).toMillis()));
                } catch (IOException ignored) {}
            }
        }
        logFiles.sort(Comparator.comparingLong(LogFileEntry::lastModified));
        long totalSize = 0;
        for (LogFileEntry entry : logFiles) {
            totalSize += entry.size();
            if (totalSize > maxBytes) Files.deleteIfExists(entry.path());
        }
    }

    private record LogFileEntry(Path path, long size, long lastModified) {}

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onReceivePacket(PacketEvent.Receive event) {
        if (s2cPackets.get().contains(event.packet.getClass())) logPacket("<- S2C", event.packet);
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onSendPacket(PacketEvent.Send event) {
        if (c2sPackets.get().contains(event.packet.getClass())) logPacket("-> C2S", event.packet);
    }
}
