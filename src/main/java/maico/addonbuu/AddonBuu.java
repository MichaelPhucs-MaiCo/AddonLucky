package maico.addonbuu;

import maico.addonbuu.commands.*;
import maico.addonbuu.commands.LogCommand.AnLogCommand;
import maico.addonbuu.commands.LogCommand.HienLogCommand;
import maico.addonbuu.hud.BuuHud;
import maico.addonbuu.hud.ModHudRenderer;
import com.mojang.logging.LogUtils;
import maico.addonbuu.modules.TestLogModule;
import maico.addonbuu.modules.auto_luckyvn.*;
import maico.addonbuu.modules.logs.AnLog;
import maico.addonbuu.utils.FileLogger;
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
    public static final Category CLICK_SLOT_CUSTOM = new Category("ClickSlotCustom", Items.POPPED_CHORUS_FRUIT.getDefaultStack());
    public static final HudGroup HUD_GROUP = new HudGroup("AddonBuu");
    public static final Logger LOG = LogUtils.getLogger();

    public static boolean showComponents = false;
    public static boolean showGuiTitle = false; //in title cửa sổ ra log
    public static boolean itemClickCopy = false; //copy component item

    @Override
    public void onInitialize() {
        FileLogger.init();

        LOG.info("Addon Buu đang khởi chạy... Sẵn sàng quẩy Minecraft! 🔥");

        // --- Khởi tạo Custom HUD Renderer ---
        ModHudRenderer.init(); // 2. Gọi hàm này để nó đăng ký các layer vẽ thông báo nhé!

        //ModuleS AddonBuu
        Modules.get().add(new TestLogModule());

        //Modules LuckyVN
        Modules.get().add(new TreoPhoBan());
        Modules.get().add(new AutoEnableDanDuoc());
        Modules.get().add(new TuCatDo());
        Modules.get().add(new AutoWarp());
        Modules.get().add(new SaveLogCheTao());
        Modules.get().add(new CopyDataComp());
        Modules.get().add(new AutoClickCustom());

        //Module Logs
        Modules.get().add(new AnLog());

        // Commands
        Commands.add(new AnLogCommand());
        Commands.add(new HienLogCommand());
        Commands.add(new ComponentCommand());
        Commands.add(new GuiTitleCommand());
        Commands.add(new ItemCopyCommand());


        // HUD (Cái này là HUD chuẩn của Meteor)
        Hud.get().register(BuuHud.INFO);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(ADDONBUU);
        Modules.registerCategory(LUCKYVN);
        Modules.registerCategory(CLICK_SLOT_CUSTOM);
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
