package buwwet.tinyblocksmod.world;

import buwwet.tinyblocksmod.TinyBlocksMod;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;

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

        // Root block storage pos
        BlockPos rootBlockPos = getBlockStoragePosition(blockHitResult.getBlockPos());

        double x_offset = ((blockHitResult.getLocation().x - Math.floor(blockHitResult.getLocation().x)) * 4);
        double y_offset = ((blockHitResult.getLocation().y - Math.floor(blockHitResult.getLocation().y)) * 4);
        double z_offset = ((blockHitResult.getLocation().z - Math.floor(blockHitResult.getLocation().z)) * 4);

        // If we face the limit of the block, it returns as a whole number, which makes our check not work.
        // TODO This is a band-aid fix, something smarter should be used instead!
        if (blockHitResult.getLocation().x == (double) blockHitResult.getBlockPos().getX() + 1.0) {
            x_offset = 3.0;
        }
        if (blockHitResult.getLocation().y == (double) blockHitResult.getBlockPos().getY() + 1.0) {
            y_offset = 3.0;
        }
        if (blockHitResult.getLocation().z == (double) blockHitResult.getBlockPos().getZ() + 1.0) {
            z_offset = 3.0;
        }


        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().gui.setOverlayMessage(Component.literal("x: " + x_offset + " y: " + y_offset + " z: " + z_offset), false);
            Minecraft.getInstance().player.displayClientMessage(Component.literal("" + blockHitResult.getLocation().x), false);
        }

        return rootBlockPos.offset((int) x_offset, (int) y_offset, (int) z_offset);
    }
}
