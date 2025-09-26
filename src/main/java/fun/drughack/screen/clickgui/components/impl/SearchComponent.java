package fun.drughack.screen.clickgui.components.impl;

import fun.drughack.screen.clickgui.components.Component;
import fun.drughack.utils.math.MathUtils;
import fun.drughack.utils.render.Render2D;
import fun.drughack.utils.render.fonts.Fonts;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class SearchComponent extends Component {

    private StringBuilder input = new StringBuilder();
    private boolean typing = false;

    public SearchComponent() {
        super("Search");
        this.setHeight(15f);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        Render2D.drawRoundedRect(context.getMatrices(), x, y, width, height, 2f, new Color(0, 0, 0, 150));


        String text = input.isEmpty() ? "Search..." : input.toString();
        Color color = input.isEmpty() ? new Color(150, 150, 150) : Color.WHITE;

        Render2D.drawFont(
                context.getMatrices(),
                Fonts.REGULAR.getFont(8f),
                text,
                x + 4f,
                y + 3f,
                color
        );


        if (typing && System.currentTimeMillis() % 1000 < 500) {
            float textWidth = Fonts.REGULAR.getWidth(text, 8f);
            Render2D.drawFont(
                    context.getMatrices(),
                    Fonts.REGULAR.getFont(8f),
                    "|",
                    x + 4f + textWidth,
                    y + 3f,
                    Color.WHITE
            );
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        typing = MathUtils.isHovered(x, y, width, height, (float) mouseX, (float) mouseY) && button == 0;
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {}

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {

        if (!typing) return;

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && input.length() > 0) {
            input.deleteCharAt(input.length() - 1);
        } else if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
            typing = false;
        }
    }

    @Override
    public void keyReleased(int keyCode, int scanCode, int modifiers) {}

    @Override
    public void charTyped(char chr, int modifiers) {
        if (!typing) return;
        if (Character.isISOControl(chr)) return;
        input.append(chr);
    }

    public String getSearchText() {
        return input.toString().toLowerCase();
    }
}
