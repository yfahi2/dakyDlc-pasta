package fun.drughack.modules.impl.render;

import fun.drughack.modules.api.Category;
import fun.drughack.modules.api.Module;
import fun.drughack.modules.settings.impl.BooleanSetting;
import fun.drughack.modules.settings.impl.ListSetting;
import fun.drughack.modules.settings.impl.NumberSetting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class SwingAnimations extends Module {

    private static final SwingAnimations INSTANCE = new SwingAnimations();

    // Settings
    private final BooleanSetting modeNormal = new BooleanSetting("Normal", true, () -> false);
    private final BooleanSetting modeFirst = new BooleanSetting("First", false, () -> false);
    private final BooleanSetting modeSecond = new BooleanSetting("Second", false, () -> false);
    private final BooleanSetting modeThird = new BooleanSetting("Third", false, () -> false);
    private final BooleanSetting modeFourth = new BooleanSetting("Fourth", false, () -> false);
    private final BooleanSetting modeSixth = new BooleanSetting("Sixth", false, () -> false);
    private final BooleanSetting modeSeventh = new BooleanSetting("Seventh", false, () -> false);

    private final ListSetting animationMode = new ListSetting(
            I18n.translate("Animation Mode"),
            modeNormal, modeFirst, modeSecond, modeThird, modeFourth,
            modeSixth, modeSeventh
    );

    private final NumberSetting swingPower = new NumberSetting("Swing Power", 5.0f, 1.0f, 10.0f, 0.05f);
    private static final float scale = 0.5f;

    public SwingAnimations() {
        super("SwingAnimation", Category.Render);
    }

    public void renderSwordAnimation(MatrixStack matrices, float swingProgress, float equipProgress, Arm arm) {
        // Ensure only one mode is active
        BooleanSetting activeMode = animationMode.getToggled().stream().findFirst().orElse(modeNormal);
        if (animationMode.getToggled().size() > 1) {
            // If multiple modes are toggled, disable all except the first
            BooleanSetting finalActiveMode = activeMode;
            animationMode.getValue().forEach(setting -> {
                if (setting != finalActiveMode) {
                    setting.setValue(false);
                }
            });
        } else if (animationMode.getToggled().isEmpty()) {
            // If no modes are toggled, enable Normal
            modeNormal.setValue(true);
            activeMode = modeNormal;
        }

        float power = swingPower.getValue().floatValue();
        float anim = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
        float scaleValue = scale;

        switch (activeMode.getName()) {
            case "Normal" -> {
                matrices.translate(0.56F, -0.52F, -0.72F);
                float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -60.0F * power / 5.0f));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(g * -30.0F * power / 5.0f));
            }
            case "First" -> {
                if (swingProgress > 0) {
                    float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                    matrices.translate(0.56F, equipProgress * -0.2f - 0.5F, -0.7F);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -85.0F * power / 5.0f));
                    matrices.translate(-0.1F, 0.28F, 0.2F);
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-85.0F));
                } else {
                    float n = -0.4f * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                    float m = 0.2f * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI * 2);
                    float f1 = -0.2f * MathHelper.sin(swingProgress * (float) Math.PI);
                    matrices.translate(n, m, f1);
                    applyEquipOffset(matrices, arm, equipProgress);
                    applySwingOffset(matrices, arm, swingProgress);
                }
            }
            case "Second" -> {
                float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                applyEquipOffset(matrices, arm, 0);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(50f));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-60f));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(110f + 20f * g * power / 5.0f));
            }
            case "Third" -> {
                float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                applyEquipOffset(matrices, arm, 0);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(50f));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-30f * (1f - g) - 30f ));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(110f));
            }
            case "Fourth" -> {
                float g = MathHelper.sin(swingProgress * (float) Math.PI);
                applyEquipOffset(matrices, arm, 0);
                matrices.translate(0.1F, -0.2F, -0.3F);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-30f * g * power / 5.0f - 36f));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(25f * g * power / 5.0f));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(12f));
            }
            case "Sixth" -> {
                matrices.scale(scaleValue, scaleValue, scaleValue);
                applyEquipOffset(matrices, arm, 0);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(15 * anim));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(0 * anim));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-(power * 10) * anim));
            }
            case "Seventh" -> {
                matrices.scale(scaleValue+0.1f, scaleValue, scaleValue-0.1f);
                applyEquipOffset(matrices, arm, 0);
                matrices.translate(0.2f * anim, 0, -0.5f);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90 * anim * power / 5.0f));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-140 * anim * power / 5.0f));
            }
        }
    }

    private void applyEquipOffset(MatrixStack matrices, Arm arm, float equipProgress) {
        int i = arm == Arm.RIGHT ? 1 : -1;
        matrices.translate((float) i * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
    }

    private void applySwingOffset(MatrixStack matrices, Arm arm, float swingProgress) {
        int i = arm == Arm.RIGHT ? 1 : -1;
        float f = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * (45.0F + f * -20.0F)));
        float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) i * g * -20.0F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -80.0F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * -45.0F));
    }

    // Getter for singleton instance
    public static SwingAnimations getInstance() {
        return INSTANCE;
    }
}