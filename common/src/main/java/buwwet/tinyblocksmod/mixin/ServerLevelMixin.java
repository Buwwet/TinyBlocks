package buwwet.tinyblocksmod.mixin;

import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @Inject(method = "shouldTickBlocksAt", cancellable = true, at = @At("HEAD"))
    private void shouldTickChunkOverride(long l, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true); //not working
        cir.cancel();
    }
}
