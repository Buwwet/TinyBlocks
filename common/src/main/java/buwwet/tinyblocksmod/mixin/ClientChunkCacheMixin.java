package buwwet.tinyblocksmod.mixin;

import buwwet.tinyblocksmod.TinyBlocksMod;
import buwwet.tinyblocksmod.world.ClientStorageChunkManager;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(ClientChunkCache.class)
public class ClientChunkCacheMixin {

    @Shadow @Final private ClientLevel level;

    @Inject(method = "drop", at = @At(value = "HEAD"))
    public void dropInterject(int i, int j, CallbackInfo ci) {

        ChunkPos chunkPos = new ChunkPos(i, j);

        if (ClientStorageChunkManager.loadedChunks.containsKey(chunkPos)) {
            TinyBlocksMod.LOGGER.warning("Dropping protected chunk!!!: " + i + ", " + j);
        }
    }

    @Inject(method = "replaceWithPacketData", at = @At(value = "HEAD"), cancellable = true)
    // Create the chunk ourselves! And keep it too.
    public void storeStorageChunks(int i, int j, FriendlyByteBuf friendlyByteBuf, CompoundTag compoundTag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer, CallbackInfoReturnable<LevelChunk> cir) {

        ChunkPos chunkPos = new ChunkPos(i, j);
        // Check if this chunk is a protected storage one.
        if (ClientStorageChunkManager.loadedChunks.containsKey(chunkPos)) {
            // It is! Create a LevelChunk out of it and store it somewhere we can manage it.
            LevelChunk levelChunk = new LevelChunk(this.level, chunkPos);
            levelChunk.replaceWithPacketData(friendlyByteBuf, compoundTag, consumer);

            this.level.onChunkLoaded(chunkPos);

            // Store it!
            ClientStorageChunkManager.storage.storeNewChunk(levelChunk);

            // Return our cool chunk to whom?
            cir.setReturnValue(levelChunk);
            //cir.cancel();

        }
    }


    @Inject(method = "getChunk(IILnet/minecraft/world/level/chunk/ChunkStatus;Z)Lnet/minecraft/world/level/chunk/LevelChunk;", at = @At(value = "HEAD"), cancellable = true)
    public void getStorageChunk(int i, int j, ChunkStatus chunkStatus, boolean bl, CallbackInfoReturnable<LevelChunk> cir) {
        // Because ClientChunkCache.Storage has no idea about us holding chunks, it can may lead to bad stuff.
        ChunkPos chunkPos = new ChunkPos(i, j);

        if (ClientStorageChunkManager.storage.hasChunk(chunkPos)) {

            // We have to return it!
            cir.setReturnValue(ClientStorageChunkManager.storage.getChunk(chunkPos));
            //cir.cancel();


        }
    }
}
