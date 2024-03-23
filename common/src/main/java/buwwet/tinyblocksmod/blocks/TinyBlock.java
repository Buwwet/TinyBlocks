package buwwet.tinyblocksmod.blocks;

import buwwet.tinyblocksmod.TinyBlocksMod;
import buwwet.tinyblocksmod.blocks.entities.TinyBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class TinyBlock extends Block implements EntityBlock {

    public TinyBlock(Properties properties) {

        super(properties);


    }

    @Override
    public boolean hasDynamicShape() {
        return super.hasDynamicShape();
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {


        TinyBlockEntity tinyBlockEntity = getBlockEntity(blockGetter, blockPos);

        if (tinyBlockEntity == null) {
            return super.getShape(blockState, blockGetter, blockPos, collisionContext);
        }

        int counter = tinyBlockEntity.getCounter();

        return Shapes.box(0.0, 0.0, 0.0, 1.0, 0.01 * counter, 1.0);
        //return super.getCollisionShape(blockState, blockGetter, blockPos, collisionContext);
    }

    @Override
    public VoxelShape getVisualShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return this.getShape(blockState, blockGetter, blockPos, collisionContext);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return this.getShape(blockState, blockGetter, blockPos, collisionContext);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return TinyBlocksMod.TINY_BLOCK_ENTITY.get().create(blockPos, blockState);
    }

    private TinyBlockEntity getBlockEntity(BlockGetter blockGetter, BlockPos blockPos) {
        TinyBlockEntity tinyBlockEntity = (TinyBlockEntity) blockGetter.getBlockEntity(blockPos);
        return tinyBlockEntity;
    }


    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        System.out.println("Player clicked us!");
        TinyBlockEntity tinyBlockEntity = (TinyBlockEntity) level.getBlockEntity(blockPos);
        tinyBlockEntity.incrementCounter();

        if (level.getServer() != null) {
            MinecraftServer server = level.getServer();
            ServerLevel serverLevel = (ServerLevel) level;
            ServerPlayer serverPlayer = (ServerPlayer) player;

            var chunkPos = new ChunkPos(tinyBlockEntity.getBlockStoragePosition());
            serverLevel.setChunkForced(chunkPos.x, chunkPos.z, true);

            //server.getConnection().getConnections()
            //serverPlayer.connection.disconnect(Component.literal("byebye"));


            // okay we are sending it
            serverPlayer.connection.send(
                    new ClientboundLevelChunkWithLightPacket(
                            serverLevel.getChunkAt(tinyBlockEntity.getBlockStoragePosition()),
                            serverLevel.getLightEngine(),
                            null, null
                    )
            );

            TinyBlocksMod.LOGGER.info("AAAAAAAAAAA " + serverLevel.isLoaded(tinyBlockEntity.getBlockStoragePosition()));


        }

        return super.use(blockState, level, blockPos, player, interactionHand, blockHitResult);
    }

    // We don't want to render the block, just the tile entity.
    @Override
    public boolean skipRendering(BlockState blockState, BlockState blockState2, Direction direction) {
        return true;
    }
}
