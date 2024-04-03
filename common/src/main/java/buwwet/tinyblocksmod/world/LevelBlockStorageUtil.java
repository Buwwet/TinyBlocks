package buwwet.tinyblocksmod.world;

import buwwet.tinyblocksmod.TinyBlocksMod;
import buwwet.tinyblocksmod.blocks.TinyBlock;
import buwwet.tinyblocksmod.blocks.entities.TinyBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Math;
import org.joml.Vector3f;

public class LevelBlockStorageUtil {

    public static int WORLD_STORAGE_START = 1000;

    // Returns either a 1 or a -1 whether the number is negative or positive
    public static int sinageInt(int i) {
        if (i < 0) {
            return -1;
        } else {
            return 1;
        }
    }

    // As we cannot expand the world height, we need to store four columns to represent just one XZ position,
    // With this we can get the world offset.
    public static Vec3i columnOffset(int tinyBlockHeight) {
        if (tinyBlockHeight < 32) {
            // I
            return new Vec3i(4, 0, 0);
        } else if (tinyBlockHeight < 128) {
            // II
            return new Vec3i(0, 0, 0);
        } else if (tinyBlockHeight < 224) {
            // III
            return new Vec3i(0, 0, 4);
        } else  {
            // IV
            return new Vec3i(4, 0, 4);
        }
    }

    /** Calculate the root of this tiny block's storage (4x4x4) */
    public static BlockPos getBlockStoragePosition(BlockPos blockPos) {


        // Get the start of the quadrant.
        int start_x = sinageInt(blockPos.getX()) * WORLD_STORAGE_START;
        int start_z = sinageInt(blockPos.getZ()) * WORLD_STORAGE_START;

        // For each XZ coordinate, we require 8x8x320 storage space for tiny blocks.
        int tinyblock_x = blockPos.getX() * 8;
        int tinyblock_z = blockPos.getZ() * 8;

        // Calculate the starting y of this tinyblock by using the modulo of how many tiny blocks we can fit in
        // one 4x4 column (96) times 4.
        // We need to add 64 at the start and remove 64 at the end for the modulo function to work propperly.
        int tinyblock_y = ((blockPos.getY() + 64) % 96) * 4 - 64;

        // Sum the column offset
        Vec3i tinyBlockPosition = new Vec3i(start_x + tinyblock_x, tinyblock_y, start_z + tinyblock_z).offset(columnOffset(blockPos.getY()));
        //TinyBlocksMod.LOGGER.info("WHAT + " + tinyBlockPosition);

        return new BlockPos(tinyBlockPosition);

    }

    /** Get a specific block's storage position that is within a tiny block */
    public static BlockPos getBlockStorageOfInnerBlock(BlockHitResult blockHitResult) {

        // Tiny block position
        BlockPos tinyBlockPos = blockHitResult.getBlockPos();

        // Take a step towards where we are facing (the direction is opposite to where we are facing).
        Vector3f step = blockHitResult.getDirection().step().div(-100.0f);
        Vector3f blockHitResultStepped = blockHitResult.getLocation().toVector3f().add(step);

        return getStoragePosOfBlockInside(tinyBlockPos, blockHitResultStepped);
    }

    /** Gets the storage position of the block inside a tiny block. Does not do any step, it will give you the middle of two blocks if you don't process the Vector3f beforehand. */
    public static BlockPos getStoragePosOfBlockInside(BlockPos tinyBlockPos, Vector3f hitPosition) {

        // Root block storage pos
        BlockPos rootStorageBlockPos = getBlockStoragePosition(tinyBlockPos);

        // Get the offsets (0-3)
        float x_offset = ((hitPosition.x - tinyBlockPos.getX()) * 4);
        float y_offset = ((hitPosition.y - tinyBlockPos.getY()) * 4);
        float z_offset = ((hitPosition.z - tinyBlockPos.getZ()) * 4);

        // If we face the limit of the block, it returns as a whole number, which makes our check not work.
        if (hitPosition.x == (double) tinyBlockPos.getX() + 1.0) {
            x_offset = 3.0f;
        }
        if (hitPosition.y == (double) tinyBlockPos.getY() + 1.0) {
            y_offset = 3.0f;
        }
        if (hitPosition.z == (double) tinyBlockPos.getZ() + 1.0) {
            z_offset = 3.0f;
        }


        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().gui.setOverlayMessage(Component.literal("x: " + x_offset + " y: " + y_offset + " z: " + z_offset), false);
            //Minecraft.getInstance().player.displayClientMessage(Component.literal("" + blockHitResult.getLocation().x), false);
        }

        return rootStorageBlockPos.offset((int) x_offset, (int) y_offset, (int) z_offset);
    }


    public static BlockPos getTinyBlockPositionFromStorageBlock(BlockPos sBlock) {

        // Get the start of the quadrant the block is in.
        int quadrant_x = sinageInt(sBlock.getX()) * WORLD_STORAGE_START;
        int quadrant_z = sinageInt(sBlock.getZ()) * WORLD_STORAGE_START;

        //int tinyBlock_x = (sBlock.getX() - quadrant_x - column_offset.getX()) / 8;
        //int tinyBlock_z = (sBlock.getZ() - quadrant_z - column_offset.getZ()) / 8;

        // If we ever hope to find y, we need to check the column offset.
        // But currently, I don't know how

        //TODO

        return null;
   }

    // Do some magic to get the inner block position of where we are looking at + our direction.
    // Then when we get the storage position of THAT subtract the direction so that we target the block that is required to then
    // place the block where we really want.

    public static void placeInnerBlock(Player player, BlockHitResult blockHitResult) {
        Vector3f directionStep = blockHitResult.getDirection().step().div(4.2f);

        Vector3f placeBlockInnerPos = blockHitResult.getLocation().toVector3f()
                .add(directionStep);

        // Get the tiny block position of the target.
        int tinyBlockX = (int) Math.floor(placeBlockInnerPos.x);
        int tinyBlockY = (int) Math.floor(placeBlockInnerPos.y);
        int tinyBlockZ = (int) Math.floor(placeBlockInnerPos.z);

        BlockPos targetTinyBlockPos = new BlockPos(tinyBlockX, tinyBlockY, tinyBlockZ);

        BlockPos targetedBlockPos = LevelBlockStorageUtil.getStoragePosOfBlockInside(targetTinyBlockPos, placeBlockInnerPos);

        BlockHitResult placeBlockHitResult = new BlockHitResult(
                new Vec3(placeBlockInnerPos.sub(directionStep)),
                blockHitResult.getDirection(),
                targetedBlockPos,
                false
        );

        // Check if the targeted tiny block pos is a replaceable block (like air) or an actual tiny block.
        Block targetBlockType = player.level().getBlockState(targetTinyBlockPos).getBlock();

        if (targetBlockType instanceof AirBlock || targetBlockType instanceof TinyBlock) {

            if (targetBlockType instanceof AirBlock) {
                // Replace the air block with a tiny block.
                player.level().setBlockAndUpdate(targetTinyBlockPos, TinyBlocksMod.TINY_BLOCK.get().defaultBlockState());
                // Do this before using the item, or the newly placed block will be cleared.
            }

            // Use the item
            InteractionResult itemUse = player.getMainHandItem().useOn(
                    new UseOnContext(
                            player, player.getUsedItemHand(), placeBlockHitResult
                    )
            );



            //if (targetBlockType instanceof TinyBlock) {
            // If the targeted host of the inner block is a tiny block, mark its shape as dirty.
            TinyBlockEntity targetBlockEntity = (TinyBlockEntity) player.level().getBlockEntity(targetTinyBlockPos);

            // Success! Make the targeted one dirty
            if (itemUse == InteractionResult.SUCCESS && targetBlockEntity != null) {
                targetBlockEntity.isShapeDirty = true;
            }
            // }
        }
    }
}
