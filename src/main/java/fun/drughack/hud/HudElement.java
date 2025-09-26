package fun.drughack.hud;

import fun.drughack.DrugHack;
import fun.drughack.api.events.impl.EventKey;
import fun.drughack.api.events.impl.EventMouse;
import fun.drughack.api.events.impl.EventRender2D;
import fun.drughack.hud.windows.Window;
import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import fun.drughack.modules.settings.Setting;
import fun.drughack.modules.settings.api.Position;
import fun.drughack.modules.settings.impl.BooleanSetting;
import fun.drughack.modules.settings.impl.PositionSetting;
import fun.drughack.utils.animations.Animation;
import fun.drughack.utils.animations.Easing;
import fun.drughack.utils.math.MathUtils;
import fun.drughack.utils.notify.Notify;
import fun.drughack.utils.notify.NotifyIcons;
import fun.drughack.utils.render.Render2D;
import lombok.Getter;
import lombok.Setter;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ChatScreen;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class HudElement extends Module {

    private final PositionSetting position = new PositionSetting("Position", new Position(0, 0));
    private final BooleanSetting chatEdit = new BooleanSetting("Chat edit", true);

    private final Animation hoverAnimation = new Animation(300, 1f, false, Easing.SMOOTH_STEP);
    protected final Animation toggledAnimation = new Animation(300, 1f, false, Easing.BOTH_SINE);

    private float dragX, dragY, width, height;
    private boolean dragging, button;

    // resize
    private boolean resizing = false;
    private final float resizePad = 6f;
    private boolean manualSizeLocked = false;

    private final List<Setting<?>> settings = new ArrayList<>();
    private Window window;

    public HudElement(String name) {
        super(name, Category.Hud);
        settings.add(position);
        settings.add(chatEdit);
        setToggled(true);
    }

    @EventHandler
    public void onRender2D(EventRender2D e) {
        if (fullNullCheck()) return;

        boolean hovered = MathUtils.isHovered(getX(), getY(), getWidth(), getHeight(), mouseX(), mouseY());
        hoverAnimation.update((hovered && window == null) || button || dragging || resizing);

        // зоны захвата для ресайза
        boolean onRight = MathUtils.isHovered(getX() + getWidth() - resizePad, getY(), resizePad, getHeight(), mouseX(), mouseY());
        boolean onBottom = MathUtils.isHovered(getX(), getY() + getHeight() - resizePad, getWidth(), resizePad, mouseX(), mouseY());
        boolean onCorner = MathUtils.isHovered(getX() + getWidth() - resizePad, getY() + getHeight() - resizePad, resizePad, resizePad, mouseX(), mouseY());

        if (button && Boolean.TRUE.equals(chatEdit.getValue())) {
            if (!dragging && !resizing && hovered) {
                // выбираем: перетаскивание или ресайз
                if (onCorner || onRight || onBottom) {
                    resizing = true;
                    DrugHack.getInstance().getHudManager().setCurrentDragging(this);
                } else {
                    dragX = mouseX() - getX();
                    dragY = mouseY() - getY();
                    dragging = true;
                    DrugHack.getInstance().getHudManager().setCurrentDragging(this);
                }
            }

            if (resizing) {
                float newW = Math.max(10f, mouseX() - getX());
                float newH = Math.max(10f, mouseY() - getY());
                // кламп по экрану
                float maxW = mc.getWindow().getScaledWidth() - getX();
                float maxH = mc.getWindow().getScaledHeight() - getY();
                newW = Math.min(newW, maxW);
                newH = Math.min(newH, maxH);
                setBounds(getX(), getY(), newW, newH);
            } else if (dragging) {
                float finalX = (float) MathUtils.clamp((double) (mouseX() - dragX), 0.0, (double) (mc.getWindow().getScaledWidth() - width));
                float finalY = (float) MathUtils.clamp((double) (mouseY() - dragY), 0.0, (double) (mc.getWindow().getScaledHeight() - height));
                position.getValue().setX(finalX / mc.getWindow().getScaledWidth());
                position.getValue().setY(finalY / mc.getWindow().getScaledHeight());
            }
        } else {
            dragging = false;
            resizing = false;
        }

        // рамка и маркер уголка в чате
        if (mc.currentScreen instanceof ChatScreen) {
            Render2D.drawBorder(
                    e.getContext().getMatrices(),
                    getX() - 3,
                    getY() - 3,
                    getWidth() + 6,
                    getHeight() + 6,
                    3.5f + (3.5f / 2f),
                    1.25f,
                    1.25f,
                    new Color(255, 255, 255, (int) (255 * hoverAnimation.getValue()))
            );
            // маркер "уголка" для захвата
            Render2D.drawRoundedRect(
                    e.getContext().getMatrices(),
                    getX() + getWidth() - 4f,
                    getY() + getHeight() - 4f,
                    4f,
                    4f,
                    1.5f,
                    new Color(255, 255, 255, 120)
            );
        }

        // окно настроек
        if (window != null) {
            if (!(mc.currentScreen instanceof ChatScreen)) window.reset();

            if (window.closed()) {
                window = null;
                return;
            }

            window.render(e.getContext(), mouseX(), mouseY());
        }
    }

    @EventHandler
    public void onRender2DX2(EventRender2D e) {
        if (fullNullCheck()) return;
        toggledAnimation.update(
                DrugHack.getInstance()
                        .getHudManager()
                        .getElements()
                        .getName("elements.settings.elements." + getName().toLowerCase())
                        .getValue()
        );
    }

    @EventHandler
    public void onMouse(EventMouse e) {
        if (!(mc.currentScreen instanceof ChatScreen) || fullNullCheck()) return;

        if (e.getAction() == 0) {
            // mouse up
            button = false;

            // фиксируем вручную заданный размер, если шёл ресайз
            if (resizing) manualSizeLocked = true;

            dragging = false;
            resizing = false;
            DrugHack.getInstance().getHudManager().setCurrentDragging(null);
        } else if (e.getAction() == 1) {
            // mouse down
            if (e.getButton() == 0
                    && MathUtils.isHovered(getX(), getY(), getWidth(), getHeight(), mouseX(), mouseY())
                    && DrugHack.getInstance().getHudManager().getCurrentDragging() == null) {
                button = true;
            }

            if (window != null) {
                if (MathUtils.isHovered(window.getX(), window.getY(), window.getWidth(), window.getFinalHeight(), mouseX(), mouseY())) {
                    window.mouseClicked(mouseX(), mouseY(), e.getButton());
                    return;
                } else window.reset();
            }

            if (e.getButton() == 1) {
                if (MathUtils.isHovered(getX(), getY(), getWidth(), getHeight(), mouseX(), mouseY())) {

                    if (settings.size() == 1) {
                        if (DrugHack.getInstance().getHudManager().getWindow() != null) DrugHack.getInstance().getHudManager().setWindow(null);
                        DrugHack.getInstance().getNotifyManager().add(
                                new Notify(NotifyIcons.failIcon, "У этого элемента нету настроек!", 1000, Color.red, Color.red)
                        );
                    } else {
                        if (DrugHack.getInstance().getHudManager().getWindow() != null)
                            DrugHack.getInstance().getHudManager().getWindow().reset();
                        for (HudElement element : DrugHack.getInstance().getHudManager().getHudElements()) {
                            if (element.getWindow() == null) continue;
                            element.getWindow().reset();
                        }
                        window = new Window(mouseX() + 3, mouseY() + 3, 100, 12.5f, settings);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onKey(EventKey e) {
        if (!(mc.currentScreen instanceof ChatScreen) || fullNullCheck()) return;

        // Ctrl + U — разблокировать ручной размер (вернуть авто)
        long handle = mc.getWindow().getHandle();
        boolean ctrl = org.lwjgl.glfw.GLFW.glfwGetKey(handle, org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL) == org.lwjgl.glfw.GLFW.GLFW_PRESS
                || org.lwjgl.glfw.GLFW.glfwGetKey(handle, org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL) == org.lwjgl.glfw.GLFW.GLFW_PRESS;

        if (ctrl && e.getKey() == org.lwjgl.glfw.GLFW.GLFW_KEY_U) {
            manualSizeLocked = false;
        }
    }

    public float getX() {
        return mc.getWindow().getScaledWidth() * position.getValue().getX();
    }

    public float getY() {
        return mc.getWindow().getScaledHeight() * position.getValue().getY();
    }

    public int mouseX() {
        return (int) (mc.mouse.getX() / mc.getWindow().getScaleFactor());
    }

    public int mouseY() {
        return (int) (mc.mouse.getY() / mc.getWindow().getScaleFactor());
    }

    public void setBounds(float x, float y, float width, float height) {
        // если размер залочен пользователем и сейчас не идёт активный ресайз — не трогаем width/height
        if (!manualSizeLocked || resizing) {
            this.width = width;
            this.height = height;
        }
        position.getValue().setX(x / mc.getWindow().getScaledWidth());
        position.getValue().setY(y / mc.getWindow().getScaledHeight());
    }

    protected boolean closed() {
        return toggledAnimation.getValue() <= 0f;
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}