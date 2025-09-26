package fun.drughack.hud.impl;

import fun.drughack.api.events.impl.EventRender2D;
import fun.drughack.hud.HudElement;
import fun.drughack.utils.math.MathUtils;
import fun.drughack.utils.render.Render2D;
import fun.drughack.utils.render.fonts.Font;
import fun.drughack.utils.render.fonts.Fonts;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class CooldownList extends HudElement {

    public CooldownList() {
        super("CooldownList");
    }

    @Override
    public void onRender2D(EventRender2D e) {
        if (fullNullCheck() || closed()) return;

        float scale = 1.0f / 1.05f;

        float x = getX();
        float y = getY();
        float padding = 6f * scale;
        float fontSize = 7.2f * scale;
        float headerTextSize = fontSize + 1.4f;
        float iconDrawSize = 11f * scale;
        float iconGap = 3f * scale;
        float cornerRadius = 3f * scale;

        Font font = Fonts.SFPD;

        String header = "Item Cooldowns";

        float itemHeight = fontSize + padding * 1.7f;
        float headerHeight = headerTextSize + padding * 2f;

        float verticalSpacingRows = 1f * scale;
        float dividerH = 1.5f * scale;
        float dividerGap = 3f * scale;

        PlayerInventory inventory = mc.player.getInventory();
        ItemCooldownManager cooldownManager = mc.player.getItemCooldownManager();

        List<ItemStack> cooldownItems = inventory.main.stream()
                .filter(stack -> !stack.isEmpty())
                .filter(cooldownManager::isCoolingDown)
                .collect(Collectors.toList());

        if (cooldownItems.isEmpty()) return;

        // Цвета (опак фон!)
        Color cFill = new Color(0, 0, 0, 255);
        Color cBorder = new Color(130, 130, 180, 235);
        Color cDivider = cBorder;
        Color cText = Color.WHITE;

        // Авторазмер
        float minWidth = 90f * scale;
        float autoWidth = Math.max(minWidth, font.getWidth(header, headerTextSize) + padding * 2f);

        for (ItemStack stack : cooldownItems) {
            String itemName = stack.getName().getString();
            float progress = cooldownManager.getCooldownProgress(stack, MathUtils.getTickDelta());
            int seconds = (int) (progress * 10f);

            String leftText = itemName;
            String rightText = seconds + "s";

            float leftWidth = font.getWidth(leftText, fontSize);
            float rightWidth = font.getWidth(rightText, fontSize);

            float rowWidth = padding
                    + iconDrawSize + iconGap
                    + leftWidth
                    + padding
                    + rightWidth
                    + padding;

            if (rowWidth > autoWidth) autoWidth = rowWidth;
        }

        float contentHeight = cooldownItems.size() * (itemHeight + verticalSpacingRows);
        if (!cooldownItems.isEmpty()) contentHeight -= verticalSpacingRows;
        float autoHeight = headerHeight + dividerH + dividerGap + contentHeight + 3.0f * scale;

        // Если элемент уже имеет ручной размер — используем его
        float drawW = getWidth() > 0 ? getWidth() : autoWidth;
        float drawH = getHeight() > 0 ? getHeight() : autoHeight;

        MatrixStack ms = e.getContext().getMatrices();

        // ФОН (непрозрачный) → ОБВОДКА
        Render2D.drawRoundedRect(ms, x, y, drawW, drawH, cornerRadius, cFill);
        Render2D.drawBorder(ms, x, y, drawW, drawH, cornerRadius, 1.0f, 1.0f, cBorder);

        // Шапка
        float headerY = y + (headerHeight - headerTextSize) / 2f;
        Render2D.drawFont(ms, font.getFont(headerTextSize), header, x + padding, headerY, cText);

        // Разделитель
        float lineTop = y + headerHeight;
        Render2D.drawRoundedRect(ms, x + 0.5f, lineTop, Math.max(1f, drawW - 1f), dividerH, 0f, cDivider);

        // Область списка — клип, чтобы фон был за текстом и ничего не вылезало
        float listX = x;
        float listY = lineTop + dividerH + dividerGap;
        float listH = Math.max(0f, drawH - (listY - y));

        Render2D.startScissor(e.getContext(), listX, listY, drawW, listH);

        // Список кулдаунов
        float currentY = listY;
        for (ItemStack stack : cooldownItems) {
            String itemName = stack.getName().getString();
            float progress = cooldownManager.getCooldownProgress(stack, MathUtils.getTickDelta());
            int seconds = (int) (progress * 10f);

            String leftText = itemName;
            String rightText = seconds + "s";

            // Иконка
            float iconX = x + padding;
            float iconY = currentY + (itemHeight - iconDrawSize) / 2f;

            ms.push();
            ms.translate(iconX, iconY, 0);
            float scaleIcon = iconDrawSize / 16f;
            ms.scale(scaleIcon, scaleIcon, 1f);
            e.getContext().drawItem(stack, 0, 0);
            ms.pop();

            // Тексты
            float textLeftX = x + padding + iconDrawSize + iconGap;
            float textY = currentY + (itemHeight - fontSize) / 2f;

            Color textColor = Color.WHITE;
            Color cdColor = seconds <= 3 ? new Color(255, 0, 0, 200) : Color.WHITE;

            Render2D.drawFont(ms, font.getFont(fontSize), leftText, textLeftX, textY, textColor);

            float rightWidth = font.getWidth(rightText, fontSize);
            float rightX = x + drawW - padding - rightWidth;
            Render2D.drawFont(ms, font.getFont(fontSize), rightText, rightX, textY, cdColor);

            currentY += itemHeight + verticalSpacingRows;
            if (currentY > y + drawH) break; // не рисуем за пределами клипа
        }

        Render2D.stopScissor(e.getContext());

        // Сообщаем рамки (если не был установлен — установится авто)
        setBounds(getX(), getY(), drawW, drawH);
        super.onRender2D(e);
    }
}