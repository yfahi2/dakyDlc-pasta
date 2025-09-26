package fun.drughack.hud.impl;

import fun.drughack.DrugHack;
import fun.drughack.api.events.impl.EventRender2D;
import fun.drughack.api.render.builders.Builder;
import fun.drughack.api.render.builders.states.QuadColorState;
import fun.drughack.api.render.builders.states.QuadRadiusState;
import fun.drughack.api.render.builders.states.SizeState;
import fun.drughack.hud.HudElement;
import fun.drughack.modules.impl.combat.Aura;
import fun.drughack.modules.impl.combat.LegitAura;
import fun.drughack.modules.settings.impl.BooleanSetting;
import fun.drughack.utils.animations.Animation;
import fun.drughack.utils.animations.Easing;
import fun.drughack.utils.animations.infinity.InfinityAnimation;
import fun.drughack.utils.math.MathUtils;
import fun.drughack.utils.network.Server;
import fun.drughack.utils.render.BetterColor;
import fun.drughack.utils.render.Render2D;
import fun.drughack.utils.render.fonts.Fonts;
import fun.drughack.utils.world.ServerUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.List;

public class TargetHud extends HudElement {

    public TargetHud() {
        super("TargetHud");
    }

    private final InfinityAnimation healthAnimation = new InfinityAnimation(Easing.LINEAR);
    private final InfinityAnimation gappleAnimation = new InfinityAnimation(Easing.LINEAR);
    private final Animation toggledAnimation = new Animation(300, 1f, true, Easing.LINEAR);
    private final BooleanSetting setting1 = new BooleanSetting("Рисовать цвет от хп", true);

    @Override
    public void onRender2D(EventRender2D e) {
        if (fullNullCheck() || closed()) return;

        final Color cFill = new Color(0, 0, 0, 255);
        final Color cBorder = new Color(130, 130, 180, 235);
        final Color cText = Color.WHITE;

        DrawContext context = e.getContext();
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        Aura aura = DrugHack.getInstance().getModuleManager().getModule(Aura.class);
        LegitAura aura1 = DrugHack.getInstance().getModuleManager().getModule(LegitAura.class);

        LivingEntity target = mc.currentScreen instanceof ChatScreen
                ? mc.player
                : (aura.getTarget() != null ? aura.getTarget() : aura1.getTarget());

        float x = getX();
        float y = getY();

        // Базовые минимальные габариты (авто)
        float minW = 120f;
        float minH = 50f;

        float padding = 5.3f;
        float headSize = 34f;
        float fontSize = 8f;

        // Если HUD уже имеет вручную установленный размер — используем его; иначе — авто-минимум
        float drawW = getWidth() > 0 ? getWidth() : minW;
        float drawH = getHeight() > 0 ? getHeight() : minH;

        // Фон + обводка (опак фон)
        Render2D.drawRoundedRect(context.getMatrices(), x, y, drawW, drawH, 8f, cFill);
        Render2D.drawBorder(context.getMatrices(), x, y, drawW, drawH, 8f, 1.0f, 1.0f, cBorder);

        // Если нет цели — показываем плейсхолдер и границы для драга/ресайза
        if (target == null) {
            Render2D.drawFont(context.getMatrices(),
                    Fonts.SFPROSTEXT.getFont(fontSize),
                    "Target",
                    x + padding,
                    y + padding * 1.6f - padding / 2f,
                    cText
            );

            // Обновляем рамки на случай первого показа
            setBounds(getX(), getY(), drawW, drawH);
            super.onRender2D(e);
            return;
        }

        // Иконки экипировки цели (если игрок)
        if (target instanceof PlayerEntity player) {
            float offset = 0f;
            List<ItemStack> armor = player.getInventory().armor;
            for (ItemStack stack : new ItemStack[]{
                    armor.get(3), armor.get(2), armor.get(1), armor.get(0),
                    player.getOffHandStack(), player.getMainHandStack()
            }) {
                if (stack.isEmpty()) continue;
                context.getMatrices().push();
                context.getMatrices().scale(0.75f, 0.75f, 0.75f);
                context.drawItem(stack,
                        (int) ((x + drawW - offset - padding * 3.75f) / 0.75f),
                        (int) ((y - padding * 2.5f) / 0.75f));
                context.getMatrices().pop();
                offset += 12f;
            }
        }

        float hp = MathUtils.round(Server.getHealth(target, false));
        float maxHp = MathUtils.round(target.getMaxHealth());
        float gappleHp = MathUtils.round(target.getAbsorptionAmount());

        float healthPercent = hp / Math.max(1f, maxHp);
        float gapplePercent = gappleHp / Math.max(1f, maxHp);

        // Привязываем зону рендера к текущей панели (клип)
        Render2D.startScissor(e.getContext(), x, y, drawW, drawH);

        // ГОЛОВА цели (как в рабочей версии)
        float headX = x + padding;
        float headY = y + padding;

        if (target instanceof PlayerEntity) {
            Render2D.drawTexture(
                    context.getMatrices(),
                    headX,
                    headY - 1,
                    headSize + 1,
                    headSize + 1,
                    6f,
                    0.125f, // u
                    0.125f, // v
                    0.125f, // selW
                    0.125f, // selH
                    ((AbstractClientPlayerEntity) target).getSkinTextures().texture(),
                    cText
            );
        } else {
            Render2D.drawRoundedRect(
                    context.getMatrices(),
                    headX,
                    headY - 1,
                    headSize + 1,
                    headSize + 1,
                    6f,
                    new Color(20, 20, 20)
            );
            Render2D.drawFont(
                    context.getMatrices(),
                    Fonts.BOLD.getFont(9f),
                    "?",
                    headX + (headSize / 2f) - Fonts.BOLD.getWidth("?", 9f) / 2f,
                    headY + (headSize / 2f) - Fonts.BOLD.getHeight(9f) / 2f,
                    Color.RED
            );
        }

        // Имя цели
        if (!target.getName().getString().isEmpty()) {
            Render2D.drawFont(
                    context.getMatrices(),
                    Fonts.SFPROSTEXT.getFont(fontSize),
                    target.getName().getString(),
                    headX + headSize + padding,
                    y + padding * 1.6f - padding / 2f,
                    cText
            );
        }

        // Отрисовка HP
        String hpText;
        if (!ServerUtil.isFuntime()) {
            hpText = "HP: " + MathUtils.round(Server.getHealth(target, false))
                    + (target.getAbsorptionAmount() > 0 ? " (%s)".formatted(MathUtils.round(target.getAbsorptionAmount())) : "");
        } else {
            float rounded = MathUtils.round(Server.getHealth(target, false));
            hpText = "HP: " + (rounded >= 1000 ? "?" : rounded);
        }
        Render2D.drawFont(
                context.getMatrices(),
                Fonts.REGULAR.getFont(fontSize),
                hpText,
                headX + headSize + padding,
                y + drawH - 23f - padding * 2,
                cText
        );

        // Полоса HP (адаптация к ширине панели)
        float barStartX = headX + headSize + padding;
        float barY = y + drawH - 18f - padding;
        float maxBarEndX = x + drawW - 5f;
        float barWidth = (drawW - padding * 2) - (barStartX - x);
        if (barStartX + barWidth > maxBarEndX) {
            barWidth = Math.max(10f, maxBarEndX - barStartX);
        }

        float targetHealthW = barWidth * healthPercent;
        float targetGappleW = barWidth * gapplePercent;

        float healthWidth = Math.min(barWidth, (float) healthAnimation.animate(targetHealthW, 200));
        float gappleWidth = Math.min(barWidth, (float) gappleAnimation.animate(targetGappleW, 200));

        // Фон полосы
        Render2D.drawRoundedRect(
                context.getMatrices(),
                barStartX,
                barY,
                barWidth,
                9.4f,
                2.7f,
                new Color(23, 23, 23)
        );

        // Градиент HP
        if (setting1.getValue()) {
            Builder.rectangle()
                    .size(new SizeState(healthWidth, 9.4f))
                    .color(new QuadColorState(
                            BetterColor.darkenWithAlpha(new Color(244, 0, 0).getRGB(), 0.12f),
                            BetterColor.darkenWithAlpha(new Color(244, 147, 0).getRGB(), 0.12f),
                            new Color(49, 172, 36).getRGB(),
                            new Color(29, 202, 9, 0).getRGB()))
                    .radius(new QuadRadiusState(2.7f))
                    .smoothness(0.9f)
                    .smoothness(1f)
                    .build()
                    .render(matrix, barStartX, barY);
        } else {
            Builder.rectangle()
                    .size(new SizeState(healthWidth, 9.4f))
                    .color(new QuadColorState(
                            BetterColor.darkenWithAlpha(new Color(138, 4, 209).getRGB(), 0.12f),
                            BetterColor.darkenWithAlpha(new Color(138, 4, 209).getRGB(), 0.12f),
                            new Color(75, 97, 163).getRGB(),
                            new Color(75, 97, 163).getRGB()))
                    .radius(new QuadRadiusState(2.7f))
                    .smoothness(0.9f)
                    .smoothness(1f)
                    .build()
                    .render(matrix, barStartX, barY);
        }

        // Абсорб поверх
        if (!ServerUtil.isFuntime()) {
            Render2D.drawRoundedRect(
                    context.getMatrices(),
                    barStartX,
                    barY,
                    gappleWidth,
                    9.5f,
                    2.7f,
                    new Color(255, 200, 0)
            );
        }

        Render2D.stopScissor(e.getContext());

        // Обновляем границы панели финальными габаритами
        setBounds(getX(), getY(), drawW, drawH);
        super.onRender2D(e);
    }
}