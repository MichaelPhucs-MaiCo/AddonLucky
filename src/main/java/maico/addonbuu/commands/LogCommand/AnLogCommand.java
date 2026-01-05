package maico.addonbuu.commands.LogCommand;

import maico.addonbuu.hud.ModHudRenderer;
import meteordevelopment.meteorclient.commands.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

public class AnLogCommand extends Command {
    public AnLogCommand() {
        super("anlog", "Tạm ẩn các thông báo nổi trên HUD.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            ModHudRenderer.showNotifications = false;
            info("§eĐã ẩn thông báo nổi. Gõ §a.hienlog §eđể hiện lại log 👀");
            return SINGLE_SUCCESS;
        });
    }
}
