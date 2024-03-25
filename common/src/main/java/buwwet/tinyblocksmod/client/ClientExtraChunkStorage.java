package buwwet.tinyblocksmod.client;

import buwwet.tinyblocksmod.TinyBlocksMod;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class ClientExtraChunkStorage {

    private HashMap<ChunkPos, LevelChunk> chunks = new HashMap<>();
    public ClientExtraChunkStorage() {

    }

    public void storeNewChunk(LevelChunk levelChunk) {
        chunks.put(levelChunk.getPos(), levelChunk);
    }

    public boolean hasChunk(ChunkPos chunkPos) {
        return chunks.containsKey(chunkPos);
    }

    @Nullable
    public LevelChunk getChunk(ChunkPos chunkPos) {
        return chunks.get(chunkPos);
    }

    /** Removes the desired chunk from our storage. */
    public void unloadChunk(ChunkPos chunkPos) {

        LevelChunk levelChunk = chunks.get(chunkPos);

        ClientLevel clientLevel = (ClientLevel) levelChunk.getLevel();
        // Unload the chunk
        clientLevel.unload(levelChunk);
        //levelChunk = null;

        chunks.remove(chunkPos);
    }
}
