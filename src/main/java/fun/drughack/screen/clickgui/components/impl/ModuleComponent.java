package fun.drughack.screen.clickgui.components.impl;

import fun.drughack.DrugHack;
import fun.drughack.modules.api.Module;
import fun.drughack.modules.settings.Setting;
import fun.drughack.modules.settings.api.Bind;
import fun.drughack.modules.settings.impl.BindSetting;
import fun.drughack.modules.settings.impl.BooleanSetting;
import fun.drughack.modules.settings.impl.EnumSetting;
import fun.drughack.modules.settings.impl.ListSetting;
import fun.drughack.modules.settings.impl.NumberSetting;
import fun.drughack.modules.settings.impl.StringSetting;
import fun.drughack.screen.clickgui.components.Component;
import fun.drughack.utils.animations.Animation;
import fun.drughack.utils.animations.Easing;
import fun.drughack.utils.math.MathUtils;
import fun.drughack.utils.render.Render2D;
import fun.drughack.utils.render.fonts.Fonts;
import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleComponent extends Component {

    private final Module module;
    private boolean open, binding;

    @Getter private final List<Component> components = new ArrayList<>();

    private final Animation alphaAnimation = new Animation(300, 1f, false, Easing.BOTH_SINE);
    private final Animation toggleAnimation = new Animation(150, 1f, false, Easing.BOTH_SINE);
    private final Animation showingAnimation = new Animation(300, 1f, false, Easing.BOTH_SINE);
    @Getter private final Animation openAnimation = new Animation(300, 1f, false, Easing.BOTH_SINE);

    public ModuleComponent(Module module) {
        super(module.getName());
        this.module = module;

        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof BooleanSetting) components.add(new BooleanComponent((BooleanSetting) setting));
            else if (setting instanceof NumberSetting) components.add(new SliderComponent((NumberSetting) setting));
            else if (setting instanceof EnumSetting) components.add(new EnumComponent((EnumSetting<?>) setting));
            else if (setting instanceof StringSetting) components.add(new StringComponent((StringSetting) setting));
            else if (setting instanceof ListSetting) components.add(new ListComponent((ListSetting) setting));
            else if (setting instanceof BindSetting) components.add(new BindComponent((BindSetting) setting));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean showing = Screen.hasAltDown() && !module.getBind().isEmpty();
        boolean hovered = MathUtils.isHovered(x, y, width, height, mouseX, mouseY);

        if (!showing) {
            if (hovered) alphaAnimation.update(true);
            else alphaAnimation.update(module.isToggled());
            toggleAnimation.update(module.isToggled());
        }

        openAnimation.update(open);
        showingAnimation.update(showing);

        // Бэкграунд строки модуля (тонкий)
        Render2D.drawStyledRect(context.getMatrices(),
                x + 0.5f, y + 0.5f,
                width - 1f, height - 1f,
                2.5f,
                new Color(0, 0, 0, module.isToggled() ? (90 + (int)(80 * alphaAnimation.getValue())) : 45),
                255
        );

        // Иконка включения справа (анимация прозрачности)
        Render2D.drawFont(context.getMatrices(), Fonts.ICONS.getFont(11f),
                "D", x + width - 18f, y + 5f,
                new Color(255, 255, 255, (int) (255 * toggleAnimation.getValue())));

        // Текст: либо биндинг, либо имя модуля
        if (binding) {
            Render2D.drawFont(context.getMatrices(), Fonts.REGULAR.getFont(8f),
                    "Key: " + module.getBind().toString().replace("_", " "),
                    x + 7f, y + 5f,
                    new Color(255, 255, 255, (int) (200 + (55 * alphaAnimation.getValue()))));
        } else {
            Render2D.drawFont(context.getMatrices(), Fonts.REGULAR.getFont(8f),
                    module.getName(),
                    x + 7f + (3f * toggleAnimation.getValue()),
                    y + 5f,
                    showingAnimation.getValue() > 0.2f
                            ? new Color(255, 255, 255, (int) (255 * showingAnimation.getReversedValue()))
                            : new Color(255, 255, 255, (int) (200 + (55 * alphaAnimation.getValue()))));
        }

        // Вынос бинда влево при удержании ALT (как подсказка)
        Render2D.drawFont(context.getMatrices(),
                Fonts.REGULAR.getFont(8f),
                "Key: " + module.getBind().toString().replace("_", " "),
                x + 7f - ((Fonts.REGULAR.getWidth("Key: " + module.getBind().toString().replace("_", " "), 8f) + 10f) * showingAnimation.getReversedValue()),
                y + 5f,
                Color.WHITE
        );

        // Низкая разделительная линия между элементами
        Render2D.drawStyledRect(context.getMatrices(), x, y + height - 1, width, 1, 1f, new Color(255, 255, 255, 25), 255);

        // Хинт в описании
        if (hovered) {
            DrugHack.getInstance().getClickGui().setDescription(I18n.translate(module.getDescription()));
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtils.isHovered(x, y, width, height, (float) mouseX, (float) mouseY)) {
            if (button == 0 && !binding) {
                module.toggle();
            } else if (button == 1 && !components.isEmpty() && !binding) {
                open = !open;
            } else if (button == 2 && !binding) {
                binding = true;
                return;
            }
        }

        if (binding) {
            // Если кликаем мышью — устанавливаем бин мыши
            module.setBind(new Bind(button, true));
            binding = false;
        }

        if (openAnimation.getValue() > 0) {
            for (Component component : components) {
                component.mouseClicked(mouseX, mouseY, button);
            }
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (openAnimation.getValue() > 0) {
            for (Component component : components) {
                component.mouseReleased(mouseX, mouseY, button);
            }
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (binding) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE) {
                module.setBind(new Bind(-1, false));
            } else {
                module.setBind(new Bind(keyCode, false));
            }
            binding = false;
        }

        if (openAnimation.getValue() > 0) {
            for (Component component : components) {
                component.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }

    @Override
    public void keyReleased(int keyCode, int scanCode, int modifiers) {
        if (openAnimation.getValue() > 0) {
            for (Component component : components) {
                component.keyReleased(keyCode, scanCode, modifiers);
            }
        }
    }

    @Override
    public void charTyped(char chr, int modifiers) {
        if (openAnimation.getValue() > 0) {
            for (Component component : components) {
                component.charTyped(chr, modifiers);
            }
        }
    }

    public Module getModule() {
        return module;
    }
}