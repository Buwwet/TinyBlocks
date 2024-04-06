package buwwet.tinyblocksmod.blocks;

import buwwet.tinyblocksmod.TinyBlocksMod;
import buwwet.tinyblocksmod.blocks.entities.TinyBlockEntity;
import buwwet.tinyblocksmod.world.ClientStorageChunkManager;
import buwwet.tinyblocksmod.world.LevelBlockStorageUtil;
import buwwet.tinyblocksmod.world.ServerStorageChunkManager;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.concurrent.atomic.AtomicReference;

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
        if (tinyBlockEntity != null) {
            return tinyBlockEntity.getShape();
        }

        return super.getShape(blockState, blockGetter, blockPos, collisionContext);
    }

    @Override
    public VoxelShape getVisualShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return this.getShape(blockState, blockGetter, blockPos, collisionContext);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {

        TinyBlockEntity tinyBlockEntity = getBlockEntity(blockGetter, blockPos);
        if (tinyBlockEntity != null) {
            return tinyBlockEntity.getCollisionShape();
        }


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


    /** Clear a 4x4x4 space in storage when being created. */
    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        BlockPos storageBlockPos = LevelBlockStorageUtil.getBlockStoragePosition(blockPos);

        for (int z_offset = 0; z_offset < 4; z_offset++) {
            for (int y_offset = 0; y_offset < 4; y_offset++) {
                for (int x_offset = 0; x_offset < 4; x_offset++) {

                    BlockPos offsetStorageBlockPos = storageBlockPos.offset(x_offset, y_offset, z_offset);
                    level.setBlockAndUpdate(offsetStorageBlockPos, Blocks.AIR.defaultBlockState());

                }
            }
        }
    }

    @Override
    /** Runs when the player wants to INTERACT with a tiny block, not place it. */
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {


        // Let's get the tiny block position using the block hit result

        // Don't run if we are not a TinyBlockEntity
        if (level.getBlockEntity(blockPos) instanceof TinyBlockEntity) {
        } else {
            return InteractionResult.CONSUME;
        }

        TinyBlockEntity tinyBlockEntity = (TinyBlockEntity) level.getBlockEntity(blockPos);



        BlockPos resultingBlockPos = LevelBlockStorageUtil.getBlockStorageOfInnerBlock(blockHitResult);
        BlockState block = level.getBlockState(resultingBlockPos);

        // Generate a new block hit result targeting this block
        BlockHitResult newBlockHitResult = new BlockHitResult(
                resultingBlockPos.getCenter(),
                player.getDirection(),
                resultingBlockPos,
                false
        );

        // Get the result from interacting with this block.
        InteractionResult interactionResult = block.use(level, player, interactionHand, newBlockHitResult);

        // TODO: some tools have special interactions with some blocks (ex, axe with copper, so we need to add that check).
        if (interactionResult == InteractionResult.CONSUME) {
            // There must have been a change!
            tinyBlockEntity.isShapeDirty = true;
        }


        // Our interaction wasn't consumed, so we procede to placea block!
        if (interactionResult == InteractionResult.PASS) {
            if (player.getMainHandItem().getItem() instanceof BlockItem) {

                LevelBlockStorageUtil.placeInnerBlock(player, blockHitResult);

            }
        }

        // InteractionResult isn't viable when we are the client, so...
        if (level.isClientSide) {
            tinyBlockEntity.isShapeDirty = true;
        }

        return InteractionResult.CONSUME;
    }

    // We don't want to render the block, just the tile entity.
    @Override
    public boolean skipRendering(BlockState blockState, BlockState blockState2, Direction direction) {
        return true;
    }
}
