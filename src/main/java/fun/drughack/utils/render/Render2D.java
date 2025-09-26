package fun.drughack.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import fun.drughack.api.render.builders.*;
import fun.drughack.api.render.builders.states.*;
import fun.drughack.api.render.renderers.impl.*;
import fun.drughack.utils.Wrapper;

import fun.drughack.utils.render.fonts.Instance;
import lombok.experimental.UtilityClass;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix4f;

import java.awt.*;
import java.awt.image.BufferedImage;



@UtilityClass
public class Render2D implements Wrapper {

    public void drawRoundedRect(MatrixStack stack, float x, float y, float width, float height, float radius, Color color) {
        BuiltRectangle built = Builder.rectangle()
                .size(new SizeState(width, height))
                .radius(new QuadRadiusState(radius))
                .color(new QuadColorState(color))
                .build();
        built.render(stack.peek().getPositionMatrix(), x, y);
    }
    public void drawRoundedRect(MatrixStack stack, float x, float y, float width, float height, float radius, int color) {
        BuiltRectangle built = Builder.rectangle()
                .size(new SizeState(width, height))
                .radius(new QuadRadiusState(radius))
                .color(new QuadColorState(color))
                .build();
        built.render(stack.peek().getPositionMatrix(), x, y);
    }

    public void drawSprite(MatrixStack matrix, Sprite sprite, int x, int y, int z, int width, int height) {
        if (width != 0 && height != 0) {
            drawTexturedQuad(matrix, sprite.getAtlasId(), x, x + width, y, y + height, z,
                    sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
        }
    }
    static public void renderFood(
            Identifier FOOD_EMPTY_HUNGER_TEXTURE,
            Identifier FOOD_HALF_HUNGER_TEXTURE,
            Identifier FOOD_FULL_HUNGER_TEXTURE,
            Identifier FOOD_EMPTY_TEXTURE,
            Identifier FOOD_HALF_TEXTURE,
            Identifier FOOD_FULL_TEXTURE,
            Random random,
            int ticks,
            DrawContext context,
            PlayerEntity player,
            int top, int right
    ) {
        HungerManager hungerManager = player.getHungerManager();
        int foodLevel = hungerManager.getFoodLevel() + (int) hungerManager.getSaturationLevel();

        for (int j = 0; j < 10 + (hungerManager.getSaturationLevel() / 2); ++j) {
            int x = right - (j % 10) * 8 - 9;
            int y = top - (j / 10) * 10;

            Identifier emptyTexture;
            Identifier halfTexture;
            Identifier fullTexture;

            if (player.hasStatusEffect(StatusEffects.HUNGER)) {
                emptyTexture = FOOD_EMPTY_HUNGER_TEXTURE;
                halfTexture = FOOD_HALF_HUNGER_TEXTURE;
                fullTexture = FOOD_FULL_HUNGER_TEXTURE;
            } else {
                emptyTexture = FOOD_EMPTY_TEXTURE;
                halfTexture = FOOD_HALF_TEXTURE;
                fullTexture = FOOD_FULL_TEXTURE;
            }

            if (hungerManager.getSaturationLevel() <= 0.0F && ticks % (foodLevel * 3 + 1) == 0) {
                y += random.nextInt(3) - 1;
            }

            context.drawGuiTexture(RenderLayer::getGuiTextured, emptyTexture, x, y, 9, 9);

            if (j * 2 + 1 < foodLevel) {
                context.drawGuiTexture(RenderLayer::getGuiTextured, fullTexture, x, y, 9, 9);
            } else if (j * 2 + 1 == foodLevel) {
                context.drawGuiTexture(RenderLayer::getGuiTextured, halfTexture, x, y, 9, 9);
            }
        }
    }
    public static float interpolateFloat(float oldValue, float newValue, double interpolationValue) {
        return (float) interpolate(oldValue, newValue, (float) interpolationValue);
    }
    public static Color interpolateColorHue(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));

        float[] color1HSB = Color.RGBtoHSB(color1.getRed(), color1.getGreen(), color1.getBlue(), null);
        float[] color2HSB = Color.RGBtoHSB(color2.getRed(), color2.getGreen(), color2.getBlue(), null);

        Color resultColor = Color.getHSBColor(interpolateFloat(color1HSB[0], color2HSB[0], amount), interpolateFloat(color1HSB[1], color2HSB[1], amount), interpolateFloat(color1HSB[2], color2HSB[2], amount));

        return new Color(resultColor.getRed(), resultColor.getGreen(), resultColor.getBlue(), interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }
    public static int interpolateInt(int start, int end, float progress) {
        return (int) (start + (end - start) * progress);
    }
    public static Color interpolateColorsBackAndForth(float speed, int index, Color start, Color end, boolean trueColor) {
        int angle = (int) (((System.currentTimeMillis()) / speed + index) % 360);
        angle = (angle >= 180 ? 360 - angle : angle) * 2;
        return trueColor ? interpolateColorHue(start, end, angle / 360f) : interpolateColorC(start, end, angle / 360f);
    }

    public static Color interpolateColorC(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        return new Color(interpolateInt(color1.getRed(), color2.getRed(), amount), interpolateInt(color1.getGreen(), color2.getGreen(), amount), interpolateInt(color1.getBlue(), color2.getBlue(), amount), interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }

    public static Color applyOpacity(Color color, float opacity) {
        opacity = Math.min(1, Math.max(0, opacity));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * opacity));
    }

    public static int applyOpacity(int color_int, float opacity) {
        opacity = Math.min(1, Math.max(0, opacity));
        Color color = new Color(color_int);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * opacity)).getRGB();
    }


    public static double interpolate(double oldValue, double newValue, double interpolationValue) {
        return (oldValue + (newValue - oldValue) * interpolationValue);
    }
    public void drawTexture(MatrixStack matrix, Identifier texture, int x, int y, int width, int height,
                            float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        drawTexture(matrix, texture, x, x + width, y, y + height, 0, regionWidth, regionHeight, u, v, textureWidth, textureHeight);
    }

    public void drawTexture(MatrixStack matrix, Identifier texture, int x1, int x2, int y1, int y2, int z,
                            int regionWidth, int regionHeight, float u, float v, int textureWidth, int textureHeight) {
        drawTexturedQuad(matrix, texture, x1, x2, y1, y2, z,
                (u + 0.0F) / (float)textureWidth, (u + (float)regionWidth) / (float)textureWidth,
                (v + 0.0F) / (float)textureHeight, (v + (float)regionHeight) / (float)textureHeight);
    }
    public static Color rainbow(int delay, float saturation, float brightness) {
        double rainbow = Math.ceil((System.currentTimeMillis() + delay) / 16f);
        rainbow %= 360;
        return Color.getHSBColor((float) (rainbow / 360), saturation, brightness);
    }
    public static Color rainbow(int speed, int index, float saturation, float brightness, float opacity) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        float hue = angle / 360f;
        Color color = new Color(Color.HSBtoRGB(hue, saturation, brightness));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, (int) (opacity * 255))));
    }



    public void drawTexturedQuad(MatrixStack matrix, Identifier texture,
                                 int x1, int x2, int y1, int y2, int z,
                                 float u1, float u2, float v1, float v2) {
        RenderSystem.setShaderTexture(0, texture);
        // Я ПАСТЕР
        // Я ПАСТЕР
        // Я ПАСТЕР
        // Я ПАСТЕР
        // Я ПАСТЕР
        // Я ПАСТЕР


        Matrix4f matrix4f = matrix.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        bufferBuilder.vertex(matrix4f, (float)x1, (float)y1, (float)z).texture(u1, v1);
        bufferBuilder.vertex(matrix4f, (float)x1, (float)y2, (float)z).texture(u1, v2);
        bufferBuilder.vertex(matrix4f, (float)x2, (float)y2, (float)z).texture(u2, v2);
        bufferBuilder.vertex(matrix4f, (float)x2, (float)y1, (float)z).texture(u2, v1);

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР



    public void drawRoundedRect4(MatrixStack stack, float x, float y, float width, float height, float radius, Color color,Color color1,Color color2,Color color3) {
        BuiltRectangle built = Builder.rectangle()
                .size(new SizeState(width, height))
                .radius(new QuadRadiusState(radius))
                .color(new QuadColorState(color))
                .color(new QuadColorState(color2))
                .color(new QuadColorState(color3))
                .build();
        built.render(stack.peek().getPositionMatrix(), x, y);
    }
    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР

    public void drawBlurredRect(MatrixStack stack, float x, float y, float width, float height, float radius, float blurRadius, Color color) {
        BuiltBlur built = Builder.blur()
                .size(new SizeState(width, height))
                .radius(new QuadRadiusState(radius))
                .blurRadius(blurRadius)
                .color(new QuadColorState(color))
                .build();
        built.render(stack.peek().getPositionMatrix(), x, y);
    }    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР


    public void drawBorder(MatrixStack stack, float x, float y, float width, float height, float radius, float internalSmoothness, float externalSmoothness, Color color) {
        BuiltBorder built = Builder.border()
                .size(new SizeState(width, height))
                .radius(new QuadRadiusState(radius))
                .smoothness(internalSmoothness, externalSmoothness)
                .color(new QuadColorState(color))
                .build();
        built.render(stack.peek().getPositionMatrix(), x, y);
    }
    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР

    public void drawStyledRect(MatrixStack stack, float x, float y, float width, float height, float radius, Color color, int blurAlpha) {
        drawBlurredRect(stack, x, y, width, height, radius, 10f, new Color(255, 255, 255, blurAlpha));
        drawRoundedRect(stack, x, y, width, height, radius, color);
    }    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР


    public void drawFont(MatrixStack stack, Instance instance, String text, float x, float y, Color color) {
        BuiltText built = Builder.text()
                .size(instance.size())
                .font(instance.font())
                .text(text)
                .thickness(0.05f)
                .color(color)
                .build();
        built.render(stack.peek().getPositionMatrix(), x, y);
    }
    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР

    public void drawTexture(MatrixStack stack, float x, float y, float width, float height, float radius, Identifier texture, Color color) {
        drawTexture(stack, x, y, width, height, radius, mc.getTextureManager().getTexture(texture), color);
    }
    public void drawBufTexture(MatrixStack stack, float x, float y, float width, float height, float radius, BufferedImage texture, Color color) {
        AbstractTexture abstractTexture = convert(texture);
        drawTexture(stack, x, y, width, height, radius, abstractTexture, color);
    }
    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР

    public void drawTexture(MatrixStack stack, float x, float y, float width, float height, float radius, AbstractTexture texture, Color color) {
        BuiltTexture built = Builder.texture()
                .size(new SizeState(width, height))
                .radius(new QuadRadiusState(radius))
                .texture(1f, 1f, 1f, 1f, texture)
                .color(new QuadColorState(color))
                .build();
        built.render(stack.peek().getPositionMatrix(), x, y);
    }
    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР

    public void drawTexture(MatrixStack stack, float x, float y, float width, float height, float radius, float u, float v, float textWidth, float texHeight, Identifier texture, Color color) {
        BuiltTexture built = Builder.texture()
                .size(new SizeState(width, height))
                .radius(new QuadRadiusState(radius))
                .texture(u, v, textWidth, texHeight, mc.getTextureManager().getTexture(texture))
                .color(new QuadColorState(color))
                .build();
        built.render(stack.peek().getPositionMatrix(), x, y);
    }
    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР

    public void startScissor(DrawContext context, float x, float y, float width, float height) {
        context.enableScissor((int) x, (int) y, (int) (x + width), (int) (y + height));
    }
    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР

    public void stopScissor(DrawContext context) {
        context.disableScissor();
    }
    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР

    public AbstractTexture convert(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        NativeImage img = new NativeImage(width, height, false);
        for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) img.setColorArgb(x, y, image.getRGB(x, y));

        return new NativeImageBackedTexture(img);
    }    // Я ПАСТЕР

    public static void drawRect(MatrixStack ms, float v, float lineTop, float v1, float v2, Color color) {
    }

    public static void drawTexture(MatrixStack ms, Identifier user, float userIconX, float userIconY, float iconSize, float iconSize1, float v, Color cIcon) {

    }
    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР
    // Я ПАСТЕР



}