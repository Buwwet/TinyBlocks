package buwwet.tinyblocksmod.networking;

import buwwet.tinyblocksmod.TinyBlocksMod;
import buwwet.tinyblocksmod.blocks.TinyBlock;
import buwwet.tinyblocksmod.blocks.entities.TinyBlockEntity;
import buwwet.tinyblocksmod.world.ClientStorageChunkManager;
import buwwet.tinyblocksmod.world.LevelBlockStorageUtil;
import buwwet.tinyblocksmod.world.ServerStorageChunkManager;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class NetworkPackets {

    // SERVER TO CLIENT
    /** Tells the client that a specific tiny block is dirty */
    public static final ResourceLocation CLIENTBOUND_DIRTY_BLOCK_UPDATE_PACKET = new ResourceLocation(TinyBlocksMod.MOD_ID, "clientbound-dirty-block-update-packet");


    /** Register the server's packets handlers */
    public static void init_client() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, CLIENTBOUND_DIRTY_BLOCK_UPDATE_PACKET, ((buf, context) -> {
            ClientStorageChunkManager.handleClientBoundDirtyChunkPacket(buf, context);
        }));
    }
    // CLIENT TO SERVER

    /** Requests a chunk to be stored in our own chunk cache */
    public static final ResourceLocation SERVERBOUND_BLOCK_CHUNK_REQUEST_PACKET = new ResourceLocation(TinyBlocksMod.MOD_ID, "serverbound-block-chunk-request-packet");

    /** Sends the tiny block pos and the storage block pos of the inner block that we want to break */
    public static final ResourceLocation SERVERBOUND_BREAK_INNER_BLOCK = new ResourceLocation(TinyBlocksMod.MOD_ID, "serverbound-break-inner-block");


    public static final ResourceLocation SERVERBOUND_PLACE_INNER_BLOCK = new ResourceLocation(TinyBlocksMod.MOD_ID, "serverbound-place-inner-block");

    /** Register the client's packet handlers. */
    public static void init_server() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, SERVERBOUND_BLOCK_CHUNK_REQUEST_PACKET, ((buf, context) -> {
            BlockPos tinyBlockPos = BlockPos.of(buf.getLong(0));
            ServerStorageChunkManager.requestStorageChunk(context.getPlayer(), tinyBlockPos);


        }));

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, SERVERBOUND_BREAK_INNER_BLOCK, ((buf, context) -> {

            // Read the data from the buffer as we are going to switch threads soon.
            BlockPos innerBlockPos = buf.readBlockPos();
            BlockPos tinyBlockPos = buf.readBlockPos();

            // Ask the main thread if it could run this for us (so that we can access BlockEntities)
            MinecraftServer server = context.getPlayer().level().getServer();
            server.executeIfPossible(() -> {
                ServerLevel level = (ServerLevel) context.getPlayer().level();

                // Get the block drops of the targeted inner block and give them to the player, if the player isn't in creative.
                if (!context.getPlayer().isCreative()) {
                    Item blockItem = level.getBlockState(innerBlockPos).getBlock().asItem();
                    context.getPlayer().getInventory().add(blockItem.getDefaultInstance());
                }

                // Remove the block from the world
                level.setBlockAndUpdate(innerBlockPos, Blocks.AIR.defaultBlockState());

                // Recalculate the shape!
                TinyBlockEntity tinyBlockEntity = (TinyBlockEntity) level.getBlockEntity(tinyBlockPos);
                tinyBlockEntity.isShapeDirty = true;


            });
        }));

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, SERVERBOUND_PLACE_INNER_BLOCK, (buf, context) -> {
            BlockHitResult blockHitResult = buf.readBlockHitResult();



            ServerLevel level = (ServerLevel) context.getPlayer().level();
            // Do nothing if the block that we are selecting is a tinyBlock, as the use() method handles that.
            if (level.getBlockState(blockHitResult.getBlockPos()).getBlock() instanceof TinyBlock) {
                return;
            }

            // This is the block where the transformations will take place.
            BlockPos targetBlockPos = blockHitResult.getBlockPos().offset(blockHitResult.getDirection().getNormal());

            Block targetBlock = level.getBlockState(targetBlockPos).getBlock();

            if (targetBlock instanceof AirBlock || targetBlock instanceof TinyBlock) {

                if (targetBlock instanceof AirBlock) {
                    level.setBlockAndUpdate(targetBlockPos, TinyBlocksMod.TINY_BLOCK.get().defaultBlockState());
                }

                // Place the inner block!
                LevelBlockStorageUtil.placeInnerBlock(context.getPlayer(), blockHitResult);
            }
        });


    }
}
