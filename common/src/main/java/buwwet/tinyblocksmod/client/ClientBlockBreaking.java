package buwwet.tinyblocksmod.client;

import buwwet.tinyblocksmod.TinyBlocksMod;
import buwwet.tinyblocksmod.blocks.entities.TinyBlockEntity;
import buwwet.tinyblocksmod.networking.NetworkPackets;
import buwwet.tinyblocksmod.world.LevelBlockStorageUtil;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ClientBlockBreaking {
    public static boolean isBreaking = false;

    public static float breakProgress = 0;

    @Nullable
    public static BlockPos targetInnerBlock = null;
    @Nullable
    public static BlockPos tinyBlockPos = null;

    @Nullable
    public static float destroySpeed;


    public static void tick() {
        if (!isBreaking) {
            return;
        }

        if (Minecraft.getInstance().hitResult instanceof BlockHitResult) {
            BlockHitResult hitResult = (BlockHitResult) Minecraft.getInstance().hitResult;

            if (Minecraft.getInstance().level.getBlockEntity(hitResult.getBlockPos()) instanceof TinyBlockEntity) {
                // Check if the current inner block position matches with the one we are targeting.

                BlockPos newInnerBlockPos = LevelBlockStorageUtil.getBlockStorageOfInnerBlock(hitResult);

                if (targetInnerBlock != null) {
                    if (newInnerBlockPos.asLong() == targetInnerBlock.asLong()) {
                        // Tick the timer by an amount (idk how much), soo.
                        // Have to take into account hardness I think

                        breakProgress += destroySpeed;
                    } else {
                        // We are looking at a new target!
                        reset();
                        setNewBlock(hitResult.getBlockPos(), newInnerBlockPos);
                    }
                } else {
                    // We are starting to look at something
                    setNewBlock(hitResult.getBlockPos(), newInnerBlockPos);
                }


            } else {
                reset();
            }

        } else {
            // We are hitting nothing.
            reset();
        }

        // Check if we broke a threshold or something
        if (breakProgress > 20.0f) {
            breakBlock();
            reset();
        }

        // Creative mode does not care about block strength calculations
        if (breakProgress > 3.0f && Minecraft.getInstance().player.isCreative()) {
            breakBlock();
            reset();
        }
    }

    private static void setNewBlock(BlockPos newTinyBlockPos, BlockPos newTargetBlockPos) {
        targetInnerBlock = newTargetBlockPos;
        tinyBlockPos = newTinyBlockPos;

        // Get the block strength
        BlockState blockState = Minecraft.getInstance().level.getBlockState(targetInnerBlock);
        destroySpeed = Minecraft.getInstance().player.getMainHandItem().getDestroySpeed(blockState);
    }

    private static void breakBlock() {
        if (targetInnerBlock != null) {
            //Minecraft.getInstance().player.displayClientMessage(Component.literal("Broke tiny block!"), false);

            // Update in the meantime.
            Minecraft.getInstance().level.setBlock(targetInnerBlock, Blocks.AIR.defaultBlockState(), 0);
            TinyBlockEntity tinyBlockEntity = (TinyBlockEntity) Minecraft.getInstance().level.getBlockEntity(tinyBlockPos);
            tinyBlockEntity.isShapeDirty = true;


            // Tell the server
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeBlockPos(targetInnerBlock);
            buf.writeBlockPos(tinyBlockPos);
            NetworkManager.sendToServer(NetworkPackets.SERVERBOUND_BREAK_INNER_BLOCK, buf);
        }
    }

    private static void reset() {
        targetInnerBlock = null;
        breakProgress = 0.0f;
        tinyBlockPos = null;
    }

    public static void startBreaking() {

        isBreaking = true;

        if (Minecraft.getInstance().player.isCreative()) {
            breakProgress = 100.0f;
        }
    }

    public static void stopBreaking() {
        isBreaking = false;
        reset();
    }


}
