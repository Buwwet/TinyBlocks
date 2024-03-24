package buwwet.tinyblocksmod.mixin;

import buwwet.tinyblocksmod.TinyBlocksMod;
import buwwet.tinyblocksmod.world.ClientStorageChunkManager;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(targets = {"net.minecraft.client.multiplayer.ClientChunkCache$Storage"})
public class ClientChunkCacheStorageMixin {

    @Inject(method = "inRange", at= @At(value = "RETURN"), cancellable = true)
    public void permitExtraChunks(int i, int j, CallbackInfoReturnable<Boolean> cir) {

        //TinyBlocksMod.LOGGER.info("" + i + " " + j);
        //TODO: Keep this loaded ONLY if it is in a list of permited chunks, which will be
        // updated whenever a block goes in or out of view distance.

        cir.setReturnValue(true);

    }

    // Simple mixin that warns us if we weren't able to prevent a chunk from being replaced
    @Inject(method = "replace(ILnet/minecraft/world/level/chunk/LevelChunk;)V", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "TAIL"))
    public void warnOnReplaceTwo(int i, LevelChunk levelChunk, CallbackInfo ci, @Local LevelChunk levelChunk2) {

        ChunkPos targetChunkPos = levelChunk.getPos();

        // WE'RE GOING TO GET REPLACED!!!
        if (ClientStorageChunkManager.loadedChunks.containsKey(targetChunkPos)) {
            TinyBlocksMod.LOGGER.warning("Replacing protected chunk!!!: " + targetChunkPos);
        }
    }

    @Inject(method = "replace(ILnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/chunk/LevelChunk;)Lnet/minecraft/world/level/chunk/LevelChunk;", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "TAIL"))
    public void warnOnReplaceOne(int i, LevelChunk levelChunk, LevelChunk levelChunk2, CallbackInfoReturnable<LevelChunk> cir) {

        ChunkPos targetChunkPos = levelChunk.getPos();

        // WE'RE GOING TO GET REPLACED!!!
        if (ClientStorageChunkManager.loadedChunks.containsKey(targetChunkPos)) {
            TinyBlocksMod.LOGGER.warning("Replacing protected chunk!!!: " + targetChunkPos);
        }
    }

}
