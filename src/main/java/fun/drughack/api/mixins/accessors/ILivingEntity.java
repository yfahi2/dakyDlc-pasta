package fun.drughack.api.mixins.accessors;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface ILivingEntity {

    @Accessor("jumpingCooldown") void setJumpingCooldown(int jumpingCooldown);
}