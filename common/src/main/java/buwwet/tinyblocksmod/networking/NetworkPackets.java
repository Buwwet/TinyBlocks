package buwwet.tinyblocksmod.networking;

import buwwet.tinyblocksmod.TinyBlocksMod;
import buwwet.tinyblocksmod.world.ClientStorageChunkManager;
import buwwet.tinyblocksmod.world.ServerStorageChunkManager;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;

public class NetworkPackets {

    // SERVER TO CLIENT
    /** Tells the client that a specific tiny block is dirty */
    public static final ResourceLocation CLIENTBOUND_DIRTY_BLOCK_UPDATE_PACKET = new ResourceLocation(TinyBlocksMod.MOD_ID, "clientbound-dirty-block-update-packet");


    /** Register the server's packets handlers */
    public static void init_server() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, CLIENTBOUND_DIRTY_BLOCK_UPDATE_PACKET, ((buf, context) -> {
            ClientStorageChunkManager.handleClientBoundDirtyChunkPacket(buf, context);
        }));
    }
    // CLIENT TO SERVER

    /** Requests a chunk to be stored in our own chunk cache */
    public static final ResourceLocation SERVERBOUND_BLOCK_CHUNK_REQUEST_PACKET = new ResourceLocation(TinyBlocksMod.MOD_ID, "serverbound-block-chunk-request-packet");

    /** Sends the tiny block pos and the storage block pos of the inner block that we want to break */
    public static final ResourceLocation SERVERBOUND_BREAK_INNER_BLOCK = new ResourceLocation(TinyBlocksMod.MOD_ID, "serverbound-break-inner-block");

    /** Register the client's packet handlers. */
    public static void init_client() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, SERVERBOUND_BLOCK_CHUNK_REQUEST_PACKET, ((buf, context) -> {
            BlockPos tinyBlockPos = BlockPos.of(buf.getLong(0));
            ServerStorageChunkManager.requestStorageChunk(context.getPlayer(), tinyBlockPos);
        }));

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, SERVERBOUND_BREAK_INNER_BLOCK, ((buf, context) -> {
            BlockPos innerBlockPos = buf.readBlockPos();
            BlockPos tinyBlockPos = buf.readBlockPos();

            // Get the block drops of the targeted inner block and give them to the player, if the player isn't in creative.
            if (!context.getPlayer().isCreative()) {
                ServerLevel level = (ServerLevel) context.getPlayer().level();
                Item blockItem = level.getBlockState(innerBlockPos).getBlock().asItem();
                context.getPlayer().getInventory().add(blockItem.getDefaultInstance());
            }

            // Break the block
            context.getPlayer().level().setBlockAndUpdate(innerBlockPos, Blocks.AIR.defaultBlockState());

            // Tell all the clients that have to chunk loaded to recalculate.
            ChunkPos chunkPos = context.getPlayer().level().getChunkAt(innerBlockPos).getPos();

            FriendlyByteBuf new_buf = new FriendlyByteBuf(Unpooled.buffer());
            new_buf.writeChunkPos(chunkPos);
            new_buf.writeBlockPos(tinyBlockPos);
        }));


    }
}