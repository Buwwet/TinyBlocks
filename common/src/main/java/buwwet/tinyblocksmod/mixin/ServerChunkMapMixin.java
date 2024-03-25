package buwwet.tinyblocksmod.mixin;

import buwwet.tinyblocksmod.TinyBlocksMod;
import buwwet.tinyblocksmod.world.ServerStorageChunkManager;
import com.google.common.collect.ImmutableList;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Set;

@Mixin(ChunkMap.class)
public class ServerChunkMapMixin {


    @Shadow @Final private ServerLevel level;

    // Add all players that have a tiny block's storage chunk enabled.
    @Inject(method = "getPlayers", at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/Set;iterator()Ljava/util/Iterator;"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void playerListChunkAdder(ChunkPos chunkPos, boolean bl, CallbackInfoReturnable<List<ServerPlayer>> cir, Set set, ImmutableList.Builder builder) {
        // Add players if this is a special storage chunk.
        if (ServerStorageChunkManager.loadedChunksByPlayers.containsKey(chunkPos)) {

            // TODO: I don't know if this is the best place for this, but if a packet happened, that means that something changed, right?
            ServerStorageChunkManager.markBlockListenersAsDirty(level, chunkPos);

            // Extend the list to our player list.
            List<ServerPlayer> extraObservers = ServerStorageChunkManager.loadedChunksByPlayers.get(chunkPos);
            for (ServerPlayer player : extraObservers) {
                builder.add(player);
            }

        }

    }
}
