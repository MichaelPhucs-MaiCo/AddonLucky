package maico.addonbuu;

import maico.addonbuu.commands.BuuCommand;
import maico.addonbuu.hud.BuuHud;
import maico.addonbuu.hud.ModHudRenderer;
import com.mojang.logging.LogUtils;
import maico.addonbuu.modules.TestLogModule;
import maico.addonbuu.modules.auto_luckyvn.*;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.item.Items;
import org.slf4j.Logger;

public class AddonBuu extends MeteorAddon {
    public static final Category ADDONBUU = new Category("AddonBuu", Items.POPPED_CHORUS_FRUIT.getDefaultStack());
    public static final Category LUCKYVN = new Category("LuckyVN", Items.POPPED_CHORUS_FRUIT.getDefaultStack());
    public static final HudGroup HUD_GROUP = new HudGroup("AddonBuu");
    public static final Logger LOG = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        LOG.info("Addon Buu đang khởi chạy... Sẵn sàng quẩy Minecraft! 🔥");

        // --- Khởi tạo Custom HUD Renderer ---
        ModHudRenderer.init(); // 2. Gọi hàm này để nó đăng ký các layer vẽ thông báo nhé!

        //ModuleS AddonBuu
        Modules.get().add(new TestLogModule());

        //Modules LuckyVN
        Modules.get().add(new TreoPhoBan());

        // Commands
        Commands.add(new BuuCommand());

        // HUD (Cái này là HUD chuẩn của Meteor)
        Hud.get().register(BuuHud.INFO);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(ADDONBUU);
        Modules.registerCategory(LUCKYVN);
    }

    @Override
    public String getPackage() {
        return "maico.addonbuu";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("Maico", "addonbuu");
    }
}
