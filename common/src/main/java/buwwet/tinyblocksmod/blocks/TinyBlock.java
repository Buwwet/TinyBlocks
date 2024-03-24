package buwwet.tinyblocksmod.blocks;

import buwwet.tinyblocksmod.TinyBlocksMod;
import buwwet.tinyblocksmod.blocks.entities.TinyBlockEntity;
import buwwet.tinyblocksmod.world.ClientStorageChunkManager;
import buwwet.tinyblocksmod.world.ServerStorageChunkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.concurrent.atomic.AtomicReference;

public class TinyBlock extends Block implements EntityBlock {

    //TODO loaded players

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

        // Don't run if not loaded
        if (tinyBlockEntity == null) {
            return super.getShape(blockState, blockGetter, blockPos, collisionContext);
        }
        if (!tinyBlockEntity.getLevel().isLoaded(tinyBlockEntity.getBlockStoragePosition())) {


            return  super.getShape(blockState, blockGetter, blockPos, collisionContext);
        }

        Level level = tinyBlockEntity.getLevel();
        BlockPos initialBlockPos = tinyBlockEntity.getBlockStoragePosition();
        AtomicReference<VoxelShape> finalVoxel = new AtomicReference<>(Shapes.box(0.0, 0.0, 0.0, 0.0, 0.0, 0.0));

        // Add all the shapes to ourselves
        for (int z_offset = 0; z_offset < 4; z_offset++) {
            for (int y_offset = 0; y_offset < 4; y_offset++) {
                for (int x_offset = 0; x_offset < 4; x_offset++) {
                    BlockPos storageBlockPos = initialBlockPos.offset(x_offset, y_offset, z_offset);
                    VoxelShape shape = level.getBlockState(storageBlockPos).getCollisionShape(blockGetter, storageBlockPos);

                    final Vector3d offset = new Vector3d(x_offset / 4.0f, y_offset / 4.0f, z_offset / 4.0);
                    // Create a scaled down box for each
                    shape.forAllBoxes((d, e, f, g, h, i) -> {
                        VoxelShape shrinkedShape = Shapes.box(d / 4, e / 4, f / 4, g / 4, h / 4, i / 4);
                        shrinkedShape = shrinkedShape.move(offset.x, offset.y, offset.z);

                        finalVoxel.set(Shapes.or(shrinkedShape, finalVoxel.get()));
                    });
                }
            }
        }

        return finalVoxel.get();


        //return Shapes.box(0.0, 0.0, 0.0, 1.0, 0.01, 1.0);
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

    @Nullable
    private TinyBlockEntity getBlockEntity(BlockGetter blockGetter, BlockPos blockPos) {
        if (blockGetter.getBlockEntity(blockPos) instanceof TinyBlockEntity) {
            TinyBlockEntity tinyBlockEntity = (TinyBlockEntity) blockGetter.getBlockEntity(blockPos);
            return tinyBlockEntity;
        } else {
            return null;
        }
    }


    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {



        // Don't run if we are not a TinyBlockEntity
        if (level.getBlockEntity(blockPos) instanceof TinyBlockEntity) {
        } else {
            return InteractionResult.CONSUME;
        }

        TinyBlockEntity tinyBlockEntity = (TinyBlockEntity) level.getBlockEntity(blockPos);
        tinyBlockEntity.incrementCounter();

        if (level.getServer() != null) {
            MinecraftServer server = level.getServer();
            ServerLevel serverLevel = (ServerLevel) level;
            ServerPlayer serverPlayer = (ServerPlayer) player;


            var chunkPos = new ChunkPos(tinyBlockEntity.getBlockStoragePosition());
            //serverLevel.setChunkForced(chunkPos.x, chunkPos.z, true);



            //TODO: we have to create a start that starts from the edge of the 4x4 column to one that goes to the bottom.
            // If we comepletely miss, but there is a block below, above, or whatever, add it anyways.
            //BlockHitResult epic = level.clip(new ClipContext(

            //));

            /*

            BlockHitResult newBlockHitResult = new BlockHitResult(
                    tinyBlockEntity.getBlockStoragePosition().getCenter(),
                    player.getDirection(),
                    tinyBlockEntity.getBlockStoragePosition(),
                    false
            );

            serverPlayer.getMainHandItem().useOn(
                    new UseOnContext(
                            serverPlayer,
                            interactionHand,
                            newBlockHitResult
                    )
            );*/

            //serverPlayer.


        } else {

        }







        return InteractionResult.CONSUME;
    }

    // We don't want to render the block, just the tile entity.
    @Override
    public boolean skipRendering(BlockState blockState, BlockState blockState2, Direction direction) {
        return true;
    }
}
