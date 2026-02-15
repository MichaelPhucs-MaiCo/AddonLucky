package maico.addonbuu;

import maico.addonbuu.commands.*;
import maico.addonbuu.commands.LogCommand.AnLogCommand;
import maico.addonbuu.commands.LogCommand.HienLogCommand;
import maico.addonbuu.commands.check_gui.CheckGuiCommand;
import maico.addonbuu.hud.BuuHud;
import maico.addonbuu.hud.ModHudRenderer;
import maico.addonbuu.hud.SaveTargetHud;
import maico.addonbuu.modules.logs.*;
import maico.addonbuu.utils.SecurityUtils;
import com.mojang.logging.LogUtils;
import maico.addonbuu.modules.*;
import maico.addonbuu.modules.FairyPrion.*;
import maico.addonbuu.modules.TestLogModule;
import maico.addonbuu.modules.auto_luckyvn.*;
import maico.addonbuu.modules.autofish.*;
import maico.addonbuu.modules.logs.AnLog;
import maico.addonbuu.modules.treo_pho_ban.*;
import maico.addonbuu.utils.FileLogger;
import maico.addonbuu.settings.StringAreaSetting;
import maico.addonbuu.utils.quick_access_server.LobbyManager;
import meteordevelopment.meteorclient.gui.utils.SettingsWidgetFactory;
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
        // --- CHỐT CHẶN BẢO MẬT CỦA MAI CỒ ---
        if (!SecurityUtils.isVerified()) {
            String hwid = SecurityUtils.getHWID();
            SecurityUtils.copyHWIDToClipboard(); // Tự động copy HWID cho khách

            LOG.error("====================================================");
            LOG.error("   ADDONBUU: MÁY CHƯA KÍCH HOẠT! ❌");
            LOG.error("   HWID CỦA BẠN ĐÃ ĐƯỢC COPY VÀO CLIPBOARD.");
            LOG.error("   HÃY GỬI NÓ CHO BUU ĐỂ NHẬN LICENSE.DAT");
            LOG.error("   HWID: " + hwid);
            LOG.error("====================================================");

            // Dừng toàn bộ việc đăng ký module/command
            return;
        }

        // --- NẾU VƯỢT QUA KIỂM TRA THÌ MỚI CHẠY TIẾP ---
        FileLogger.init();
        LobbyManager.load();
        LOG.info("Addon Buu đã được kích hoạt thành công! Quẩy đêyyyyyy🔥");

        SettingsWidgetFactory.registerCustomFactory(StringAreaSetting.class, (theme) -> (table, setting) -> {
            StringAreaSetting.fillTable(theme, table, (StringAreaSetting) setting);
        });

        ModHudRenderer.init();
        SaveTargetHud.init();

        // Đăng ký Modules
        Modules.get().add(new TestLogModule());
        Modules.get().add(new SlotIndex());
        Modules.get().add(new TestHud());
        Modules.get().add(new SavePos());
        Modules.get().add(new SaveTarget());
        Modules.get().add(new AutoFish());
        Modules.get().add(new TreoPhoBan());
        Modules.get().add(new AutoEnableDanDuoc());
        Modules.get().add(new TuCatDo());
        Modules.get().add(new AutoWarp());
        Modules.get().add(new SaveLogCheTao());
        Modules.get().add(new CopyDataComp());
        Modules.get().add(new AutoClickCustom());
        Modules.get().add(new ChongTreoPhoBan());
        Modules.get().add(new TheoDoiCraft());
        Modules.get().add(new AutoSellFP());
        Modules.get().add(new NukerFP());
        Modules.get().add(new AutoWarpFP());
        Modules.get().add(new SpamScriptFP());
        Modules.get().add(new CheckNukerFP());
        Modules.get().add(new FarmMineFP());
        Modules.get().add(new CheckDungIm());
        Modules.get().add(new AnLog());
        Modules.get().add(new PacketLogger());
        Modules.get().add(new AutoFishHold());


        // Đăng ký Commands
        Commands.add(new AnLogCommand());
        Commands.add(new HienLogCommand());
        Commands.add(new ComponentCommand());
        Commands.add(new GuiTitleCommand());
        Commands.add(new ItemCopyCommand());
        Commands.add(new SlotIndexCommand());
        Commands.add(new CheckGuiCommand());

        Hud.get().register(BuuHud.INFO);
    }

    @Override
    public void onRegisterCategories() {
        // Chỉ đăng ký Category nếu đã vượt qua kiểm tra bảo mật (để chắc ăn 2 lớp)
        if (SecurityUtils.isVerified()) {
            Modules.registerCategory(ADDONBUU);
            Modules.registerCategory(LUCKYVN);
            Modules.registerCategory(CLICK_SLOT_CUSTOM);
            Modules.registerCategory(TREOPHOBAN);
            Modules.registerCategory(FAIRY_PRISON);
        }
    }

    @Override
    public String getPackage() { return "maico.addonbuu"; }

    @Override
    public GithubRepo getRepo() { return new GithubRepo("Maico", "addonbuu"); }
}
