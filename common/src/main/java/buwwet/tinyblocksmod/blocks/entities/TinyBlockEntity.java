package buwwet.tinyblocksmod.blocks.entities;

import buwwet.tinyblocksmod.TinyBlocksMod;
import buwwet.tinyblocksmod.world.ClientStorageChunkManager;
import buwwet.tinyblocksmod.world.LevelBlockStorageUtil;
import buwwet.tinyblocksmod.world.ServerStorageChunkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.concurrent.atomic.AtomicReference;

/**
 * TinyBlockEntity manages the state, rendering and shape updates of the TinyBlock.
 */
public class TinyBlockEntity extends BlockEntity {

    /** Indicates where in the world this block stores its 4x4x4 blocks. */
    BlockPos blockStoragePosition;
    int state = 2;

    /** Shape to be used for collision */
    private VoxelShape shape;
    /** Determines if we have to recalculate our shape due to a block update. */
    public boolean isShapeDirty;


    public TinyBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(TinyBlocksMod.TINY_BLOCK_ENTITY.get(), blockPos, blockState);

        blockStoragePosition = LevelBlockStorageUtil.getBlockStoragePosition(blockPos);

        // Set up a placeholder shape.
        shape = Shapes.box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
        isShapeDirty = true;

        // Append ourselves to the server's list.
        if (Minecraft.getInstance().player == null) {
            ServerStorageChunkManager.addBlockListenerToChunk(this);
        }
    }

    @Override
    public void load(CompoundTag nbt) {

        state = nbt.getInt("initial");


    }




    @Override
    // Save our NBT to the world
    protected void saveAdditional(CompoundTag nbt) {
        // Add the current counter
        nbt.putInt("initial", state);
        super.saveAdditional(nbt);
    }

    @Override
    public CompoundTag getUpdateTag() {
        // Request chunk if we don't have it yet TODO
        if (Minecraft.getInstance().player != null) {
            if (!Minecraft.getInstance().level.isLoaded(blockStoragePosition)) {
                ClientStorageChunkManager.requestStorageChunk(this);
            }
        }

        // Return all data
        return this.saveWithoutMetadata();
    }

    @Nullable
    @Override
    // Send data when we request to sync
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }


    public void incrementCounter() {
        state += 1;

        // Update client!
        this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        // Mark ourselves as dirty, so that the server saves us!
        this.setChanged();

    }

    public int getCounter() {
        return state;
    }

    public BlockPos getBlockStoragePosition() {return blockStoragePosition; }


    /** Gets the shape of the block, recalculates if shape is marked as dirty. */
    public VoxelShape getShape() {

        if (isShapeDirty) {
            recalculateShape();
            isShapeDirty = false;
        }

        return shape;
    }

    /** Recalculates the shape used for collisions. */
    private void recalculateShape() {
        Level level = this.getLevel();
        BlockPos initialBlockPos = this.blockStoragePosition;
        AtomicReference<VoxelShape> finalVoxel = new AtomicReference<>(Shapes.box(0.0, 0.0, 0.0, 0.0, 0.0, 0.0));

        // Add all the shapes to ourselves
        for (int z_offset = 0; z_offset < 4; z_offset++) {
            for (int y_offset = 0; y_offset < 4; y_offset++) {
                for (int x_offset = 0; x_offset < 4; x_offset++) {
                    BlockPos storageBlockPos = initialBlockPos.offset(x_offset, y_offset, z_offset);
                    //TODO: special block blacklist (for ladders and such)
                    BlockState blockState = level.getBlockState(storageBlockPos);
                    VoxelShape shape = blockState.getCollisionShape(this.level, storageBlockPos);



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

        // Save the shape.
        shape = finalVoxel.get();
    }


}
