package fun.drughack.api.mixins;

import fun.drughack.DrugHack;
import fun.drughack.api.events.impl.EventKey;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {

    @Inject(method = "onKey", at = @At("HEAD"))
    public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        EventKey event = new EventKey(key, action, modifiers);
        DrugHack.getInstance().getEventHandler().post(event);
    }
}