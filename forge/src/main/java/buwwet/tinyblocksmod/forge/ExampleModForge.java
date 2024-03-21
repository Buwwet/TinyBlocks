package buwwet.tinyblocksmod.forge;

import dev.architectury.platform.forge.EventBuses;
import buwwet.tinyblocksmod.TinyBlocksMod;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(TinyBlocksMod.MOD_ID)
public class ExampleModForge {
    public ExampleModForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(TinyBlocksMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        TinyBlocksMod.init();
    }
}
