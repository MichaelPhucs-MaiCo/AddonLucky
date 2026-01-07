package maico.addonbuu.modules.auto_luckyvn;

import maico.addonbuu.AddonBuu;
import maico.addonbuu.utils.ChatUtils;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Formatting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class SaveLogCheTao extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<String>> keywords = sgGeneral.add(new StringListSetting.Builder()
        .name("tu-khoa-loc")
        .description("Them cac cap bac muon luu log (Vi du: ʟɪɴʜ ᴋʜi ⭐⭐)")
        .defaultValue(Collections.singletonList("ʟɪɴʜ ᴋʜí ⭐"))
        .build()
    );

    private File currentLogFile;
    private LocalDate lastDateChecked; // Biến này để "canh" lúc qua nửa đêm nè Mai Cồ 🌙
    private final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final DateTimeFormatter DATE_FILE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public SaveLogCheTao() {
        super(AddonBuu.LUCKYVN, "save-log-che-tao", "Luu log che tao theo ngay📋");
    }

    @Override
    public void onActivate() {
        initLogFile();
        ChatUtils.addModMessage("§aĐã kích hoạt bộ lọc chế tạo theo ngày! 📝🚀");
    }

    private void initLogFile() {
        File folder = new File(mc.runDirectory, "addonbuu/log_chetao");
        if (!folder.exists()) folder.mkdirs();

        // Lấy ngày hiện tại để đặt tên file
        LocalDate now = LocalDate.now();
        lastDateChecked = now;

        String fileName = "log_chetao_" + now.format(DATE_FILE_FORMAT) + ".log";
        currentLogFile = new File(folder, fileName);

        // Ghi chú một dòng bắt đầu phiên cho đỡ nhầm
        writeToLog("\n--- PHIÊN LOG MỚI [" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "] ---");
    }

    @EventHandler
    private void onMessage(ReceiveMessageEvent event) {
        String cleanMsg = Formatting.strip(event.getMessage().getString());

        if (cleanMsg.contains("Chế tạo vật phẩm thành công")) {
            // Kiểm tra xem có phải đã qua ngày mới chưa (dành cho anh em treo máy xuyên đêm)
            checkDayTransition();

            for (String key : keywords.get()) {
                if (!key.isEmpty() && cleanMsg.contains(key)) {
                    String timestamp = "[" + LocalDateTime.now().format(TIME_FORMAT) + "] ";
                    writeToLog(timestamp + cleanMsg);

                    ChatUtils.debug("§6[Lưu Log] §fĐã ghi lại vật phẩm: §e" + key);
                    break;
                }
            }
        }
    }

    /**
     * Logic check qua đêm: Nếu ngày hiện tại khác ngày lưu gần nhất thì "kẻ vạch" phân chia
     */
    private void checkDayTransition() {
        LocalDate today = LocalDate.now();
        if (!today.equals(lastDateChecked)) {
            writeToLog("\n==================================================");
            writeToLog(">> SANG NGÀY MỚI: " + today.format(DATE_FILE_FORMAT) + " <<");
            writeToLog("==================================================\n");

            lastDateChecked = today;
            // Lưu ý: Tớ vẫn giữ nguyên currentLogFile cũ theo yêu cầu của cậu
            // để ghi dấu riêng trong cùng một file khi treo máy nhé!
        }
    }

    private void writeToLog(String line) {
        if (currentLogFile == null) return;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentLogFile, true))) {
            writer.write(line);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            AddonBuu.LOG.error("Lỗi khi ghi file log_chetao!", e);
        }
    }
}
