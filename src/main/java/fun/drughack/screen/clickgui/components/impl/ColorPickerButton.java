package fun.drughack.screen.clickgui.components.impl;

import fun.drughack.screen.clickgui.ColorPickerWindow;
import fun.drughack.screen.clickgui.components.Component;
import fun.drughack.utils.render.Render2D;
import fun.drughack.utils.render.fonts.Fonts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

public class ColorPickerButton extends Component {

    private final ColorSettings setting;

    public ColorPickerButton(ColorSettings setting) {
        super("Open Color Picker");
        this.setting = setting;
        this.height = 15f;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        Color color = setting.getAwtColor();
        Render2D.drawStyledRect(context.getMatrices(), x, y, width, height, 3f, color, 255);
        Render2D.drawFont(context.getMatrices(), Fonts.REGULAR.getFont(8f), "Open Color Picker", x + 4, y + 3, Color.WHITE);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 &&
                mouseX >= x && mouseX <= x + width &&
                mouseY >= y && mouseY <= y + height) {

            mc.setScreen(new ColorPickerWindow(setting));
        }
    }

    @Override public void mouseReleased(double mouseX, double mouseY, int button) {}
    @Override public void keyPressed(int keyCode, int scanCode, int modifiers) {}
    @Override public void keyReleased(int keyCode, int scanCode, int modifiers) {}
    @Override public void charTyped(char chr, int modifiers) {}
}