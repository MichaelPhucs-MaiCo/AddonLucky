package maico.addonbuu.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import maico.addonbuu.AddonBuu;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

public class GuiTitleCommand extends Command {
    public GuiTitleCommand() {
        super("guititle", "Bật/Tắt hiển thị tiêu đề của GUI khi mở.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            AddonBuu.showGuiTitle = !AddonBuu.showGuiTitle;
            info("Soi tiêu đề GUI: " + (AddonBuu.showGuiTitle ? "§aBẬT ✅" : "§cTẮT ❌"));
            return SINGLE_SUCCESS;
        });
    }
}
