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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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

        block.use(level, player, interactionHand, newBlockHitResult);

        if (level.getServer() == null) {
            //Minecraft.getInstance().player.displayClientMessage(Component.literal(block.getBlock().getName().toString()), false);
        }

        //level.setBlockAndUpdate(resultingBlockPos, Blocks.DIAMOND_BLOCK.defaultBlockState());



        if (level.getServer() != null) {


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
