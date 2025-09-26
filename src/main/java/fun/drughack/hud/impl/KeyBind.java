package fun.drughack.hud.impl;

import fun.drughack.DrugHack;
import fun.drughack.api.events.impl.EventRender2D;
import fun.drughack.hud.HudElement;
import fun.drughack.modules.api.Module;
import fun.drughack.utils.other.StringUtil;
import fun.drughack.utils.render.Render2D;
import fun.drughack.utils.render.fonts.Font;
import fun.drughack.utils.render.fonts.Fonts;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class KeyBind extends HudElement {

    public KeyBind() {
        super("KeyBind");
    }

    static final class Row {
        final String name;
        final String bind;
        Row(String name, String bind) { this.name = name; this.bind = bind; }
    }

    @Override
    public void onRender2D(EventRender2D e) {
        if (fullNullCheck() || closed()) return;

        MatrixStack ms = e.getContext().getMatrices();

        float x = getX();
        float y = getY();

        // Метрики
        float fontSize = 7.6f;
        float padding = 5f;
        float headerIconSize = 10f;
        float cornerRadius = 5f;

        Font sf = Fonts.SFPROSTEXT; // основной текст
        Font icons = Fonts.nur;     // иконки ("C" — клавиатура)

        // Цвета (опак фон)
        Color cFill = new Color(0, 0, 0, 255);
        Color cBorder = new Color(130, 130, 180, 235);
        Color cDivider = cBorder;
        Color cIconC = new Color(205, 200, 255, 255);
        Color cText = Color.WHITE;

        String header = "Hotkeys";

        // Сбор строк
        List<Module> modules = DrugHack.getInstance().getModuleManager().getModules();
        List<Row> rows = new ArrayList<>();
        for (Module module : modules) {
            if (module.isToggled() && !module.getBind().isEmpty()) {
                String name = module.getName();
                String bindStr = StringUtil.shortenBindName(module.getBind().toString());
                rows.add(new Row(name, bindStr));
            }
        }

        // Размеры (авто)
        float iconCW = icons.getWidth("C", headerIconSize);
        float headerTextSize = fontSize + 0.45f;
        float headerW = sf.getWidth(header, headerTextSize);
        float headerBlockW = padding + iconCW + 4f + headerW + padding;

        float autoWidth = Math.max(80f, headerBlockW);
        for (Row r : rows) {
            float w = padding + sf.getWidth(r.name, fontSize) + 12f + sf.getWidth(r.bind, fontSize) + padding;
            if (w > autoWidth) autoWidth = w;
        }

        float headerHeight = fontSize + padding * 2f;
        float rowHeight = fontSize + padding * 1.5f;

        float dividerH = 1.5f;
        float dividerGap = 3f;
        float verticalSpacingRows = 1f;

        float contentHeight = rows.size() * (rowHeight + verticalSpacingRows);
        if (!rows.isEmpty()) contentHeight -= verticalSpacingRows;

        float autoHeight = headerHeight + dividerH + dividerGap + contentHeight + 2.5f;

        // Если элемент уже имеет ручной размер — используем его, иначе авто
        float drawW = getWidth() > 0 ? getWidth() : autoWidth;
        float drawH = getHeight() > 0 ? getHeight() : autoHeight;

        // Фон + обводка
        Render2D.drawRoundedRect(ms, x, y, drawW, drawH, cornerRadius, cFill);
        Render2D.drawBorder(ms, x, y, drawW, drawH, cornerRadius, 1.0f, 1.0f, cBorder);

        // Шапка: иконка "C" и текст
        float headerTextY = y + padding + 0.5f;
        float iconCX = x + padding;
        float iconCY = headerTextY + (headerTextSize - headerIconSize) / 2f;
        Render2D.drawFont(ms, icons.getFont(headerIconSize), "C", iconCX, iconCY, cIconC);

        float headerTextX = iconCX + iconCW + 4f;
        Render2D.drawFont(ms, sf.getFont(headerTextSize), header, headerTextX, headerTextY, cText);

        // Разделитель
        float lineTop = y + headerHeight;
        Render2D.drawRoundedRect(ms, x + 0.5f, lineTop, Math.max(1f, drawW - 1f), dividerH, 0f, cDivider);

        // Область списка — клип
        float listX = x;
        float listY = lineTop + dividerH + dividerGap;
        float listH = Math.max(0f, drawH - (listY - y));
        Render2D.startScissor(e.getContext(), listX, listY, drawW, listH);

        // Список биндов
        float curY = listY;
        for (Row r : rows) {
            float textY = curY + padding / 1.5f;

            // Имя слева
            Render2D.drawFont(ms, sf.getFont(fontSize), r.name, x + padding, textY, cText);

            // Бинд справа
            float bindW = sf.getWidth(r.bind, fontSize);
            float bindX = x + drawW - padding - bindW;
            Render2D.drawFont(ms, sf.getFont(fontSize), r.bind, bindX, textY, cText);

            curY += rowHeight + verticalSpacingRows;
            if (curY > y + drawH) break; // не рисуем за пределами видимой области
        }

        Render2D.stopScissor(e.getContext());

        // Сообщаем рамки (если не задано — установится авто)
        setBounds(x, y, drawW, drawH);
        super.onRender2D(e);
    }
}