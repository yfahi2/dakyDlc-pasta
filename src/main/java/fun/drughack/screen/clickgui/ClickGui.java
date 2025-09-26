package fun.drughack.screen.clickgui;

import fun.drughack.screen.clickgui.components.Component;
import fun.drughack.screen.clickgui.components.impl.ModuleComponent;
import fun.drughack.screen.clickgui.components.impl.SearchComponent;
import lombok.Setter;
import fun.drughack.DrugHack;
import fun.drughack.modules.api.Category;
import fun.drughack.modules.impl.client.UI;
import fun.drughack.utils.Wrapper;
import fun.drughack.utils.animations.Animation;
import fun.drughack.utils.animations.Easing;
import fun.drughack.utils.render.fonts.Fonts;
import fun.drughack.utils.render.Render2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;


import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClickGui extends Screen implements Wrapper {

    private final List<Panel> panels = new ArrayList<>();
    private final SearchComponent searchComponent = new SearchComponent();
    private final Animation openAnimation = new Animation(300, 1f, true, Easing.BOTH_SINE);
    private final Animation hoverAnimation = new Animation(300, 1f, true, Easing.BOTH_SINE);
    @Setter private String description = "";
    private boolean close = false;

    public ClickGui() {
        super(Text.of("drughack-clickgui"));
        for (Category category : DrugHack.getInstance().getModuleManager().getCategories()) {
            if (category == Category.Hud) continue;
            panels.add(new Panel(-999, -999, 110, 20, category));
        }
    }

    @Override
    public void init() {
        openAnimation.update(true);
        close = false;


        searchComponent.setX(mc.getWindow().getScaledWidth() / 2f - 60f);
        searchComponent.setY(mc.getWindow().getScaledHeight() - 200f);
        searchComponent.setWidth(120f);
        searchComponent.setHeight(15f);
    }

    @Override
    public void tick() {
        float x = (mc.getWindow().getScaledWidth() / 2f) - (110 * ((Category.values().length - 1) / 2f)) - (4f * 1.5f);
        float y = (mc.getWindow().getScaledHeight() / 2f) - 125f;
        for (Panel panel : panels) {
            panel.setX(x);
            panel.setY(y);
            x += 110f + 4f;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (close && openAnimation.getValue() <= 0.01f) {
            DrugHack.getInstance().getModuleManager().getModule(UI.class).setToggled(false);
            super.close();
            return;
        }

        searchComponent.render(context, mouseX, mouseY, delta);
        context.getMatrices().push();
        context.getMatrices().translate(mc.getWindow().getScaledWidth() / 2f, mc.getWindow().getScaledHeight() / 2f, 1f);
        context.getMatrices().scale(openAnimation.getValue(), openAnimation.getValue(), 1f);
        context.getMatrices().translate(-mc.getWindow().getScaledWidth() / 2f, -mc.getWindow().getScaledHeight() / 2f, 0f);

        String filter = searchComponent.getSearchText().toLowerCase();

        for (Panel panel : panels) {

            if (!filter.isEmpty()) {
                List<Component> filtered = panel.getComponents().stream()
                        .filter(c -> c.getName().toLowerCase().contains(filter))
                        .toList();
                if (!filtered.isEmpty()) {
                    panel.renderFiltered(context, mouseX, mouseY, delta, filtered);
                } else {
                    panel.renderFiltered(context, mouseX, mouseY, delta, Collections.emptyList());
                }
            } else {
                panel.render(context, mouseX, mouseY, delta);
            }
        }

        hoverAnimation.update(!description.isEmpty());
        if (!description.isEmpty()) {
            float width = Fonts.MEDIUM.getWidth(description, 9f);
            float x = mc.getWindow().getScaledWidth() / 2f - width / 2f;
            float y = mc.getWindow().getScaledHeight() / 2f - 150f;
            Render2D.drawStyledRect(context.getMatrices(), x - 3f, y, width + 6f, 14f, 1.5f, new Color(0, 0, 0, (int) (175 * hoverAnimation.getValue())), (int) (255 * hoverAnimation.getValue()));
            Render2D.drawFont(context.getMatrices(), Fonts.MEDIUM.getFont(9f), description, x, y + 1.25f, new Color(255, 255, 255, (int) (255 * hoverAnimation.getValue())));
            description = "";
        }

        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        searchComponent.mouseClicked(mouseX, mouseY, button);
        if (close) return false;

        String filter = searchComponent.getSearchText().toLowerCase();

        for (Panel panel : panels) {
            if (filter.isEmpty()) {
                panel.mouseClicked(mouseX, mouseY, button);
            } else {

                List<Component> filtered = panel.getComponents().stream()
                        .filter(c -> c.getName().toLowerCase().contains(filter))
                        .toList();

                panel.mouseClickedFiltered(mouseX, mouseY, button, filtered);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (close) return false;
        for (Panel panel : panels) panel.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (close) return false;
        for (Panel panel : panels) panel.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        searchComponent.keyPressed(keyCode, scanCode, modifiers);
        if (close) return false;
        for (Panel panel : panels) panel.keyPressed(keyCode, scanCode, modifiers);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (close) return false;
        for (Panel panel : panels) panel.keyReleased(keyCode, scanCode, modifiers);
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        searchComponent.charTyped(chr, modifiers);
        if (close) return false;
        for (Panel panel : panels) panel.charTyped(chr, modifiers);
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void close() {
        if (!close) {
            close = true;
            openAnimation.update(false);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}