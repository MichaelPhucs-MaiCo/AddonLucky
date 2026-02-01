package maico.addonbuu.settings;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.gui.themes.meteor.widgets.input.WMeteorTextBox;
import meteordevelopment.meteorclient.gui.utils.CharFilter;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.util.math.MathHelper;

import static org.lwjgl.glfw.GLFW.*;

public class WTextArea extends WMeteorTextBox {
    public int minLines = 3;
    private int dragStartCursor = -1;
    private final double lineSpacing = 2.0;

    public WTextArea(String text, CharFilter filter) {
        super(text, "Dán nội dung vào đây...", filter, null);
    }

    // Hàm helper để lấy tổng chiều cao một dòng (bao gồm khoảng cách)
    private double getLineHeight() {
        return theme.textHeight() + lineSpacing;
    }

    // --- 1. FIX SCALE NGANG & DỌC ---
    @Override
    protected void onCalculateSize() {
        double pad = pad();
        String[] lines = text.split("\n", -1);

        double maxW = 0;
        for (String line : lines) {
            maxW = Math.max(maxW, theme.textWidth(line));
        }

        width = pad + Math.max(theme.scale(260), maxW) + pad;
        // Sử dụng getLineHeight() để Box dài ra tương ứng với độ giãn dòng
        height = pad + (getLineHeight() * Math.max(minLines, lines.length)) + pad;
    }

    private int getCursorFromMouse(double mouseX, double mouseY) {
        double pad = pad();
        double relX = mouseX - x - pad;
        double relY = mouseY - y - pad;

        String[] lines = text.split("\n", -1);
        // Chia cho getLineHeight() để khi click chuột vào đúng dòng đã giãn
        int lineIdx = (int) Math.floor(relY / getLineHeight());
        lineIdx = MathHelper.clamp(lineIdx, 0, lines.length - 1);

        int pos = 0;
        for (int i = 0; i < lineIdx; i++) {
            pos += lines[i].length() + 1;
        }

        String currentLine = lines[lineIdx];
        int col = 0;
        double minDiff = Double.MAX_VALUE;

        for (int i = 0; i <= currentLine.length(); i++) {
            double w = theme.textWidth(currentLine.substring(0, i));
            double diff = Math.abs(w - relX);
            if (diff < minDiff) {
                minDiff = diff;
                col = i;
            }
        }
        return pos + col;
    }

    // --- 2. FIX BÔI ĐEN (DRAG SELECTION) ---
    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int button, boolean used) {
        if (mouseOver && !used && button == GLFW_MOUSE_BUTTON_LEFT) {
            setFocused(true);
            selecting = true;
            cursor = getCursorFromMouse(mouseX, mouseY);
            dragStartCursor = cursor;
            selectionStart = cursor;
            selectionEnd = cursor;
            return true;
        }
        return super.onMouseClicked(mouseX, mouseY, button, used);
    }

    @Override
    public void onMouseMoved(double mouseX, double mouseY, double lastMouseX, double lastMouseY) {
        if (selecting && dragStartCursor != -1) {
            cursor = getCursorFromMouse(mouseX, mouseY);
            selectionStart = Math.min(dragStartCursor, cursor);
            selectionEnd = Math.max(dragStartCursor, cursor);
        }
        super.onMouseMoved(mouseX, mouseY, lastMouseX, lastMouseY);
    }

    @Override
    public boolean onMouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) {
            selecting = false;
            dragStartCursor = -1;
        }
        return super.onMouseReleased(mouseX, mouseY, button);
    }

    // --- 3. FIX NÚT LÊN/XUỐNG (UP/DOWN NAVIGATION) ---
    @Override
    public boolean onKeyPressed(int key, int mods) {
        if (!focused) return false;

        String[] lines = text.split("\n", -1);

        if (key == GLFW_KEY_UP || key == GLFW_KEY_DOWN) {
            int currentLineIdx = 0;
            int tempPos = 0;
            for (int i = 0; i < lines.length; i++) {
                if (cursor <= tempPos + lines[i].length()) {
                    currentLineIdx = i;
                    break;
                }
                tempPos += lines[i].length() + 1;
            }
            int currentCol = cursor - tempPos;

            int nextLineIdx = (key == GLFW_KEY_UP) ? currentLineIdx - 1 : currentLineIdx + 1;

            if (nextLineIdx >= 0 && nextLineIdx < lines.length) {
                int newPos = 0;
                for (int i = 0; i < nextLineIdx; i++) {
                    newPos += lines[i].length() + 1;
                }
                cursor = newPos + Math.min(currentCol, lines[nextLineIdx].length());
                selectionStart = cursor;
                selectionEnd = cursor;
                return true;
            }
        }

        if (key == GLFW_KEY_A && (mods & GLFW_MOD_CONTROL) != 0) {
            selectionStart = 0; cursor = text.length(); selectionEnd = cursor;
            return true;
        }

        if (key == GLFW_KEY_ENTER || key == GLFW_KEY_KP_ENTER) {
            int start = Math.min(selectionStart, selectionEnd);
            int end = Math.max(selectionStart, selectionEnd);
            text = text.substring(0, start) + "\n" + text.substring(end);
            cursor = start + 1;
            selectionStart = cursor; selectionEnd = cursor;
            invalidate();
            if (action != null) action.run();
            return true;
        }

        boolean result = super.onKeyPressed(key, mods);
        if (key == GLFW_KEY_BACKSPACE || key == GLFW_KEY_DELETE) invalidate();
        return result;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        renderBackground(renderer, this, false, mouseOver);
        MeteorGuiTheme theme = theme();
        double pad = pad();
        Color textColor = theme.textColor.get();
        Color selColor = theme.textHighlightColor.get();
        double lineHeight = getLineHeight(); // Chiều cao thực tế sau khi giãn

        renderer.scissorStart(x + pad, y + pad, width - pad * 2, height - pad * 2);

        String[] lines = text.split("\n", -1);
        int charAcc = 0;
        int sStart = Math.min(selectionStart, selectionEnd);
        int sEnd = Math.max(selectionStart, selectionEnd);

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            // Y của mỗi dòng sẽ thưa ra dựa trên lineHeight
            double lineY = y + pad + (i * lineHeight);
            int lineStart = charAcc;
            int lineEnd = charAcc + line.length();

            // Highlight bôi đen
            if (sStart < lineEnd && sEnd > lineStart) {
                int selLineStart = Math.max(sStart, lineStart) - lineStart;
                int selLineEnd = Math.min(sEnd, lineEnd) - lineStart;
                double drawStart = x + pad + theme.textWidth(line.substring(0, selLineStart));
                double drawWidth = theme.textWidth(line.substring(selLineStart, selLineEnd));
                // Vẽ highlight khớp với chiều cao dòng mới
                renderer.quad(drawStart, lineY, drawWidth, lineHeight, selColor);
            }

            renderer.text(line, x + pad, lineY, textColor, false);

            // Cursor nhấp nháy
            if (focused && cursor >= lineStart && cursor <= lineEnd && (System.currentTimeMillis() / 500) % 2 == 0) {
                boolean isCursorHere = (cursor != lineEnd || i == lines.length - 1 || selectionStart != selectionEnd);
                if (isCursorHere) {
                    double cursorX = x + pad + theme.textWidth(line.substring(0, cursor - lineStart));
                    // Vẽ cursor dài bằng chiều cao dòng mới cho đẹp
                    renderer.quad(cursorX, lineY, theme.scale(1), lineHeight, textColor);
                }
            }
            charAcc += line.length() + 1;
        }
        renderer.scissorEnd();
    }

    @Override
    public boolean onCharTyped(char c) {
        boolean result = super.onCharTyped(c);
        if (result) invalidate();
        return result;
    }
}
