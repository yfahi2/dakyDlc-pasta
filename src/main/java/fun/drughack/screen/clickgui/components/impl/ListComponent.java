package fun.drughack.screen.clickgui.components.impl;

import fun.drughack.modules.settings.impl.BooleanSetting;
import fun.drughack.modules.settings.impl.ListSetting;
import fun.drughack.screen.clickgui.components.Component;
import fun.drughack.utils.animations.Animation;
import fun.drughack.utils.animations.Easing;
import fun.drughack.utils.math.MathUtils;
import fun.drughack.utils.render.Render2D;
import fun.drughack.utils.render.fonts.Fonts;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.resource.language.I18n;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ListComponent extends Component {

    private final ListSetting setting;
    private final Animation openAnimation = new Animation(300, 1f, false, Easing.BOTH_SINE);
    private final Map<BooleanSetting, Animation> pickAnimations = new HashMap<>();
    private boolean open;

    public ListComponent(ListSetting setting) {
        super(setting.getName());
        this.setting = setting;
        for (BooleanSetting s : setting.getValue()) pickAnimations.put(s, new Animation(300, 1f, false, Easing.BOTH_SINE));
        this.addHeight = () -> openAnimation.getValue() > 0 ? ((setting.getValue().size() * 14f)) * openAnimation.getValue() : 0;
        this.visible = setting::isVisible;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        openAnimation.update(open);

        Render2D.drawFont(context.getMatrices(),
                Fonts.REGULAR.getFont(7.5f),
                I18n.translate(setting.getName()),
                x + 5f,
                y + 3.5f,
                Color.WHITE
        );

        Render2D.drawFont(context.getMatrices(),
                Fonts.REGULAR.getFont(7.5f),
                "(" + setting.getToggled().size() + "/" + setting.getValue().size() + ")",
                x + width - Fonts.REGULAR.getWidth("(" + setting.getToggled().size() + "/" + setting.getValue().size() + ")", 7.5f) - 5f,
                y + 3.5f,
                Color.WHITE
        );

        if (openAnimation.getValue() > 0) {
            float yOffset = height;
            for (BooleanSetting setting : setting.getValue()) {
                Animation anim = pickAnimations.get(setting);
                anim.update(setting.getValue());
                Render2D.drawFont(context.getMatrices(), Fonts.ICONS.getFont(10f), "D", x + width - 14f, y + yOffset + 3.5f, new Color(255, 255, 255, (int) (255 * anim.getValue())));
                Render2D.drawFont(context.getMatrices(),
                        Fonts.REGULAR.getFont(7.5f),
                        I18n.translate(setting.getName()),
                        x + 6f,
                        y + yOffset + 3.5f,
                        Color.WHITE
                );
                yOffset += 14f;
            }
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtils.isHovered(x, y, width, height, (float) mouseX, (float) mouseY) && button == 1) open = !open;

        if (openAnimation.getValue() > 0 && button == 0) {
            float yOffset = height;
            for (BooleanSetting setting : setting.getValue()) {
                if (MathUtils.isHovered(x, y + yOffset, width, 14f, (float) mouseX, (float) mouseY)) {
                    setting.setValue(!setting.getValue());
                    break;
                }

                yOffset += 14f;
            }
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {

    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {

    }

    @Override
    public void keyReleased(int keyCode, int scanCode, int modifiers) {

    }

    @Override
    public void charTyped(char chr, int modifiers) {

    }
}