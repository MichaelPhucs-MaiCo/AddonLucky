package maico.addonbuu.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import maico.addonbuu.AddonBuu;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

public class ComponentCommand extends Command {
    public ComponentCommand() {
        super("component", "Bật/Tắt hiển thị component của item.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        // Lệnh .component hien
        builder.then(literal("hien").executes(context -> {
            AddonBuu.showComponents = true;
            info("Đã §ahiện§7 component trên tooltip! 👀");
            return SINGLE_SUCCESS;
        }));

        // Lệnh .component an
        builder.then(literal("an").executes(context -> {
            AddonBuu.showComponents = false;
            info("Đã §cẩn§7 component trên tooltip! 🙈");
            return SINGLE_SUCCESS;
        }));
    }
}
