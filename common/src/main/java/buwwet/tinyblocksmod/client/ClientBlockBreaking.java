package buwwet.tinyblocksmod.client;

import buwwet.tinyblocksmod.TinyBlocksMod;
import buwwet.tinyblocksmod.blocks.entities.TinyBlockEntity;
import buwwet.tinyblocksmod.world.LevelBlockStorageUtil;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ClientBlockBreaking {
    public static boolean isBreaking = false;

    public static float breakProgress = 0;

    @Nullable
    public static BlockPos targetInnerBlock = null;
    @Nullable
    public static BlockPos tinyBlockPos = null;


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
                        breakProgress += 1.0f;
                    } else {
                        // We are looking at a new target!
                        reset();
                        targetInnerBlock = newInnerBlockPos;
                        tinyBlockPos = hitResult.getBlockPos();
                    }
                } else {
                    targetInnerBlock = newInnerBlockPos;
                    tinyBlockPos = hitResult.getBlockPos();
                }


            } else {
                reset();
            }

        } else {
            // We are hitting nothing.
            reset();
        }

        // Check if we broke a threshold or something
        if (breakProgress > 10.0) {
            breakBlock();
            reset();
        }
    }

    private static void breakBlock() {
        if (targetInnerBlock != null) {
            Minecraft.getInstance().player.displayClientMessage(Component.literal("Broke tiny block!"), false);

            // Update in the meantime.
            Minecraft.getInstance().level.setBlock(targetInnerBlock, Blocks.AIR.defaultBlockState(), 0);
            TinyBlockEntity tinyBlockEntity = (TinyBlockEntity) Minecraft.getInstance().level.getBlockEntity(tinyBlockPos);
            tinyBlockEntity.isShapeDirty = true;


            // Tell the server
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeBlockPos(targetInnerBlock);
            buf.writeBlockPos(tinyBlockPos);
            NetworkManager.sendToServer(TinyBlocksMod.SERVERBOUND_BREAK_INNER_BLOCK, buf);
        }
    }

    private static void reset() {
        targetInnerBlock = null;
        breakProgress = 0.0f;
        tinyBlockPos = null;
    }

    public static void startBreaking() {
        isBreaking = true;
    }

    public static void stopBreaking() {
        isBreaking = false;
        reset();
    }


}
