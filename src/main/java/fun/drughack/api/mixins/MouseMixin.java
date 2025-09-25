package fun.drughack.api.mixins;

import fun.drughack.DrugHack;
import fun.drughack.api.events.impl.EventMouse;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    public void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        EventMouse event = new EventMouse(button, action);
        DrugHack.getInstance().getEventHandler().post(event);
    }
}