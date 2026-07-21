package com.shane.stasismod.blockentity;

import com.shane.stasismod.StasisMod;
import com.shane.stasismod.block.ChamberBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ChamberBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, StasisMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChamberTile>> CHAMBER_TILE =
        BLOCK_ENTITIES.register("chamber_tile",
            () -> BlockEntityType.Builder.of(
                ChamberTile::new,
                ChamberBlocks.ACTIVATION_CORE.get()
            ).build(null)
        );
}
