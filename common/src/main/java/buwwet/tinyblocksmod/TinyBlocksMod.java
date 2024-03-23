package buwwet.tinyblocksmod;

import buwwet.tinyblocksmod.blocks.TinyBlock;
import buwwet.tinyblocksmod.blocks.entities.TinyBlockEntity;
import buwwet.tinyblocksmod.blocks.entities.render.TinyBlockEntityRenderer;
import com.google.common.base.Suppliers;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Logger;


public class TinyBlocksMod {
    public static final String MOD_ID = "tinyblocks";


    public static final Logger LOGGER = Logger.getLogger(MOD_ID);




    // We can use this if we don't want to use DeferredRegister
    public static final Supplier<RegistrarManager> REGISTRIES = Suppliers.memoize(() -> RegistrarManager.get(MOD_ID));

    // Registering a new creative tab
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(MOD_ID, Registries.CREATIVE_MODE_TAB);
    public static final RegistrySupplier<CreativeModeTab> EXAMPLE_TAB = TABS.register("example_tab", () ->
            CreativeTabRegistry.create(Component.translatable("itemGroup." + MOD_ID + ".example_tab"),
                    () -> new ItemStack(TinyBlocksMod.EXAMPLE_ITEM.get())));
    
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, Registries.ITEM);
    // All the block magick
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(MOD_ID, Registries.BLOCK);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    //public static final DeferredRegister<?> BLOCK_ENTITY_RENDERERS = DeferredRegister.create(MOD_ID, Registries.BLOCK_ENTITY_TYPE)
    // Registering items
    public static final RegistrySupplier<Block> TINY_BLOCK = BLOCKS.register("tiny_block", () ->
       new TinyBlock(BlockBehaviour.Properties.of()
               .isSuffocating((blockState, blockGetter, blockPos) -> false)
               .isRedstoneConductor((blockState, blockGetter, blockPos) -> false)
               .dynamicShape()
               .noOcclusion()


       )
    );

    public static final RegistrySupplier<BlockEntityType<TinyBlockEntity>> TINY_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("tiny_block_entity", () ->
        BlockEntityType.Builder.of(TinyBlockEntity::new, TINY_BLOCK.get()).build(null)
    );
    public static final RegistrySupplier<Item> EXAMPLE_ITEM = ITEMS.register("example_item", () ->
            new Item(new Item.Properties().arch$tab(TinyBlocksMod.EXAMPLE_TAB)));
    
    public static void init() {
        TABS.register();
        ITEMS.register();
        BLOCKS.register();
        BLOCK_ENTITY_TYPES.register();

        // Register the rendering for the block entity.
        BlockEntityRendererRegistry.register(TINY_BLOCK_ENTITY.get(), new TinyBlockEntityRenderer(null));
        // Make the block not remove the faces of other blocks.
        RenderTypeRegistry.register(RenderType.translucent(), TINY_BLOCK.get());




        System.out.println(ExampleExpectPlatform.getConfigDirectory().toAbsolutePath().normalize().toString());
    }
}
