package maico.addonbuu.commands.LogCommand;

import maico.addonbuu.hud.ModHudRenderer;
import meteordevelopment.meteorclient.commands.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

public class HienLogCommand extends Command {
    public HienLogCommand() {
        super("hienlog", "Hiện lại các thông báo nổi trên HUD.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            ModHudRenderer.showNotifications = true;
            info("§aĐã hiện lại thông báo nổi.🚀");
            return SINGLE_SUCCESS;
        });
    }
}
