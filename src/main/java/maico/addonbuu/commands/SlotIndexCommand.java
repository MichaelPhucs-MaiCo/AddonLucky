package maico.addonbuu.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import maico.addonbuu.AddonBuu;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

public class SlotIndexCommand extends Command {
    public SlotIndexCommand() {
        super("slotindex", "Bật/Tắt hiển thị số thứ tự (Index) của mỗi ô slot.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            AddonBuu.showSlotIndex = !AddonBuu.showSlotIndex;
            info("Hiển thị Slot Index: " + (AddonBuu.showSlotIndex ? "§aBẬT ✅" : "§cTẮT ❌"));
            return SINGLE_SUCCESS;
        });
    }
}
