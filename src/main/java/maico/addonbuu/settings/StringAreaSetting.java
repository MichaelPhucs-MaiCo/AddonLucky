package maico.addonbuu.settings;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.IVisible;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class StringAreaSetting extends Setting<List<String>> {
    public StringAreaSetting(String name, String description, List<String> defaultValue, Consumer<List<String>> onChanged, Consumer<Setting<List<String>>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    @Override
    protected List<String> parseImpl(String str) {
        // Tách chuỗi theo dấu xuống dòng, -1 để giữ lại các dòng trống ở cuối
        return new ArrayList<>(Arrays.asList(str.split("\n", -1)));
    }

    @Override
    protected boolean isValueValid(List<String> value) {
        return true;
    }

    @Override
    public NbtCompound save(NbtCompound tag) {
        NbtList valueTag = new NbtList();
        for (String s : get()) {
            valueTag.add(NbtString.of(s));
        }
        tag.put("value", valueTag);
        return tag;
    }

    @Override
    public List<String> load(NbtCompound tag) {
        get().clear();
        NbtList valueTag = tag.getList("value", 8); // 8 là kiểu NbtString
        for (NbtElement tagI : valueTag) {
            get().add(tagI.asString());
        }
        return get();
    }

    @Override
    public void resetImpl() {
        value = new ArrayList<>(defaultValue);
    }

    public static void fillTable(GuiTheme theme, WTable table, StringAreaSetting setting) {
        // Chuyển List thành String duy nhất để hiện lên Box
        String initialText = String.join("\n", setting.get());

        // Tạo WTextArea với bộ lọc cho phép mọi ký tự (bao gồm cả \n)
        WTextArea textArea = table.add(new WTextArea(initialText, (text, c) -> true)).expandX().minWidth(300).widget();

        // Đồng bộ hóa dữ liệu khi người dùng gõ
        textArea.action = () -> {
            // Cập nhật lại danh sách dòng vào Setting
            setting.set(new ArrayList<>(Arrays.asList(textArea.get().split("\n", -1))));
        };

        // Quan trọng: Cập nhật khi mất focus để đảm bảo không mất đạn
        textArea.actionOnUnfocused = textArea.action;
    }

    public static class Builder extends SettingBuilder<Builder, List<String>, StringAreaSetting> {
        public Builder() {
            super(new ArrayList<>(0));
        }

        public Builder defaultValue(String... defaults) {
            return defaultValue(new ArrayList<>(Arrays.asList(defaults)));
        }

        @Override
        public StringAreaSetting build() {
            return new StringAreaSetting(name, description, defaultValue, onChanged, onModuleActivated, visible);
        }
    }
}
