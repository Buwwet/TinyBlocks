package buwwet.tinyblocksmod.blocks.entities;

import buwwet.tinyblocksmod.TinyBlocksMod;
import buwwet.tinyblocksmod.world.ClientStorageChunkManager;
import buwwet.tinyblocksmod.world.LevelBlockStorageUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TinyBlockEntity extends BlockEntity {

    BlockPos blockStoragePosition;
    int state = 2;

    public TinyBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(TinyBlocksMod.TINY_BLOCK_ENTITY.get(), blockPos, blockState);

        System.out.println("Hello world, I'm a block entity! ");
        blockStoragePosition = LevelBlockStorageUtil.getBlockStoragePosition(blockPos);


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
                ClientStorageChunkManager.requestStorageChunk(getBlockPos(), getBlockStoragePosition());
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



}
