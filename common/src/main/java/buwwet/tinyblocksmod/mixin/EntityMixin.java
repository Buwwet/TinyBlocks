package buwwet.tinyblocksmod.mixin;

import buwwet.tinyblocksmod.TinyBlocksMod;
import buwwet.tinyblocksmod.world.ServerStorageChunkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow private Level level;

    /** Check if the block position given is in a storage chunk if so, override! */
    @Inject(method = "distanceToSqr(DDD)D", cancellable = true, at = @At("HEAD"))
    private void distanceToSquareRootOverride(double d, double e, double f, CallbackInfoReturnable<Double> cir) {
        BlockPos blockPos = new BlockPos((int) d, (int) e, (int) f);
        ChunkPos chunkPos = level.getChunkAt(blockPos).getPos();

        if (ServerStorageChunkManager.loadedChunksByBlocks.containsKey(chunkPos)) {
            TinyBlocksMod.LOGGER.info("" + chunkPos);

            cir.setReturnValue(2.0);
        }
    }

}
