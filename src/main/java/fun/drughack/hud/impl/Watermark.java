package fun.drughack.hud.impl;

import fun.drughack.DrugHack;
import fun.drughack.api.events.impl.EventRender2D;
import fun.drughack.hud.HudElement;
import fun.drughack.modules.api.Module;
import fun.drughack.utils.render.Render2D;
import fun.drughack.utils.render.fonts.Font;
import fun.drughack.utils.render.fonts.Fonts;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Watermark extends HudElement {

    public Watermark() {
        super("Watermark");
    }

    @Override
    public void onRender2D(EventRender2D e) {
        if (fullNullCheck() || closed()) return;

        MatrixStack ms = e.getContext().getMatrices();

        float x = getX();
        float y = getY();

        // Параметры
        float padding = 6f;
        float fontSize = 7.6f;
        float headerTextSize = fontSize + 0.3f;
        float minPanelHeight = 18f;  // базовая высота
        float cornerRadius = 3f;

        Font sf = Fonts.SFPROSTEXT;

        // Цвета — опак фон, светлая обводка, белый текст
        Color cFill = new Color(0, 0, 0, 255);
        Color cBorder = new Color(130, 130, 180, 235);
        Color cDivider = cBorder;
        Color cText = Color.WHITE;

        // Контент
        String firstText = "LiquidPlus";
        String fpsText = mc.getCurrentFps() + " fps";

        // (оставлено для совместимости; не используется)
        List<Module> modules = DrugHack.getInstance().getModuleManager().getModules();
        List<String> dummy = new ArrayList<>();
        for (Module module : modules) {
            if (module.isToggled() && !module.getBind().isEmpty()) {
                dummy.add(module.getName());
            }
        }

        // Ширины
        float firstTextW = sf.getWidth(firstText, headerTextSize);
        float fpsTextW = sf.getWidth(fpsText, headerTextSize);

        // Вертикальный разделитель между блоками
        float sepW = 1.5f;
        float gapAroundSep = 6f;

        float contentWidth = firstTextW + gapAroundSep + sepW + gapAroundSep + fpsTextW;

        // Авторазмер
        float autoPanelWidth = contentWidth + padding * 2f;
        float autoPanelHeight = Math.max(minPanelHeight, headerTextSize + padding * 2f);

        // Если элемент уже имеет ручной размер — используем его
        float drawW = getWidth() > 0 ? getWidth() : autoPanelWidth;
        float drawH = getHeight() > 0 ? getHeight() : autoPanelHeight;

        // Фон и обводка
        Render2D.drawRoundedRect(ms, x, y, drawW, drawH, cornerRadius, cFill);
        Render2D.drawBorder(ms, x, y, drawW, drawH, cornerRadius, 1.0f, 1.0f, cBorder);

        // Центрирование по горизонтали и вертикали
        float startX = x + (drawW - contentWidth) / 2f;
        float textY = y + (drawH - headerTextSize) / 2f;

        // Рисуем первый текст
        float xCursor = startX;
        Render2D.drawFont(ms, sf.getFont(headerTextSize), firstText, xCursor, textY, cText);
        xCursor += firstTextW + gapAroundSep;

        // Разделитель
        float sepH = drawH - padding * 1.6f;
        sepH = Math.max(4f, sepH); // минимальная высота разделителя
        float sepX = xCursor;
        float sepY = y + (drawH - sepH) / 2f;
        Render2D.drawRoundedRect(ms, sepX, sepY, sepW, sepH, 0f, cDivider);
        xCursor += sepW + gapAroundSep;

        // Второй текст (FPS)
        Render2D.drawFont(ms, sf.getFont(headerTextSize), fpsText, xCursor, textY, cText);

        // Сообщаем рамки (если впервые — установится авто, далее может быть залочен пользователем)
        setBounds(x, y, drawW, drawH);
    }
}