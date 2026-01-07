package maico.addonbuu.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import maico.addonbuu.AddonBuu;
import maico.addonbuu.utils.ChatUtils;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

public class ItemCopyCommand extends Command {
    public ItemCopyCommand() {
        super("copy", "Bật/Tắt chế độ Click chuột để copy Component item.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        // Lệnh .copy on
        builder.then(literal("on").executes(context -> {
            AddonBuu.itemClickCopy = true;
            // Dùng ChatUtils để hiện [AddonBuu] và lưu vào file log luôn nhé!
            ChatUtils.addModMessage("§a§lĐÃ BẬT! §fGiờ cậu cứ mở GUI rồi click vào item để copy nhé. ✨");
            return SINGLE_SUCCESS;
        }));

        // Lệnh .copy off
        builder.then(literal("off").executes(context -> {
            AddonBuu.itemClickCopy = false;
            // Đồng bộ luôn tiền tố [AddonBuu] cho đồng nhất
            ChatUtils.addModMessage("§c§lĐÃ TẮT! §fClick chuột sẽ quay về tác dụng bình thường.");
            return SINGLE_SUCCESS;
        }));
    }
}
