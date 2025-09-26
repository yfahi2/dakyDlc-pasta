package fun.drughack.modules.impl.render;



import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import fun.drughack.DrugHack;
import fun.drughack.api.events.impl.DrawEvent;
import fun.drughack.api.events.impl.EventRender2D;
import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import fun.drughack.modules.impl.combat.Aura;
import fun.drughack.modules.impl.combat.LegitAura;
import fun.drughack.modules.settings.api.Nameable;
import fun.drughack.modules.settings.impl.EnumSetting;
import fun.drughack.modules.settings.impl.NumberSetting;
import fun.drughack.screen.clickgui.components.impl.ColorSettings;
import fun.drughack.screen.clickgui.components.impl.ColorSettingsW;
import fun.drughack.utils.animations.Animation;
import fun.drughack.utils.animations.Easing;
import fun.drughack.utils.math.MathUtils;
import fun.drughack.utils.render.BetterColor;
import fun.drughack.utils.render.ColorUtils;
import fun.drughack.utils.render.Render2D;
import fun.drughack.utils.world.WorldUtils;
import lombok.AllArgsConstructor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Random;

import static net.minecraft.util.math.RotationAxis.*;

@Nullable
public class TargetEsp extends Module {

    private final NumberSetting size = new NumberSetting(I18n.translate("settings.targetesp.size"), 15f, 10f, 25f, 1f);
    public final EnumSetting<TargetEsp.Mode> mode = new EnumSetting<>("settings.mode", Mode.NEW);
    public NumberSetting sizeDush = new NumberSetting("Размер душ", 0.22f, 0.1f, 0.4f, 0.01f);
    public NumberSetting dlinaDush = new NumberSetting("Длина душ", 6f, 1f, 12f, 0.1f);
    public NumberSetting factorDush = new NumberSetting("Фактор душ", 12f, 0f, 22f, 0.1f);
    public NumberSetting particleDensityDush = new NumberSetting("Кол-во партиклов у душ", 1.7f, 1f, 3f, 0.1f);
    public ColorSettingsW a = new ColorSettingsW("1","1");
    public final ColorSettings all = new ColorSettings(" ", " ");
    public TargetEsp() {
        super("TargetEsp", Category.Render);
    }

    private final Animation animation = new Animation(300, 1f, true, Easing.LINEAR);
    private LivingEntity lastTarget = null;


    private double interpolate(double prev, double current, float delta) {
        return prev + (current - prev) * delta;
    }

    private LivingEntity getPriorityTarget() {
        return Aura.getTarget();
    }
    @EventHandler
    public void render(DrawEvent e){
        if(mode.getValue() != Mode.Test){
            renderGhosts(dlinaDush.getValue(), factorDush.getValue(), 1.32f, 3f, getPriorityTarget());
        }

    }
    @EventHandler
    public void onRender2D(EventRender2D e) {
        if (fullNullCheck()) return;
        if (mode.getValue() != Mode.OLD) return;

        if (mc.player == null || mc.world == null) return;

        Aura aura = DrugHack.getInstance().getModuleManager().getModule(Aura.class);
        LegitAura aura1 = DrugHack.getInstance().getModuleManager().getModule(LegitAura.class);
        LivingEntity target = aura.getTarget() != null ? aura.getTarget() : aura1.getTarget();
        if (target != null) lastTarget = target;
        animation.update(target != null);

        if (animation.getValue() <= 0 || lastTarget == null) return;

        if (lastTarget.isRemoved() || !lastTarget.isAlive()) {
            lastTarget = null;
            return;
        }

        double sin = Math.sin(System.currentTimeMillis() / 1000.0);
        double deltaX = lastTarget.getX() - mc.player.getX();
        double deltaZ = lastTarget.getZ() - mc.player.getZ();
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float maxSize = (float) WorldUtils.getScale(lastTarget.getPos(), size.getValue() * animation.getValue());
        float alpha = (float) Math.sin(lastTarget.hurtTime * (18F * 0.017453292519943295F));
        float finalSize = Math.max(maxSize - (float) distance, size.getValue());

        Vec3d interpolated = lastTarget.getLerpedPos(e.getTickCounter().getTickDelta(false));
        Vec3d pos = WorldUtils.getPosition(new Vec3d(interpolated.x, interpolated.y + lastTarget.getHeight() / 2f, interpolated.z));

        Color color = ColorUtils.fade(
                new Color(255, 255, 255, (int) (255 * animation.getValue())),
                new Color(255, 0, 0, (int) (255 * animation.getValue())),
                alpha
        );

        if (pos.z < 0 || pos.z > 1) return;

        boolean wasBlendEnabled = GL11.glGetBoolean(GL11.GL_BLEND);
        boolean wasLineSmoothEnabled = GL11.glGetBoolean(GL11.GL_LINE_SMOOTH);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

        e.getContext().getMatrices().push();
        e.getContext().getMatrices().translate(pos.getX(), pos.getY(), 0);
        e.getContext().getMatrices().multiply(POSITIVE_Z.rotationDegrees((float) sin * 360));

        float glowSize = finalSize * 1.8f;
        float pulse = 1.0f + (float) Math.sin(System.currentTimeMillis() / 200.0) * 0.15f;
        glowSize *= pulse;

        Color glowColor = new Color(255, 255, 255, (int) (120 * animation.getValue()));

        Render2D.drawTexture(e.getContext().getMatrices(),
                -glowSize / 2f, -glowSize / 2f,
                glowSize, glowSize, 0f,
                DrugHack.id("particles/firefly.png"),
                all.getAwtColor()
        );

        Render2D.drawTexture(e.getContext().getMatrices(),
                -finalSize / 2f, -finalSize / 2f,
                finalSize, finalSize, 0f,
                DrugHack.id("hud/marker.png"),
                color
        );

        e.getContext().getMatrices().pop();

        if (!wasBlendEnabled) GL11.glDisable(GL11.GL_BLEND);
        if (!wasLineSmoothEnabled) GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }


    @EventHandler
    public void onRender3D(DrawEvent e) {
        if (fullNullCheck()) return;
        if (mode.getValue() != Mode.NEW) return;
        Camera camera = mc.gameRenderer.getCamera();

        Aura aura = DrugHack.getInstance().getModuleManager().getModule(Aura.class);
        LegitAura aura1 = DrugHack.getInstance().getModuleManager().getModule(LegitAura.class);
        LivingEntity target = aura.getTarget() != null ? aura.getTarget() : aura1.getTarget();
        if (target == null || !target.isAlive() || target.isRemoved()) return;

        double tPosX = interpolate(target.prevX, target.getX(), MathUtils.getTickDelta()) - camera.getPos().x;
        double tPosY = interpolate(target.prevY, target.getY(), MathUtils.getTickDelta()) - camera.getPos().y;
        double tPosZ = interpolate(target.prevZ, target.getZ(), MathUtils.getTickDelta()) - camera.getPos().z;
        float iAge = (float) interpolate(target.age - 1, target.age, MathUtils.getTickDelta());

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.setShaderTexture(0, DrugHack.id("particles/firefly.png"));
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        RenderSystem.disableDepthTest();


        // Тебе не дает гбт,окей ?
        // ты не видел столько пасты братан окей ?
        // смотри тут хуяки эти летучки нахуй я не знаю как их в конце закруглить поэтому будет такой пиздак ))))))
        int baseAlpha = 255;
        boolean isHurt = target.hurtTime > 0;
        int rFirst, gFirst, bFirst;
        int rTwin, gTwin, bTwin;

        if (isHurt) {

            rFirst = 190; gFirst = 50; bFirst = 50;
            rTwin  = 255; gTwin  = 0;  bTwin  = 0;
        } else {
            rFirst = new Color(75, 97, 163).getRed();
            gFirst = new Color(75, 97, 163).getGreen();
            bFirst = new Color(75, 97, 163).getBlue();

            rTwin  = new Color(75, 97, 163).getRed();
            gTwin  = new Color(75, 97, 163).getGreen();
            bTwin  = new Color(75, 97, 163).getBlue();
        }

        float time = (float) (System.currentTimeMillis() % 6000) / 6000.0f;
        float phaseShift = (float) (2.0 * Math.PI / 4.0);
        float particleDensity = particleDensityDush.getValue();
        float[] angles = {45f, 135f, 225f, 315f};

        for (int j = 0; j < 4; j++) {
            float colorLerp = (float) (Math.sin(2.0 * Math.PI * time + j * phaseShift) * 0.5 + 0.5);
            int r = (int) (rFirst + (rTwin - rFirst) * colorLerp);
            int g = (int) (gFirst + (gTwin - gFirst) * colorLerp);
            int b = (int) (bFirst + (bTwin - bFirst) * colorLerp);


            float step = 1f / particleDensity;
            int particleCount = (int) (dlinaDush.getValue() * particleDensity);

            for (int i = 0; i < particleCount; i++) {
                float offset = (float) i / particleCount;
                double radians = Math.toRadians((angles[j] + (offset * dlinaDush.getValue() + iAge) * factorDush.getValue()) % 360);

                double sinQuad = Math.sin(Math.toRadians(iAge * 2.5f + offset * dlinaDush.getValue() * 2.0f + j * 90) * 3) / 1.32f;


                float alphaFactor = 0.91f - offset * 0.5f;
                int gradientAlpha = (int) (baseAlpha * alphaFactor);

                float scale = sizeDush.getValue();

                MatrixStack matrices = new MatrixStack();
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
                matrices.translate(tPosX + Math.cos(radians) * target.getWidth(), tPosY + 1 + sinQuad, tPosZ + Math.sin(radians) * target.getWidth());
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                Matrix4f matrix = matrices.peek().getPositionMatrix();

                int color = BetterColor.rgba(r, g, b, gradientAlpha);

                buffer.vertex(matrix, -scale, scale, 0).texture(0f, 1f).color(color);
                buffer.vertex(matrix, scale, scale, 0).texture(1f, 1f).color(color);
                buffer.vertex(matrix, scale, -scale, 0).texture(1f, 0).color(color);
                buffer.vertex(matrix, -scale, -scale, 0).texture(0, 0).color(color);
            }
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.enableDepthTest();

        RenderSystem.disableBlend();
    }

    @EventHandler
    public void renderGhosts(float espLength, float factor, float shaking, float amplitude, Entity target) {
        if (fullNullCheck()) return;
        if (mode.getValue() != Mode.Test) return;
        Camera camera = mc.gameRenderer.getCamera();

        double tPosX = interpolate(target.prevX, target.getX(), MathUtils.getTickDelta()) - camera.getPos().x;
        double tPosY = interpolate(target.prevY, target.getY(), MathUtils.getTickDelta()) - camera.getPos().y;
        double tPosZ = interpolate(target.prevZ, target.getZ(), MathUtils.getTickDelta()) - camera.getPos().z;
        float iAge = (float) interpolate(target.age - 1, target.age, MathUtils.getTickDelta());

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.setShaderTexture(0, DrugHack.id("particles/firefly.png"));
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        RenderSystem.disableDepthTest();

        // Цвета
        int baseAlpha = 255;
        int rFirst = new Color(133, 166, 255).getRGB() >> 16 & 0xFF;
        int gFirst = new Color(133, 166, 255).getRGB() >> 8 & 0xFF;
        int bFirst = new Color(133, 166, 255).getRGB() & 0xFF;
        int rTwin = new Color(75, 97, 163).getRGB() >> 16 & 0xFF;
        int gTwin = new Color(75, 97, 163).getRGB() >> 8 & 0xFF;
        int bTwin = new Color(75, 97, 163).getRGB() & 0xFF;

        float time = (float) (System.currentTimeMillis() % 6000) / 6000.0f;
        float phaseShift = (float) (2.0 * Math.PI / 4.0);
        float particleDensity = 1;
        float[] angles = {45f, 135f, 225f, 315f};

        for (int j = 0; j < 4; j++) {
            float colorLerp = (float) (Math.sin(2.0 * Math.PI * time + j * phaseShift) * 0.5 + 0.5);
            int r = (int) (rFirst + (rTwin - rFirst) * colorLerp);
            int g = (int) (gFirst + (gTwin - gFirst) * colorLerp);
            int b = (int) (bFirst + (bTwin - bFirst) * colorLerp);

            // Нормализованный шаг для частиц
            float step = 1f / particleDensity;
            int particleCount = (int) (espLength * particleDensity); // Фиксированное количество частиц

            for (int i = 0; i < particleCount; i++) {
                float offset = (float) i / particleCount; // Нормализованный прогресс от 0 до 1
                double radians = Math.toRadians((angles[j] + (offset * espLength + iAge) * factor) % 360);
                // Восстановлена динамика оригинальной анимации с одинаковой длиной
                double sinQuad = Math.sin(Math.toRadians(iAge * 2.5f + offset * espLength * 2.0f + j * 90) * amplitude) / shaking;

                // Плавное затухание к концу траектории
                float alphaFactor = 0.91f - offset * 0.5f;
                int gradientAlpha = (int) (baseAlpha * alphaFactor);

                float scale = 0.22f;

                MatrixStack matrices = new MatrixStack();
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
                matrices.translate(tPosX + Math.cos(radians) * target.getWidth(), tPosY + 1 + sinQuad, tPosZ + Math.sin(radians) * target.getWidth());
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                Matrix4f matrix = matrices.peek().getPositionMatrix();

                int color = BetterColor.rgba(r, g, b, gradientAlpha);

                buffer.vertex(matrix, -scale, scale, 0).texture(0f, 1f).color(color);
                buffer.vertex(matrix, scale, scale, 0).texture(1f, 1f).color(color);
                buffer.vertex(matrix, scale, -scale, 0).texture(1f, 0).color(color);
                buffer.vertex(matrix, -scale, -scale, 0).texture(0, 0).color(color);
            }
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.enableDepthTest();

        RenderSystem.disableBlend();
    }

    //          ColorUtils.darkenWithAlpha(elementsColor.get(), 0.42f),
//                  ColorUtils.darkenWithAlpha(elementsColor.get(), 0.42f),
//                  elementsColor.get(),
//                  elementsColor.get()))
    @EventHandler
    public void onRender3DD(DrawEvent e) {
        if (fullNullCheck()) return;
        if (mode.getValue() != Mode.Arbuz) return;

        LivingEntity target = Aura.getTarget();
        if (target == null || !target.isAlive() || target.isRemoved()) return;

        // Настройки эффекта
        int particleCount = 12; // Увеличено количество частиц
        float baseRadius = target.getWidth() * 1.4f;
        float heightVariation = 1.8f;
        float baseSpeed = 0.8f;
        Camera camera = mc.gameRenderer.getCamera();

        // Интерполяция позиции цели
        double tPosX = interpolate(target.prevX, target.getX(), MathUtils.getTickDelta()) - camera.getPos().x;
        double tPosY = interpolate(target.prevY, target.getY(), MathUtils.getTickDelta()) - camera.getPos().y;
        double tPosZ = interpolate(target.prevZ, target.getZ(), MathUtils.getTickDelta()) - camera.getPos().z;
        float ageFactor = (target.age + MathUtils.getTickDelta()) * 0.05f;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.setShaderTexture(0, DrugHack.id("particles/firefly.png"));
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        RenderSystem.disableDepthTest();

        // Генерация уникального seed для предсказуемого рандома
        long seed = (long)(target.getX() * 1000 + target.getZ() * 2000);
        Random random = new Random(seed);

        for (int i = 0; i < particleCount; i++) {
            // Уникальные параметры для каждой частицы
            float particleProgress = (float)i / particleCount;
            float particleOffset = random.nextFloat() * 6.28f;
            float particleSpeed = baseSpeed * (0.8f + random.nextFloat() * 0.4f);
            float radiusVariation = 0.7f + random.nextFloat() * 0.6f;

            // Динамическое движение - комбинация кругового и хаотичного
            float timeFactor = ageFactor * particleSpeed;
            double angle = timeFactor + particleOffset;
            float radius = baseRadius * radiusVariation;

            // Добавляем хаотичность в движение
            float chaosX = (float)Math.sin(timeFactor * 0.7 + i * 2.3) * 0.3f;
            float chaosY = (float)Math.cos(timeFactor * 0.5 + i * 1.7) * 0.4f;
            float chaosZ = (float)Math.sin(timeFactor * 0.9 + i * 3.1) * 0.3f;

            // Высота с вариацией и плавным изменением
            float verticalOffset = (float)Math.sin(timeFactor * 0.3 + i) * heightVariation;
            float yPos = (float) (tPosY + 0.5f + verticalOffset * 0.5f + chaosY);

            MatrixStack matrices = new MatrixStack();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
            matrices.translate(
                    tPosX + Math.cos(angle) * radius + chaosX,
                    yPos,
                    tPosZ + Math.sin(angle) * radius + chaosZ
            );
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));

            Matrix4f matrix = matrices.peek().getPositionMatrix();

            // Динамический цвет с плавными переходами
            Color color1 = new Color(22, 64, 186,255);
            Color color2 = new Color(138, 20, 147,255);
            Color color3 = new Color(20, 186, 100,255);
            Color particleColor;

            if (particleProgress < 0.33f) {
                particleColor = interpolateColors(color1, color2, particleProgress * 3);
            } else if (particleProgress < 0.66f) {
                particleColor = interpolateColors(color2, color3, (particleProgress - 0.33f) * 3);
            } else {
                particleColor = interpolateColors(color3, color1, (particleProgress - 0.66f) * 3);
            }

            // Добавляем мерцание
            float alpha = 0.5f + (float)Math.sin(timeFactor * 2 + i) * 0.2f;
            particleColor = new Color(
                    particleColor.getRed() / 255f,
                    particleColor.getGreen() / 255f,
                    particleColor.getBlue() / 255f,
                    alpha
            );

            // Динамический размер частицы
            float sizeVariation = 0.8f + (float)Math.sin(timeFactor * 3 + i * 2) * 0.2f;
            float scale = 0.15f * sizeVariation;

            // Отрисовка частицы
            int color = particleColor.getRGB();
            buffer.vertex(matrix, -scale, scale, 0).texture(0f, 1f).color(color);
            buffer.vertex(matrix, scale, scale, 0).texture(1f, 1f).color(color);
            buffer.vertex(matrix, scale, -scale, 0).texture(1f, 0).color(color);
            buffer.vertex(matrix, -scale, -scale, 0).texture(0, 0).color(color);
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    // Вспомогательная функция для интерполяции цветов

    // Вспомогательная функция для интерполяции цветов
    private Color interpolateColors(Color c1, Color c2, float progress) {
        float invProgress = 1.0f - progress;
        return new Color(
                (int)(c1.getRed() * invProgress + c2.getRed() * progress),
                (int)(c1.getGreen() * invProgress + c2.getGreen() * progress),
                (int)(c1.getBlue() * invProgress + c2.getBlue() * progress)
        );
    }

    @AllArgsConstructor
    public enum Mode implements Nameable {
        OLD("нурик"),
        NEW("точка"),
        Test("Гост"),
        Arbuz("Арбуз");

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }
    // int color = Render2D.applyOpacity(Render2D.interpolateColorsBackAndForth(2,2,new Color(22, 64, 186),new Color(138, 20, 147),true), (180 * offset)).getRGB();
}