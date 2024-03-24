package buwwet.tinyblocksmod.world;

import buwwet.tinyblocksmod.TinyBlocksMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

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

    // Calculate the root of this tiny block's storage (4x4x4)
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
}
