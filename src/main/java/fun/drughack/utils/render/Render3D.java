package fun.drughack.utils.render;

import fun.drughack.DrugHack;
import fun.drughack.api.mixins.accessors.IWorldRenderer;
import fun.drughack.hud.HudElement;
import fun.drughack.utils.Wrapper;
import lombok.experimental.UtilityClass;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class Render3D implements Wrapper {

    public boolean rendering = false;

    public List<VertexCollection> QUADS = new ArrayList<>();
    public List<VertexCollection> DEBUG_LINES = new ArrayList<>();
    public List<VertexCollection> SHINE_QUADS = new ArrayList<>();
    public List<VertexCollection> SHINE_DEBUG_LINES = new ArrayList<>();
    public final List<Line> LINE_DEPTH = new ArrayList<>();
    public final List<Line> LINE = new ArrayList<>();
    public final List<Quad> QUAD_DEPTH = new ArrayList<>();
    public final List<Quad> QUAD = new ArrayList<>();

    public void renderCube(MatrixStack matrices, Vec3d vec3d, double size, boolean fill, Color fillColor, boolean outline, Color outlineColor) {
        if (outline) renderBoxOutline(matrices, new Box(vec3d.add(-0.5 * size, -0.5 * size,-0.5 * size), vec3d.add(0.5 * size, 0.5 * size,0.5 * size)), outlineColor);
        if (fill) renderBox(matrices, new Box(vec3d.add(-0.5 * size, -0.5 * size, -0.5 * size), vec3d.add(0.5 * size, 0.5 * size, 0.5 * size)), fillColor);
    }

    public void renderCube(MatrixStack matrices, Box box, boolean fill, Color fillColor, boolean outline, Color outlineColor) {
        if (outline) renderBoxOutline(matrices, box, outlineColor);
        if (fill) renderBox(matrices, box, fillColor);
    }

    public void renderBox(MatrixStack matrices, Box box, Color color) {
        renderGradientBox(matrices, box, color, color);
    }
    public void renderBoxС(MatrixStack matrices, Box box, int color) {
        renderGradientBox(matrices, box, new Color(color), new Color(color));
    }

    public void renderBoxCC(MatrixStack matrices, Box box, Color color) {
        renderGradientBox(matrices, box, color, color);
    }
    public void drawBox(Box box, int color, float width) {
        drawBox(box, color, width, true, true, false);
    }
    public void drawBox(Box box, int color, float width, boolean line, boolean fill, boolean depth) {
        drawBox(null, box, color, width, line, fill, depth) ;
    }
    public void drawBox(MatrixStack.Entry entry, Box box, int color, float width, boolean line, boolean fill, boolean depth) {
        box = box.expand(1e-3);

        double x1 = box.minX;
        double y1 = box.minY;
        double z1 = box.minZ;
        double x2 = box.maxX;
        double y2 = box.maxY;
        double z2 = box.maxZ;

        if (fill) {
            int fillColor = ColorUtils.applyOpacity(color, 0.1f);
            drawQuad(entry, new Vec3d(x1, y1, z1), new Vec3d(x2, y1, z1), new Vec3d(x2, y1, z2), new Vec3d(x1, y1, z2), fillColor, depth);
            drawQuad(entry, new Vec3d(x1, y1, z1), new Vec3d(x1, y2, z1), new Vec3d(x2, y2, z1), new Vec3d(x2, y1, z1), fillColor, depth);
            drawQuad(entry, new Vec3d(x2, y1, z1), new Vec3d(x2, y2, z1), new Vec3d(x2, y2, z2), new Vec3d(x2, y1, z2), fillColor, depth);
            drawQuad(entry, new Vec3d(x1, y1, z2), new Vec3d(x2, y1, z2), new Vec3d(x2, y2, z2), new Vec3d(x1, y2, z2), fillColor, depth);
            drawQuad(entry, new Vec3d(x1, y1, z1), new Vec3d(x1, y1, z2), new Vec3d(x1, y2, z2), new Vec3d(x1, y2, z1), fillColor, depth);
            drawQuad(entry, new Vec3d(x1, y2, z1), new Vec3d(x1, y2, z2), new Vec3d(x2, y2, z2), new Vec3d(x2, y2, z1), fillColor, depth);
        }

        if (line) {
            drawLine(entry, x1, y1, z1, x2, y1, z1, color, width, depth);
            drawLine(entry, x2, y1, z1, x2, y1, z2, color, width, depth);
            drawLine(entry, x2, y1, z2, x1, y1, z2, color, width, depth);
            drawLine(entry, x1, y1, z2, x1, y1, z1, color, width, depth);
            drawLine(entry, x1, y1, z2, x1, y2, z2, color, width, depth);
            drawLine(entry, x1, y1, z1, x1, y2, z1, color, width, depth);
            drawLine(entry, x2, y1, z2, x2, y2, z2, color, width, depth);
            drawLine(entry, x2, y1, z1, x2, y2, z1, color, width, depth);
            drawLine(entry, x1, y2, z1, x2, y2, z1, color, width, depth);
            drawLine(entry, x2, y2, z1, x2, y2, z2, color, width, depth);
            drawLine(entry, x2, y2, z2, x1, y2, z2, color, width, depth);
            drawLine(entry, x1, y2, z2, x1, y2, z1, color, width, depth);
        }
    }
    public void drawLine(MatrixStack.Entry entry, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int color, float width, boolean depth) {
        drawLine(entry, new Vec3d(minX, minY, minZ), new Vec3d(maxX, maxY, maxZ), color, color, width, depth);
    }

    public void drawLine(Vec3d start, Vec3d end, int color, float width, boolean depth) {
        drawLine(null, start, end, color, color, width, depth);
    }

    public void drawLine(MatrixStack.Entry entry, Vec3d start, Vec3d end, int colorStart, int colorEnd, float width, boolean depth) {
        Line line = new Line(entry, start, end, colorStart, colorEnd, width);
        if (depth) LINE_DEPTH.add(line); else LINE.add(line);
    }

    public void drawQuad(Vec3d x, Vec3d y, Vec3d w, Vec3d z, int color, boolean depth) {
        drawQuad(null,x,y,w,z,color,depth);
    }

    public void drawQuad(MatrixStack.Entry entry, Vec3d x, Vec3d y, Vec3d w, Vec3d z, int color, boolean depth) {
        Quad quad = new Quad(entry, x, y, w, z, color);
        if (depth) QUAD_DEPTH.add(quad); else QUAD.add(quad);
    }

    public void renderBoxOutline(MatrixStack matrices, Box box, Color color) {
        renderGradientBoxOutline(matrices, box, color, color);
    }

    public void renderGradientBox(MatrixStack matrices, Box box, Color startColor, Color endColor) {
        if (!rendering) return;
        if (!isVisible(box)) return;

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        box = cameraTransform(box);

        QUADS.add(new VertexCollection(new Vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ, startColor.getRGB()),
                new Vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ, startColor.getRGB()),
                new Vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ, startColor.getRGB()),
                new Vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ, startColor.getRGB())));

        QUADS.add(new VertexCollection(new Vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ, endColor.getRGB()),
                new Vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ, endColor.getRGB()),
                new Vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ, startColor.getRGB()),
                new Vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ, startColor.getRGB())));

        QUADS.add(new VertexCollection(new Vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ, startColor.getRGB()),
                new Vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ, endColor.getRGB()),
                new Vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ, endColor.getRGB()),
                new Vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ, startColor.getRGB())));

        QUADS.add(new VertexCollection(new Vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ, startColor.getRGB()),
                new Vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ, endColor.getRGB()),
                new Vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ, endColor.getRGB()),
                new Vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ, startColor.getRGB())));

        QUADS.add(new VertexCollection(new Vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ, startColor.getRGB()),
                new Vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ, endColor.getRGB()),
                new Vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ, endColor.getRGB()),
                new Vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ, startColor.getRGB())));

        QUADS.add(new VertexCollection(new Vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ, endColor.getRGB()),
                new Vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ, endColor.getRGB()),
                new Vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ, endColor.getRGB()),
                new Vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ, endColor.getRGB())));
    }

    public void renderGradientBoxOutline(MatrixStack matrices, Box box, Color startColor, Color endColor) {
        if (!rendering) return;
        if (!isVisible(box)) return;

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        box = cameraTransform(box);

        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ, endColor.getRGB()),
                new Vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ, endColor.getRGB()),
                new Vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ, endColor.getRGB()),
                new Vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ, endColor.getRGB())));

        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ, endColor.getRGB()),
                new Vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ, endColor.getRGB()),
                new Vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ, endColor.getRGB()),
                new Vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ, endColor.getRGB())));

        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ, startColor.getRGB()),
                new Vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ, startColor.getRGB()),
                new Vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ, startColor.getRGB()),
                new Vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ, startColor.getRGB())));

        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ, startColor.getRGB()),
                new Vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ, startColor.getRGB()),
                new Vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ, startColor.getRGB()),
                new Vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ, startColor.getRGB())));

        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ, endColor.getRGB()),
                new Vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ, startColor.getRGB()),
                new Vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ, endColor.getRGB()),
                new Vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ, startColor.getRGB())));

        DEBUG_LINES.add(new VertexCollection(new Vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ, endColor.getRGB()),
                new Vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ, startColor.getRGB()),
                new Vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ, endColor.getRGB()),
                new Vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ, startColor.getRGB())));
    }

    public void prepare() {
        QUADS = new ArrayList<>();
        DEBUG_LINES = new ArrayList<>();
        SHINE_QUADS = new ArrayList<>();
        SHINE_DEBUG_LINES = new ArrayList<>();
        rendering = true;
    }


    public void draw(List<VertexCollection> quads, List<VertexCollection> debugLines, boolean shine) {
        RenderSystem.enableBlend();
        if (shine) RenderSystem.blendFunc(770, 32772);
        else RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.disableDepthTest();

        if (!quads.isEmpty()) {
            BufferBuilder buffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            for (VertexCollection collection : quads) collection.vertex(buffer);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            RenderSystem.disableCull();
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            RenderSystem.enableCull();
        }

        if (!debugLines.isEmpty()) {
            BufferBuilder buffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            for (VertexCollection collection : debugLines) collection.vertex(buffer);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
        }

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private boolean isVisible(Box box) {
        return ((IWorldRenderer) mc.worldRenderer).getFrustum().isVisible(box);
    }

    private Vec3d cameraTransform(Vec3d vec3d) {
        Vec3d camera = mc.gameRenderer.getCamera().getPos();
        Vector4f vec = new Vector4f((float) (vec3d.x - camera.x), (float) (vec3d.y - camera.y), (float) (vec3d.z - camera.z), 1.0f);
        vec.mul(new MatrixStack().peek().getPositionMatrix());
        return new Vec3d(vec.x(), vec.y(), vec.z());
    }

    private Box cameraTransform(Box box) {
        Vec3d min = cameraTransform(new Vec3d(box.minX, box.minY, box.minZ));
        Vec3d max = cameraTransform(new Vec3d(box.maxX, box.maxY, box.maxZ));
        return new Box(min, max);
    }

    private record VertexCollection(Vertex... vertices) {
        public void vertex(BufferBuilder buffer) {
            for (Vertex vertex : vertices) buffer.vertex(vertex.matrix, vertex.x, vertex.y, vertex.z).color(vertex.color);
        }
    }
    public static float getTickDelta() {
        return mc.getRenderTickCounter().getTickDelta(true);
    }
    public record Quad(MatrixStack.Entry entry, Vec3d x, Vec3d y, Vec3d w, Vec3d z, int color) {}
    public record Line(MatrixStack.Entry entry, Vec3d start, Vec3d end, int colorStart, int colorEnd, float width) {}
    private record Vertex(Matrix4f matrix, float x, float y, float z, int color) { }
}