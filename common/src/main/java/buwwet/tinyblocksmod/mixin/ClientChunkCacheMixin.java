package buwwet.tinyblocksmod.mixin;

import buwwet.tinyblocksmod.TinyBlocksMod;
import net.minecraft.client.multiplayer.ClientChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {"net.minecraft.client.multiplayer.ClientChunkCache$Storage"})
public class ClientChunkCacheMixin {

    @Inject(method = "inRange", at= @At(value = "RETURN"), cancellable = true)
    public void permitExtraChunks(int i, int j, CallbackInfoReturnable<Boolean> cir) {

        //TinyBlocksMod.LOGGER.info("" + i + " " + j);
        //TODO: Keep this loaded ONLY if it is in a list of permited chunks, which will be
        // updated whenever a block goes in or out of view distance.

        cir.setReturnValue(true);

    }

}
