package maico.addonbuu.hud;

import maico.addonbuu.AddonBuu;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class BuuHud extends HudElement {
    /**
     * The {@code name} parameter should be in kebab-case.
     */
    public static final HudElementInfo<BuuHud> INFO = new HudElementInfo<>(AddonBuu.HUD_GROUP, "example", "HUD element example.", BuuHud::new);

    public BuuHud() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        setSize(renderer.textWidth("Example element", true), renderer.textHeight(true));

        // Render background
        renderer.quad(x, y, getWidth(), getHeight(), Color.LIGHT_GRAY);

        // Render text
        renderer.text("Example element", x, y, Color.WHITE, true);
    }
}
