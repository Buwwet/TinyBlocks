package buwwet.tinyblocksmod.world;


import buwwet.tinyblocksmod.TinyBlocksMod;
import buwwet.tinyblocksmod.client.ClientExtraChunkStorage;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
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

    // Request the given storage chunk if it appears that we don't have it.
    public static void requestStorageChunk(BlockPos tinyBlockPos, BlockPos storageBlockPos) {
        ChunkPos chunkPos = new ChunkPos(storageBlockPos);

        // Check if the chunk is already supposed to be loaded.
        if (loadedChunks.containsKey(chunkPos)) {
            // We do not need to load the chunk, but we do need to add this block position if is not already here
            if (!loadedChunks.get(chunkPos).contains(tinyBlockPos)) {
                loadedChunks.get(chunkPos).add(tinyBlockPos);
            }
            return;
        }

        // The chunk isn't even loaded! Add it to the list.
        List<BlockPos> blockHolderList = new ArrayList<>();
        blockHolderList.add(tinyBlockPos);
        loadedChunks.put(chunkPos, blockHolderList);

        // Request for the server to send the TinyBlock's chunk.
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBlockPos(tinyBlockPos);
        NetworkManager.sendToServer(TinyBlocksMod.SERVERBOUND_BLOCK_CHUNK_REQUEST_PACKET, buf);

    }

    public static void removeStorageChunkListener(BlockPos tinyBlockPos, BlockPos storageBlockPos) {
        //TODO
    }
}
