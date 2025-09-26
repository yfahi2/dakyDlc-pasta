package fun.drughack.api.mixins;

import fun.drughack.DrugHack;
import fun.drughack.api.events.impl.EventCollision;
import fun.drughack.managers.ModuleManager;
import fun.drughack.modules.impl.movement.Noclip;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = BlockCollisionSpliterator.class, priority = 800)
public abstract class MixinBlockCollisionSpliterator {

    @Redirect(method = "computeNext", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/BlockView;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
    private BlockState computeNextHook(BlockView instance, BlockPos blockPos) {
        if(!DrugHack.getInstance().getModuleManager().getModule(Noclip.class).isToggled())
            return instance.getBlockState(blockPos);
        EventCollision event = new EventCollision(instance.getBlockState(blockPos), blockPos);
        DrugHack.getInstance().getEventHandler().post(event);
        return event.getState();
    }
}