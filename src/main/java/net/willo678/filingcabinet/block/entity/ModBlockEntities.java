package net.willo678.filingcabinet.block.entity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.willo678.filingcabinet.FilingMod;
import net.willo678.filingcabinet.block.ModBlocks;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, FilingMod.MODID);

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }


    public static final RegistryObject<BlockEntityType<FilingCabinetBlockEntity>> FILING_CABINET =
            BLOCK_ENTITIES.register("filing_cabinet",
                    () -> BlockEntityType.Builder.of(FilingCabinetBlockEntity::new,
                            ModBlocks.FILING_CABINET.get()).build(null));





}
