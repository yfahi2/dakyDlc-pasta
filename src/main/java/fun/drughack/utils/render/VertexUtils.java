package fun.drughack.utils.render;

import net.minecraft.client.render.*;
import org.joml.Matrix4f;

import java.util.OptionalDouble;

import static net.minecraft.client.render.RenderPhase.*;
import static net.minecraft.client.render.RenderPhase.ALL_MASK;
import static net.minecraft.client.render.RenderPhase.ALWAYS_DEPTH_TEST;
import static net.minecraft.client.render.RenderPhase.DISABLE_CULLING;
import static net.minecraft.client.render.RenderPhase.ITEM_ENTITY_TARGET;
import static net.minecraft.client.render.RenderPhase.LINES_PROGRAM;
import static net.minecraft.client.render.RenderPhase.TRANSLUCENT_TRANSPARENCY;
import static net.minecraft.client.render.RenderPhase.VIEW_OFFSET_Z_LAYERING;

public class VertexUtils {

    public static final RenderLayer.MultiPhase IMAGE = RenderLayer.of("image",
            VertexFormats.POSITION_TEXTURE_COLOR,
            VertexFormat.DrawMode.QUADS,
            1536,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(POSITION_TEXTURE_COLOR_PROGRAM)
                    .layering(VIEW_OFFSET_Z_LAYERING)
                    .transparency(TRANSLUCENT_TRANSPARENCY)
                    .target(ITEM_ENTITY_TARGET)
                    .writeMaskState(ALL_MASK)
                    .depthTest(ALWAYS_DEPTH_TEST)
                    .cull(DISABLE_CULLING)
                    .build(false));

    public static final RenderLayer.MultiPhase LINES = RenderLayer.of("lines",
            VertexFormats.LINES,
            VertexFormat.DrawMode.LINES,
            1536,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(LINES_PROGRAM)
                    .lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(2f)))
                    .layering(VIEW_OFFSET_Z_LAYERING)
                    .transparency(TRANSLUCENT_TRANSPARENCY)
                    .target(ITEM_ENTITY_TARGET)
                    .writeMaskState(ALL_MASK)
                    .depthTest(ALWAYS_DEPTH_TEST)
                    .cull(DISABLE_CULLING)
                    .build(false));


    public static void drawImageQuad(VertexConsumer vertices, Matrix4f matrix, float posX, float posY, float posZ, float halfSize, int color) {
        vertices.vertex(matrix, posX - halfSize, posY - halfSize, posZ).texture(0, 1).color(color);
        vertices.vertex(matrix, posX - halfSize, posY + halfSize, posZ).texture(0, 0).color(color);
        vertices.vertex(matrix, posX + halfSize, posY + halfSize, posZ).texture(1, 0).color(color);
        vertices.vertex(matrix, posX + halfSize, posY - halfSize, posZ).texture(1, 1).color(color);
    }

}
