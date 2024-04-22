package buwwet.tinyblocksmod.fabric;

import buwwet.tinyblocksmod.TinyBlocksMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class TinyBlocksModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // Init the architectury mod part
        TinyBlocksMod.init();

        WorldRenderEvents.LAST.register((last) -> {
            PoseStack poseStack = last.matrixStack();
            MultiBufferSource multiBufferSource = last.consumers();



            Vector3f start_pos = last.camera().getLookVector();
            Vec3 end = new Vec3(3.0, 2.0, 12.0);

            //drawLine(multiBufferSource, poseStack, (float) start_pos.x, (float) start_pos.y, (float) start_pos.z, (float) end.x, (float) end.y, (float) end.z);


            Level level = Minecraft.getInstance().level;

            if (level != null) {
                Vec3 playerPos = Minecraft.getInstance().player.position();

                /*
                BlockHitResult blockHitResult = level.isBlockInLine(
                        new ClipBlockStateContext(lookStartPos, lookEndPos, blockState -> {
                            if (blockState.getBlock() instanceof AirBlock) {
                                return false;
                            }
                            return true;
                        })
                );*/
                if (Minecraft.getInstance().hitResult instanceof BlockHitResult) {
                    if (Minecraft.getInstance().hitResult.getType() == HitResult.Type.BLOCK) {
                        Vec3 hitLocation = Minecraft.getInstance().hitResult.getLocation();


                        // Start rendering the line on the player position.

                        Vec3 cameraPos = last.camera().getPosition();
                        Vector3f viewVector = last.camera().getLookVector();

                        drawBox(multiBufferSource, poseStack,

                                (float) (snapToFourths((float) hitLocation.x) - cameraPos.x) + viewVector.x,
                                (float) (hitLocation.y - cameraPos.y) + viewVector.y,
                                (float) (snapToFourths((float) hitLocation.z) - cameraPos.z) + + viewVector.z,
                                0.25f);

                    }
                }




            }
        });

    }

    float snapToFourths(float num) {
        float decimals = num - (int) num;
        float digits = (float) Math.floor(num);

        // Snap the decimals to fourths.
        float fourths = (float) Math.floor(decimals * 4.0f) / 4.0f;

        return digits + fourths;
    }

    // TODO: move these
    void drawBox(MultiBufferSource multiBufferSource, PoseStack poseStack, float x, float y, float z, float size) {
        poseStack.pushPose();

        // Bottom Face
        drawLine(multiBufferSource, poseStack, x, y, z, x + size, y, z);
        drawLine(multiBufferSource, poseStack, x + size, y, z, x + size, y, z + size);
        drawLine(multiBufferSource, poseStack, x + size, y, z + size, x, y, z + size);
        drawLine(multiBufferSource, poseStack, x, y, z + size, x, y, z);

        // Top face
        drawLine(multiBufferSource, poseStack, x, y + size, z, x + size, y + size , z);
        drawLine(multiBufferSource, poseStack, x + size, y + size, z, x + size, y + size, z + size);
        drawLine(multiBufferSource, poseStack, x + size, y + size, z + size, x, y + size, z + size);
        drawLine(multiBufferSource, poseStack, x, y + size, z + size, x, y + size, z);

        // Columns
        drawLine(multiBufferSource, poseStack, x, y, z, x, y + size, z);
        drawLine(multiBufferSource, poseStack, x + size, y, z, x + size, y + size, z);
        drawLine(multiBufferSource, poseStack, x, y, z + size, x, y + size, z + size);
        drawLine(multiBufferSource, poseStack, x + size, y, z + size, x + size, y + size, z + size);


        poseStack.popPose();
    }
    void drawLine(MultiBufferSource multiBufferSource, PoseStack poseStack, float x, float y, float z, float xx, float yy, float zz) {
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.debugLineStrip(0.7));

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();

        debugVertex(vertexConsumer, matrix4f, matrix3f, x, y, z);
        debugVertex(vertexConsumer, matrix4f, matrix3f, xx, yy, zz);
    }
    void debugVertex(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f, float x, float y, float z) {
        vertexConsumer.vertex(matrix4f, x, y, z).color(255, 255, 255, 255).uv(0.0f, 1.0f).endVertex();
    }
}
