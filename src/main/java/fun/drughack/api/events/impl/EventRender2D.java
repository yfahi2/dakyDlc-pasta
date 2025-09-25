package fun.drughack.api.events.impl;

import fun.drughack.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

@AllArgsConstructor @Getter
public class EventRender2D extends Event {
    private DrawContext context;
    private RenderTickCounter tickCounter;
}