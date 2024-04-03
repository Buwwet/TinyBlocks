package buwwet.tinyblocksmod.world;


import buwwet.tinyblocksmod.TinyBlocksMod;
import buwwet.tinyblocksmod.blocks.entities.TinyBlockEntity;
import buwwet.tinyblocksmod.client.ClientExtraChunkStorage;
import buwwet.tinyblocksmod.networking.NetworkPackets;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClientStorageChunkManager {
    //public static ClientStorageChunkManager chunkManager = new ClientStorageChunkManager();

    // Keeps track of the far away chunk and the tiny blocks that are loading it.
    public static HashMap<ChunkPos, List<BlockPos>> loadedChunks = new HashMap<>();


    // Actually stores the chunk data.
    public static ClientExtraChunkStorage storage = new ClientExtraChunkStorage();

    /** Request the given storage chunk if it appears that we don't have it. */
    public static void requestStorageChunk(TinyBlockEntity tinyBlockEntity) {
        ChunkPos chunkPos = new ChunkPos(tinyBlockEntity.getBlockStoragePosition());

        // Check if the chunk is already supposed to be loaded.
        if (loadedChunks.containsKey(chunkPos)) {
            // We do not need to load the chunk, but we do need to add this block position if is not already here
            if (!loadedChunks.get(chunkPos).contains(tinyBlockEntity.getBlockPos())) {
                loadedChunks.get(chunkPos).add(tinyBlockEntity.getBlockPos());
            }
            return;
        }

        // The chunk isn't even loaded! Add it to the list.
        List<BlockPos> blockHolderList = new ArrayList<>();
        blockHolderList.add(tinyBlockEntity.getBlockPos());
        loadedChunks.put(chunkPos, blockHolderList);

        // Request for the server to send the TinyBlock's chunk.
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBlockPos(tinyBlockEntity.getBlockPos());
        NetworkManager.sendToServer(NetworkPackets.SERVERBOUND_BLOCK_CHUNK_REQUEST_PACKET, buf);

    }

    /** Remove a block from our listeners, and if a chunk loses all listeners, unload it. */
    public static void removeStorageChunkListener(BlockPos tinyBlockPos) {

        ChunkPos storageChunkPos = new ChunkPos(LevelBlockStorageUtil.getBlockStoragePosition(tinyBlockPos));

        // Remove it from our list.
        if (loadedChunks.containsKey(storageChunkPos)) {
            // Check that we aren't the only block observing the chunk.

            List<BlockPos> record = loadedChunks.get(storageChunkPos);
            if (record.size() > 1) {
                // There are more blocks using this chunk, so we just remove ourselves and return.
                record.remove(tinyBlockPos);
                return;
            }

            // There is only one block remaining, (us), so we do the chunk unloading.
            //record = null;

            // Remove this chunk from our list and unload it.
            loadedChunks.remove(storageChunkPos);
            storage.unloadChunk(storageChunkPos);
        }


        //if (storage)
    }

    /** The server just told us that there has been an update within the chunk and that our shape is dirty. */
    public static void handleClientBoundDirtyChunkPacket(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        ChunkPos chunkPos = buf.readChunkPos();
        BlockPos tinyBlockPos = buf.readBlockPos();

        //TinyBlocksMod.LOGGER.info("Received dirty chunk packet! " + chunkPos);


        // Sanity check because I hate null
        if (loadedChunks.containsKey(chunkPos)) {
            // Get the TinyBlockEntity that corresponds to this tiny block.
            if (loadedChunks.get(chunkPos).contains(tinyBlockPos)) {
                BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(tinyBlockPos);
                if (blockEntity instanceof TinyBlockEntity) {
                    ((TinyBlockEntity) blockEntity).isShapeDirty = true;
                }
            }
        }

    }


    /** We have to reset everything as we are joining to a new world, or traveling to a new level */
    public static void clearCache() {
        // TODO: I don't know how memory leaks work in java, but this is definitely one of them.

        storage = new ClientExtraChunkStorage();
        loadedChunks = new HashMap<>();
    }
}
