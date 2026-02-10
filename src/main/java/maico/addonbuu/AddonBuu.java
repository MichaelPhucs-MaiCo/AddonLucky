package maico.addonbuu;

import maico.addonbuu.commands.*;
import maico.addonbuu.commands.LogCommand.AnLogCommand;
import maico.addonbuu.commands.LogCommand.HienLogCommand;
import maico.addonbuu.commands.check_gui.CheckGuiCommand;

import maico.addonbuu.hud.BuuHud;
import maico.addonbuu.hud.ModHudRenderer;
import maico.addonbuu.hud.SaveTargetHud;

import com.mojang.logging.LogUtils;
import maico.addonbuu.modules.*;
import maico.addonbuu.modules.FairyPrion.*;
import maico.addonbuu.modules.TestLogModule;
import maico.addonbuu.modules.auto_luckyvn.*;
import maico.addonbuu.modules.autofish.*;
import maico.addonbuu.modules.logs.AnLog;
import maico.addonbuu.modules.treo_pho_ban.*;
import maico.addonbuu.utils.FileLogger;

// --- Thêm các import này để xử lý GUI ---
import maico.addonbuu.settings.StringAreaSetting;
import maico.addonbuu.utils.quick_access_server.LobbyManager;
import meteordevelopment.meteorclient.gui.utils.SettingsWidgetFactory;
// ---------------------------------------

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
    public static final Category TREOPHOBAN = new Category("TreoPhoBan", Items.POPPED_CHORUS_FRUIT.getDefaultStack());
    public static final Category CLICK_SLOT_CUSTOM = new Category("ClickSlotCustom", Items.POPPED_CHORUS_FRUIT.getDefaultStack());
    public static final Category FAIRY_PRISON = new Category("FairyPrison", Items.POPPED_CHORUS_FRUIT.getDefaultStack());
    public static final HudGroup HUD_GROUP = new HudGroup("AddonBuu");
    public static final Logger LOG = LogUtils.getLogger();

    public static boolean showComponents = false;
    public static boolean showGuiTitle = false;
    public static boolean itemClickCopy = false;
    public static boolean showSlotIndex = false;
    public static boolean showPetInfo = false;
    public static boolean showCheckGui = false;

    @Override
    public void onInitialize() {
        FileLogger.init();
        LobbyManager.load();
        maico.addonbuu.utils.quick_access_server.BridgeProtocolHandler.initializeBridge();

        LOG.info("Addon Buu đang khởi chạy...🔥");

        // --- ĐĂNG KÝ STRING AREA SETTING VÀO HỆ THỐNG METEOR ---
        // Dòng này cực kỳ quan trọng để Meteor biết cách vẽ cái box của cậu
        SettingsWidgetFactory.registerCustomFactory(StringAreaSetting.class, (theme) -> (table, setting) -> {
            StringAreaSetting.fillTable(theme, table, (StringAreaSetting) setting);
        });

        ModHudRenderer.init();
        SaveTargetHud.init();

        // Modules AddonBuu
        Modules.get().add(new TestLogModule());
        Modules.get().add(new SlotIndex());
        Modules.get().add(new TestHud());
        Modules.get().add(new SavePos());
        Modules.get().add(new SaveTarget());
        Modules.get().add(new AutoFish());

        // Modules LuckyVN
        Modules.get().add(new TreoPhoBan());
        Modules.get().add(new AutoEnableDanDuoc());
        Modules.get().add(new TuCatDo());
        Modules.get().add(new AutoWarp());
        Modules.get().add(new SaveLogCheTao());
        Modules.get().add(new CopyDataComp());
        Modules.get().add(new AutoClickCustom());
        Modules.get().add(new ChongTreoPhoBan());
        Modules.get().add(new TheoDoiCraft());

        // FairyPrion
        Modules.get().add(new AutoSellFP());
        Modules.get().add(new NukerFP());
        Modules.get().add(new AutoWarpFP());
        Modules.get().add(new SpamScriptFP());
        Modules.get().add(new CheckNukerFP());
        Modules.get().add(new FarmMineFP());

        // Module Logs
        Modules.get().add(new AnLog());

        // Commands
        Commands.add(new AnLogCommand());
        Commands.add(new HienLogCommand());
        Commands.add(new ComponentCommand());
        Commands.add(new GuiTitleCommand());
        Commands.add(new ItemCopyCommand());
        Commands.add(new SlotIndexCommand());
        Commands.add(new CheckGuiCommand());

        // HUD
        Hud.get().register(BuuHud.INFO);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(ADDONBUU);
        Modules.registerCategory(LUCKYVN);
        Modules.registerCategory(CLICK_SLOT_CUSTOM);
        Modules.registerCategory(TREOPHOBAN);
        Modules.registerCategory(FAIRY_PRISON);
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
