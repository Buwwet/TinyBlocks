package buwwet.tinyblocksmod.blocks.entities.render;

import buwwet.tinyblocksmod.blocks.entities.TinyBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class TinyBlockEntityRenderer implements BlockEntityRenderer<TinyBlockEntity>, BlockEntityRendererProvider<TinyBlockEntity> {

    public TinyBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }
    @Override
    public void render(TinyBlockEntity blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        poseStack.pushPose();

        // For our first test, we will be using the block on our right to "mimic", so we can master the rendering function before the wild stuff.


        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();

        BlockPos target = blockEntity.getBlockPos().east();
        BlockEntity targetBlockEntity = blockEntity.getLevel().getBlockEntity(target);
        if (targetBlockEntity != null) {
            // Now, check if the target block entity has a renderer.
            BlockEntityRenderer<BlockEntity> blockEntityRenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(targetBlockEntity);
            if (blockEntityRenderer != null) {
                poseStack.scale(0.25f, 0.25f, 0.25f);
                blockEntityRenderer.render(targetBlockEntity, f, poseStack, multiBufferSource, i, j);
            } else {
                // Render as a normal block here!
            }
        } else {
            // Normal block render.
        }

        poseStack.popPose();
        /*


        // Get the last pose to get the matrices


        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.LINES);
        debugVertex(vertexConsumer, matrix4f, matrix3f, 0.0f, 0.0f, 0.0f);
        debugVertex(vertexConsumer, matrix4f, matrix3f, 0.0f, 0.01f * blockEntity.getCounter(), 0.0f);




        */
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
