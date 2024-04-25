package buwwet.tinyblocksmod;

import buwwet.tinyblocksmod.blocks.TinyBlock;
import buwwet.tinyblocksmod.blocks.entities.TinyBlockEntity;
import buwwet.tinyblocksmod.blocks.entities.render.TinyBlockEntityRenderer;
import buwwet.tinyblocksmod.client.ClientBlockBreaking;
import buwwet.tinyblocksmod.networking.NetworkPackets;
import buwwet.tinyblocksmod.world.ClientStorageChunkManager;
import buwwet.tinyblocksmod.world.LevelBlockStorageUtil;
import buwwet.tinyblocksmod.world.ServerStorageChunkManager;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import io.netty.buffer.Unpooled;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Iterator;
import java.util.List;
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
               .strength(1000f)

       )
    );

    public static final RegistrySupplier<BlockEntityType<TinyBlockEntity>> TINY_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("tiny_block_entity", () ->
        BlockEntityType.Builder.of(TinyBlockEntity::new, TINY_BLOCK.get()).build(null)
    );
    public static final RegistrySupplier<Item> EXAMPLE_ITEM = ITEMS.register("example_item", () ->
            new Item(new Item.Properties().arch$tab(TinyBlocksMod.EXAMPLE_TAB)));


    public static final KeyMapping TOGGLE_SMALL_PLACING = new KeyMapping(
            "key.tinyblocksmod.toggle_tiny_placement",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_LALT,
            "category.tinymod.test"
    );

    public static void init() {
        TABS.register();
        ITEMS.register();
        BLOCKS.register();
        BLOCK_ENTITY_TYPES.register();

        KeyMappingRegistry.register(TOGGLE_SMALL_PLACING);

        // Init networking.
        NetworkPackets.init_client();
        NetworkPackets.init_server();

        // Register the rendering for the block entity.
        BlockEntityRendererRegistry.register(TINY_BLOCK_ENTITY.get(), new TinyBlockEntityRenderer(null));
        // Make the block not remove the faces of other blocks.
        RenderTypeRegistry.register(RenderType.translucent(), TINY_BLOCK.get());


        // PACKETS! Maybe move them somewhere else.


        //TODO thing that tells client to stop loading the chunk as they are too far away from the tiny block

        // EVENTS! Also maybe move them somewhere else.

        // CLIENT
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> {
            // Clear our custom caches.
            ClientStorageChunkManager.clearCache();
        });

         ClientRawInputEvent.MOUSE_CLICKED_PRE.register((minecraft, button, action, mods) -> {
             // Action 1 means that the button has started being pressed, while action 0 means it was released
             // Right click is button 1 while left click is button 0
             if (minecraft.player != null) {
                 //minecraft.player.displayClientMessage(Component.literal("button: " + button + " action: " + action + " is_valid: " + (Minecraft.getInstance().hitResult != null)),false);

                 if (minecraft.screen != null) {
                     return EventResult.pass();
                 }

                 // Placing (yay)
                 if (button == 1 && action == 1) {
                     if (Minecraft.getInstance().hitResult instanceof BlockHitResult) {
                         BlockHitResult hitResult = (BlockHitResult) Minecraft.getInstance().hitResult;

                         //TODO: tiny block placement mode check
                         if (!ClientBlockBreaking.placingTiny) {
                             return EventResult.pass();
                         }

                         if (hitResult.getType() == HitResult.Type.BLOCK) {
                             // Check if the targeted block in question is a tiny block
                             if (!(minecraft.level.getBlockState(hitResult.getBlockPos()).getBlock() instanceof TinyBlock)) {
                                 // Send the packet
                                 FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                                 buf.writeBlockHitResult(hitResult);
                                 NetworkManager.sendToServer(NetworkPackets.SERVERBOUND_PLACE_INNER_BLOCK, buf);

                                 // Do it ourselves, so that we remain in sync.
                                 LevelBlockStorageUtil.placeInnerBlock(Minecraft.getInstance().player, hitResult);
                                 return EventResult.interrupt(true);
                             }


                         }
                     }
                 }

                 // Breaking

                 // Stop breaking (even if we weren't looking at a tiny block.
                 if (button == 0 && action == 0) {
                     ClientBlockBreaking.stopBreaking();
                 }

                 if (button == 0 && action == 1) {


                     // Check if the block we are targeting to break is a tiny block.
                     if (Minecraft.getInstance().hitResult instanceof BlockHitResult) {
                         BlockHitResult hitResult = (BlockHitResult) Minecraft.getInstance().hitResult;

                         // We are starting to hit a tiny block!
                         if (Minecraft.getInstance().level.getBlockEntity(hitResult.getBlockPos()) instanceof TinyBlockEntity) {
                             ClientBlockBreaking.startBreaking();
                             return EventResult.interrupt(true);
                         }
                     }
                 }
             }
             return EventResult.pass();
         });

        ClientTickEvent.CLIENT_POST.register((minecraft) -> {
            ClientBlockBreaking.tick();

            ClientBlockBreaking.placingTiny = TOGGLE_SMALL_PLACING.isDown();
        });


        // SERVER EVENTS

        TickEvent.ServerLevelTick.SERVER_PRE.register((server) -> {
            if (server == null) {
                return;
            }

            server.getAllLevels().forEach(serverLevel -> {

                // Tick all the chunks that we forced to load.
                for (Iterator<ChunkPos> it = ServerStorageChunkManager.getExtraLoadedChunks(); it.hasNext(); ) {
                    ChunkPos chunkPos = it.next();
                    LevelChunk chunk = serverLevel.getChunk(chunkPos.x, chunkPos.z);
                    serverLevel.tickChunk(chunk, 1);
                }
            });
        });





        System.out.println(ExampleExpectPlatform.getConfigDirectory().toAbsolutePath().normalize().toString());
    }
}
