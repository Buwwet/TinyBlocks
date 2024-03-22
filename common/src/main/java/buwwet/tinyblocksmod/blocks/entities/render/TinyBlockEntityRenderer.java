package buwwet.tinyblocksmod.blocks.entities.render;

import buwwet.tinyblocksmod.blocks.entities.TinyBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class TinyBlockEntityRenderer implements BlockEntityRenderer<TinyBlockEntity>, BlockEntityRendererProvider<TinyBlockEntity> {

    BlockRenderDispatcher blockRenderDispatcher;
    public TinyBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        //NOT CORRECT!!! I just don't know how to register with context.
        blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
    }
    @Override
    public void render(TinyBlockEntity blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        poseStack.pushPose();

        // For our first test, we will be using the block on our right to "mimic", so we can master the rendering function before the wild stuff.


        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();


        for (int z_offset = 0; z_offset < 4; z_offset++) {
            for (int y_offset = 0; y_offset < 4; y_offset++) {
                for (int x_offset = 0; x_offset < 4; x_offset++) {
                    // Get the target block position
                    BlockPos targetPosition = blockEntity.getBlockPos().east().offset(x_offset, y_offset, z_offset);
                    // Check if there is a block entity there
                    BlockEntity targetBlockEntity = blockEntity.getLevel().getBlockEntity(targetPosition);
                    if (targetBlockEntity != null) {



                        // Now, check if the target block entity has a renderer.
                        BlockEntityRenderer<BlockEntity> blockEntityRenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(targetBlockEntity);
                        if (blockEntityRenderer != null) {

                            poseStack.pushPose();
                            // Move to the correct position and scale
                            poseStack.scale(0.25f, 0.25f, 0.25f);
                            poseStack.translate(x_offset, y_offset, z_offset);

                            // Steal the block entity renderer and make it do our bidding.
                            blockEntityRenderer.render(targetBlockEntity, f, poseStack, multiBufferSource, i, j);
                            poseStack.popPose();
                        } else {
                            // Render as a normal block here!
                            BlockState blockState = blockEntity.getLevel().getBlockState(targetPosition);
                            renderBlockWithOffset(blockState, poseStack, multiBufferSource, x_offset, y_offset, z_offset, i, j);
                        }
                    } else {
                        // Normal block render.
                        BlockState blockState = blockEntity.getLevel().getBlockState(targetPosition);
                        renderBlockWithOffset(blockState, poseStack, multiBufferSource, x_offset, y_offset, z_offset, i, j);
                    }
                }
            }
        }
        poseStack.popPose();
        /*
        // Get the last pose to get the matrices
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.LINES);
        debugVertex(vertexConsumer, matrix4f, matrix3f, 0.0f, 0.0f, 0.0f);
        debugVertex(vertexConsumer, matrix4f, matrix3f, 0.0f, 0.01f * blockEntity.getCounter(), 0.0f);
        */
    }

    void renderBlockWithOffset(BlockState blockState, PoseStack poseStack, MultiBufferSource multiBufferSource, int x_offset, int y_offset, int z_offset, int i, int j) {
        poseStack.pushPose();
        poseStack.scale(0.25f, 0.25f, 0.25f);
        poseStack.translate(x_offset, y_offset, z_offset);
        //TODO Find a way to batch render this!!!
        //blockRenderDispatcher.renderBatched();
        blockRenderDispatcher.renderSingleBlock(blockState, poseStack, multiBufferSource, i, j);
        poseStack.popPose();
    }



    // Draws a useful vertex by filling all the vertex's needs.
    // Needs a vertex consumer of RenderType.LINES and at least two vertexes to appear.
    void debugVertex(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f, float x, float y, float z) {
        vertexConsumer.vertex(matrix4f, x, y, z).color(255, 255, 255, 255).uv(0.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
    }

    @Override
    public BlockEntityRenderer<TinyBlockEntity> create(Context context) {

        return new TinyBlockEntityRenderer(context);
    }
}
