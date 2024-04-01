package buwwet.tinyblocksmod.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Container.class)
public interface ContainerMixin {

    @Inject(method = "stillValidBlockEntity(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/player/Player;I)Z", at = @At("HEAD"), cancellable = true)
    private static void validateTinyBlocks(BlockEntity blockEntity, Player player, int i, CallbackInfoReturnable<Boolean> cir) {
        //cir.setReturnValue(true);
    }

}
