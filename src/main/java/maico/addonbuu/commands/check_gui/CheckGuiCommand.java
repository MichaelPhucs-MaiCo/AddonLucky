package maico.addonbuu.commands.check_gui;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import maico.addonbuu.AddonBuu;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

public class CheckGuiCommand extends Command {
    public CheckGuiCommand() {
        super("checkgui", "Bật/Tắt tính năng soi tiêu đề của GUI khi mở.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            AddonBuu.showCheckGui = !AddonBuu.showCheckGui;
            info("Soi tiêu đề GUI: " + (AddonBuu.showCheckGui ? "§aBẬT ✅" : "§cTẮT ❌"));
            return SINGLE_SUCCESS;
        });
    }
}
