package buwwet.tinyblocksmod.fabric;

import buwwet.tinyblocksmod.TinyBlocksMod;
import net.fabricmc.api.ModInitializer;

public class TinyBlocksModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        TinyBlocksMod.init();
    }
}
