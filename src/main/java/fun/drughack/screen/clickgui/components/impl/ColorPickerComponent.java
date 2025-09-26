package fun.drughack.screen.clickgui.components.impl;

import fun.drughack.screen.clickgui.components.Component;
import fun.drughack.utils.math.MathUtils;
import fun.drughack.utils.render.Render2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

public class ColorPickerComponent extends Component {

    private final ColorSettingsW setting;
    private boolean adjustingHue = false;

    private final float hueBarWidth = 150f;
    private final float hueBarHeight = 10f;

    public ColorPickerComponent(ColorSettingsW setting) {
        super(setting.getName());
        this.setting = setting;
        setHeight(hueBarHeight);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float startX = x;
        float startY = y;


        for (int i = 0; i < hueBarWidth; i++) {
            float hue = (float) i / hueBarWidth;
            int color = Color.HSBtoRGB(hue, 1f, 1f);
            Render2D.drawRoundedRect(context.getMatrices(), startX + i, startY, 2, hueBarHeight, 0, color);
        }


        float huePos = startX + setting.getHue() * hueBarWidth;
        Render2D.drawRoundedRect(context.getMatrices(), huePos - 2, startY - 1, 4, hueBarHeight + 2, 2f, Color.WHITE);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtils.isHovered(x, y, hueBarWidth, hueBarHeight, (float) mouseX, (float) mouseY)) {
            adjustingHue = button == 0;
            updateHue(mouseX);
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        adjustingHue = false;
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {}
    @Override
    public void keyReleased(int keyCode, int scanCode, int modifiers) {}
    @Override
    public void charTyped(char chr, int modifiers) {}


    public void tick(double mouseX, double mouseY) {
        if (adjustingHue) updateHue(mouseX);
    }

    private void updateHue(double mouseX) {
        float hue = (float) (mouseX - x) / hueBarWidth;
        setting.setHue(MathHelper.clamp(hue, 0f, 1f));
    }
}