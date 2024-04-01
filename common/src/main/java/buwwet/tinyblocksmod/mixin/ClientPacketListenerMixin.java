package buwwet.tinyblocksmod.mixin;

import buwwet.tinyblocksmod.TinyBlocksMod;
import buwwet.tinyblocksmod.blocks.entities.TinyBlockEntity;
import buwwet.tinyblocksmod.world.ClientStorageChunkManager;
import buwwet.tinyblocksmod.world.LevelBlockStorageUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    /** Marks tiny blocks as dirty if we recieve block updates with their storage blocks */
    @Inject(method = "handleChunkBlocksUpdate", at = @At("TAIL"))
    private void tinyBlockStorageDirtier(ClientboundSectionBlocksUpdatePacket clientboundSectionBlocksUpdatePacket, CallbackInfo ci) {

        clientboundSectionBlocksUpdatePacket.runUpdates((blockPos, blockState) ->  {
            ChunkPos chunkPos = Minecraft.getInstance().level.getChunkAt(blockPos).getPos();

            // This storage block belongs to a tiny block.
            if (ClientStorageChunkManager.loadedChunks.containsKey(chunkPos)) {
                // TODO, we want to mark specifically the block tiny block that parents this storage block, but unfortunately, I can't come up with a way
                // right now. I'd be a BIG optimisation.
                // Instead of updating one tiny block, we are updating a whopping 2x2x302 of them (worst case scenario).
                for (BlockPos tinyBlockPos : ClientStorageChunkManager.loadedChunks.get(chunkPos)) {
                    // So currently, I'll use this terrible work around.
                    BlockPos storagePos = LevelBlockStorageUtil.getBlockStoragePosition(tinyBlockPos);
                    // Check if all the fields don't vary by more than 4
                    if ((storagePos.getX() - blockPos.getX()) < 4) {
                        if ((storagePos.getY() - blockPos.getY()) < 4) {
                            if ((storagePos.getZ() - blockPos.getZ()) < 4) {
                                // It's a hit! Mark it as dirty.
                                TinyBlockEntity blockEntity = (TinyBlockEntity) Minecraft.getInstance().level.getBlockEntity(tinyBlockPos);
                                blockEntity.isShapeDirty = true;
                            }
                        }
                    }
                }
            }

        });
    }
}
