package buwwet.tinyblocksmod.world;

import buwwet.tinyblocksmod.TinyBlocksMod;
import buwwet.tinyblocksmod.blocks.TinyBlock;
import buwwet.tinyblocksmod.blocks.entities.TinyBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServerStorageChunkManager {


    public static HashMap<ChunkPos, List<ServerPlayer>> loadedChunks = new HashMap<>();


    public static void requestStorageChunk(Player player, BlockPos tinyBlockPos) {

        TinyBlocksMod.LOGGER.info("PACKET: " + player.getName() + " " + tinyBlockPos.toString());


        // For some strange reason, block entities always return NULL. So we will do the checks ourselves
        ServerPlayer serverPlayer = (ServerPlayer) player;
        ServerLevel serverLevel = serverPlayer.serverLevel();



        // Get block stuff without the block entity
        BlockState blockState = player.level().getBlockState(tinyBlockPos);
        BlockPos tinyBlockStoragePosition = LevelBlockStorageUtil.getBlockStoragePosition(tinyBlockPos);

        //TinyBlocksMod.LOGGER.info("Is chunk server-side loaded? " + serverLevel.isLoaded(tinyBlockStoragePosition));


        if (blockState.getBlock() instanceof TinyBlock) {
            // We actually have a tiny block entity in here.
            // Get the chunk storage position of the tiny entity block
            ChunkPos chunkPos = new ChunkPos(tinyBlockStoragePosition);

            if (!loadedChunks.containsKey(chunkPos)) {
                // Chunk hasn't been loaded yet!
                serverLevel.setChunkForced(chunkPos.x, chunkPos.z, true);

                ArrayList<ServerPlayer> newListPos = new ArrayList<>();
                newListPos.add(serverPlayer);
                loadedChunks.put(chunkPos, newListPos);

                TinyBlocksMod.LOGGER.info("New chunk loaded: " + serverPlayer.getName().getString());


            } else {
                // Add another block listener
                if (!loadedChunks.get(chunkPos).contains(serverPlayer)) {
                    loadedChunks.get(chunkPos).add(serverPlayer);
                }
            }


            // Send a packet with the chunk data.
            serverPlayer.connection.send(
                    new ClientboundLevelChunkWithLightPacket(
                            serverLevel.getChunkAt(tinyBlockStoragePosition),
                            serverLevel.getLightEngine(),
                            null, null
                    )
            );
        }
    }

    public static void removeStorageChunkListener(Player player, BlockPos tinyBlockPos) {
        //TODO
        // Check if the block actually still has players
        //level.unload()
        //setchunkforced(false

    }

}
