package buwwet.tinyblocksmod.world;

import buwwet.tinyblocksmod.TinyBlocksMod;
import buwwet.tinyblocksmod.blocks.TinyBlock;
import buwwet.tinyblocksmod.blocks.entities.TinyBlockEntity;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
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


    /** Lists all the players that are loading external chunks. */
    public static HashMap<ChunkPos, List<ServerPlayer>> loadedChunksByPlayers = new HashMap<>();

    /** Keeps track of all the blocks that require to be told whenever their observing chunk updates. */
    public static HashMap<ChunkPos, List<BlockPos>> loadedChunksByBlocks = new HashMap<>();


    public static void addBlockListenerToChunk(TinyBlockEntity tinyBlockEntity) {
        ChunkPos chunkPos = new ChunkPos(tinyBlockEntity.getBlockStoragePosition());

        // Create a new entry in the hashmap as this chunkpos hasn't been used yet.
        if (!loadedChunksByBlocks.containsKey(chunkPos)) {

            List<BlockPos> list = new ArrayList<>();
            list.add(tinyBlockEntity.getBlockPos());
            // Add ourselves in our newly created list
            loadedChunksByBlocks.put(chunkPos, list);
        } else {
            // Add ourselves to the already existing list.
            loadedChunksByBlocks.get(chunkPos).add(tinyBlockEntity.getBlockPos());
        }
    }

    /** Mark the TinyBlockEntities as dirty so that their shape gets updated */
    public static void markBlockListenersAsDirty(Level level, ChunkPos chunkPos) {
        // Mark all blocks as dirty
        if (loadedChunksByBlocks.containsKey(chunkPos)) {
            for (BlockPos blockPos : loadedChunksByBlocks.get(chunkPos)) {
                BlockEntity entity = level.getBlockEntity(blockPos);

                // Mark them as dirty
                if (entity instanceof  TinyBlockEntity) {
                    ((TinyBlockEntity) entity).isShapeDirty = true;
                }
            }
        }
        // Send a singular packet to the players that are currently watching this chunk to update their block records.
        // It's just a simple chunk position.

        /* Depreciate this packet, make the client figure it out.
        if (loadedChunksByPlayers.containsKey(chunkPos)) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeChunkPos(chunkPos);
            NetworkManager.sendToPlayers(loadedChunksByPlayers.get(chunkPos), TinyBlocksMod.CLIENTBOUND_DIRTY_CHUNK_UPDATE_PACKET, buf);
        }*/
    }

    /** Handle the request storage chunk Packet from the player */
    public static void requestStorageChunk(Player player, BlockPos tinyBlockPos) {

        //TinyBlocksMod.LOGGER.info("Player requested chunk from block: " + player.getName() + " " + tinyBlockPos.toString());

        // For some strange reason, block entities always return NULL. So we will do the checks ourselves
        ServerPlayer serverPlayer = (ServerPlayer) player;
        ServerLevel serverLevel = serverPlayer.serverLevel();

        // Get block stuff without the block entity
        BlockState blockState = player.level().getBlockState(tinyBlockPos);
        BlockPos tinyBlockStoragePosition = LevelBlockStorageUtil.getBlockStoragePosition(tinyBlockPos);

        if (blockState.getBlock() instanceof TinyBlock) {
            // We actually have a tiny block entity in here.
            // Get the chunk storage position of the tiny entity block
            ChunkPos chunkPos = new ChunkPos(tinyBlockStoragePosition);

            if (!loadedChunksByPlayers.containsKey(chunkPos)) {
                // Chunk hasn't been loaded yet! Force it in!
                serverLevel.setChunkForced(chunkPos.x, chunkPos.z, true);

                ArrayList<ServerPlayer> newListPos = new ArrayList<>();
                newListPos.add(serverPlayer);
                loadedChunksByPlayers.put(chunkPos, newListPos);

                //TinyBlocksMod.LOGGER.info("New chunk loaded for: " + serverPlayer.getName().getString());

            } else {
                // Add another block listener
                if (!loadedChunksByPlayers.get(chunkPos).contains(serverPlayer)) {
                    loadedChunksByPlayers.get(chunkPos).add(serverPlayer);
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
