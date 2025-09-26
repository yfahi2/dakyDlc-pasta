package fun.drughack.api.mixins;

import fun.drughack.DrugHack;
import fun.drughack.modules.impl.render.AspectRatio;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.render.GameRenderer;

@Mixin(GameRenderer.class)
public class AspectRatioMixin {

    @Inject(method = "getBasicProjectionMatrix", at = @At("RETURN"), cancellable = true)
    private void modifyProjectionMatrix(float fov, CallbackInfoReturnable<Matrix4f> cir) {
        AspectRatio module = DrugHack.getInstance().getModuleManager().getModule(AspectRatio.class);
        if (module == null || !module.isToggled()) return;

        // берём значение из NumberSetting; при необходимости замените на ваш геттер
        float aspect = module.getAspectRatio().getValue();
        float fovRad = (float) Math.toRadians(fov);

        // при желании подправьте near/far под ваш рендер-пайплайн
        Matrix4f projection = new Matrix4f().setPerspective(fovRad, aspect, 0.05f, 256.0f);
        cir.setReturnValue(projection);
    }
}