package buwwet.tinyblocksmod.blocks.entities.render;

import buwwet.tinyblocksmod.blocks.entities.TinyBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.function.Supplier;

public class TinyBlockEntityRenderer implements BlockEntityRenderer<TinyBlockEntity>, BlockEntityRendererProvider<TinyBlockEntity> {

    public TinyBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }
    @Override
    public void render(TinyBlockEntity blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {

        poseStack.pushPose();

        // Get the last pose to get the matrix
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();

        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.LINES);
        vertexConsumer.vertex(matrix4f, 0.0f, 0.0f, 0.0f).color(255, 255, 255, 255).uv(0.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
        vertexConsumer.vertex(matrix4f, 0.0f, 0.01f * blockEntity.getCounter(), 0.0f).color(255, 255, 255, 255).uv(0.0f, 1.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();




        poseStack.popPose();
    }

    @Override
    public BlockEntityRenderer<TinyBlockEntity> create(Context context) {
        return new TinyBlockEntityRenderer(context);
    }
}
