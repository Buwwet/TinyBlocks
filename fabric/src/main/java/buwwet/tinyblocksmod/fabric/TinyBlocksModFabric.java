package buwwet.tinyblocksmod.fabric;

import buwwet.tinyblocksmod.TinyBlocksMod;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.fabricmc.api.ModInitializer;

public class TinyBlocksModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // Init the architectury mod part
        TinyBlocksMod.init();

    }
}
