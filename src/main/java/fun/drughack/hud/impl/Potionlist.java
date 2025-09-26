package fun.drughack.hud.impl;

import fun.drughack.api.events.impl.EventRender2D;
import fun.drughack.hud.HudElement;
import fun.drughack.utils.other.StringUtil;
import fun.drughack.utils.render.Render2D;
import fun.drughack.utils.render.fonts.Font;
import fun.drughack.utils.render.fonts.Fonts;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Potionlist extends HudElement {

    public Potionlist() {
        super("Potionlist");
    }

    @Override
    public void onRender2D(EventRender2D e) {
        if (fullNullCheck() || closed()) return;

        MatrixStack ms = e.getContext().getMatrices();

        float x = getX();
        float y = getY();

        float scale = 1.0f / 1.05f;
        float padding = 6f * scale;
        float fontSize = 7.6f * scale;
        float headerIconSize = 11f * scale;
        float iconDrawSize = 11f * scale;
        float iconGap = 3f * scale;
        float cornerRadius = 3f * scale;

        Font font = Fonts.SFPD;
        Font fontIcon = Fonts.nur;

        // ОПАК фон и светлая обводка
        Color cFill = new Color(0, 0, 0, 255);
        Color cBorder = new Color(130, 130, 180, 235);
        Color cDivider = cBorder;
        Color cText = Color.WHITE;

        String header = "Active Potions";

        Collection<StatusEffectInstance> effectsCol = mc.player.getStatusEffects();
        List<StatusEffectInstance> effects = new ArrayList<>(effectsCol);

        float headerTextSize = fontSize + 1.6f;
        float headerHeight = headerTextSize + padding * 2f;
        float itemHeight = fontSize + padding * 1.7f;
        float verticalSpacingRows = 1f * scale;

        float dividerH = 1.5f * scale;
        float dividerGap = 3f * scale;

        float headerIconW = fontIcon.getWidth("E", headerIconSize);
        float headerBlockWidth = padding + headerIconW + 4f * scale + font.getWidth(header, headerTextSize) + padding;

        float minWidth = 90f * scale;
        float autoWidth = Math.max(minWidth, headerBlockWidth);

        for (StatusEffectInstance effect : effects) {
            StatusEffect type = effect.getEffectType().value();
            String effectName = I18n.translate(type.getTranslationKey());
            String level = effect.getAmplifier() + 1 > 1 ? StringUtil.toRomanNumeral(effect.getAmplifier() + 1) : "";
            String duration = StringUtil.formatTicks(effect.getDuration());

            String textLeft = effectName + (level.isEmpty() ? "" : " " + level);
            String textRight = duration;

            float leftWidth = font.getWidth(textLeft, fontSize);
            float rightWidth = font.getWidth(textRight, fontSize);

            float rowWidth = padding + iconDrawSize + iconGap + leftWidth + padding + rightWidth + padding;
            if (rowWidth > autoWidth) autoWidth = rowWidth;
        }

        float contentHeight = effects.size() * (itemHeight + verticalSpacingRows);
        if (!effects.isEmpty()) contentHeight -= verticalSpacingRows;
        float autoHeight = headerHeight + dividerH + dividerGap + contentHeight + 3.0f * scale;

        // Если HudElement залочен на ручной размер — используем его, иначе авто
        float drawW = getWidth() > 0 ? getWidth() : autoWidth;
        float drawH = getHeight() > 0 ? getHeight() : autoHeight;

        // Фон и обводка — СПЕРВА фон (непрозрачный), затем рамка
        Render2D.drawRoundedRect(ms, x, y, drawW, drawH, cornerRadius, cFill);
        Render2D.drawBorder(ms, x, y, drawW, drawH, cornerRadius, 1.0f, 1.0f, cBorder);

        // Заголовок (иконка + текст)
        float headerY = y + (headerHeight - headerTextSize) / 2f;
        float iconEX = x + padding;
        float iconEY = y + (headerHeight - headerIconSize) / 2f;
        Render2D.drawFont(ms, fontIcon.getFont(headerIconSize), "E", iconEX, iconEY, cText);

        float headerTextX = iconEX + headerIconW + 4f * scale;
        Render2D.drawFont(ms, font.getFont(headerTextSize), header, headerTextX, headerY, cText);

        // Разделитель под шапкой
        float lineTop = y + headerHeight;
        Render2D.drawRoundedRect(ms, x + 0.5f, lineTop, Math.max(1f, drawW - 1f), dividerH, 0f, cDivider);

        // Область списка — клип
        float listX = x;
        float listY = lineTop + dividerH + dividerGap;
        float listH = Math.max(0f, drawH - (listY - y));

        Render2D.startScissor(e.getContext(), listX, listY, drawW, listH);

        // Рисуем эффекты с прижатием к правому краю внутри drawW
        float currentY = listY;
        for (StatusEffectInstance effect : effects) {
            StatusEffect type = effect.getEffectType().value();

            String effectName = I18n.translate(type.getTranslationKey());
            String level = effect.getAmplifier() + 1 > 1 ? StringUtil.toRomanNumeral(effect.getAmplifier() + 1) : "";
            String duration = StringUtil.formatTicks(effect.getDuration());

            String textLeft = effectName + (level.isEmpty() ? "" : " " + level);
            String textRight = duration;

            StatusEffectSpriteManager statusEffectSpriteManager = mc.getStatusEffectSpriteManager();
            RegistryEntry<StatusEffect> registryEntry = effect.getEffectType();
            Sprite sprite = statusEffectSpriteManager.getSprite(registryEntry);
            e.getContext().drawSpriteStretched(RenderLayer::getGuiTextured, sprite,
                    (int) (x + padding),
                    (int) (currentY + (itemHeight - iconDrawSize) / 2f),
                    (int) iconDrawSize, (int) iconDrawSize
            );

            float textLeftX = x + padding + iconDrawSize + iconGap;
            float textY = currentY + (itemHeight - fontSize) / 2f;

            Color textColor = isBadEffect(type) ? Color.RED : Color.WHITE;
            int durationSeconds = effect.getDuration() / 20;
            Color durationColor = durationSeconds < 10
                    ? new Color(166, 26, 26, 200)
                    : durationSeconds < 30
                    ? new Color(255, 200, 0, 200)
                    : Color.WHITE;

            Render2D.drawFont(ms, font.getFont(fontSize), textLeft, textLeftX, textY, textColor);

            float rightWidth = font.getWidth(textRight, fontSize);
            float rightX = x + drawW - padding - rightWidth;
            Render2D.drawFont(ms, font.getFont(fontSize), textRight, rightX, textY, durationColor);

            currentY += itemHeight + verticalSpacingRows;
            if (currentY > y + drawH) break;
        }

        Render2D.stopScissor(e.getContext());

        // Сообщаем рамки (если впервые — установится авторазмер; дальше HUD может быть залочен на ручной)
        setBounds(getX(), getY(), drawW, drawH);

        super.onRender2D(e);
    }

    public static boolean isBadEffect(StatusEffect effect) {
        return effect.getCategory() == StatusEffectCategory.HARMFUL;
    }
}