package fun.drughack.api.mixins;

import fun.drughack.DrugHack;
import fun.drughack.modules.impl.misc.HitSound;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class HitSoundMixin {

    @Inject(method = "attack", at = @At("HEAD"))
    private void onAttack(Entity target, CallbackInfo ci) {
        HitSound module = DrugHack.getInstance().getModuleManager().getModule(HitSound.class);
        if (module != null && module.isToggled() && target instanceof LivingEntity) {
            float volume = module.getVolume().getValue();
            String soundId = module.getSelectedSound();
            MinecraftClient.getInstance().getSoundManager().play(
                    PositionedSoundInstance.master(
                            SoundEvent.of(Identifier.of(soundId)),
                            1.0f,
                            volume
                    )
            );
        }
    }
}

