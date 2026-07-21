package com.shane.stasismod.block;

import com.shane.stasismod.StasisMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.Registries;

/**
 * Chamber structure blocks.
 * - Activation Core: Control block with tile entity
 */
public class ChamberBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
        DeferredRegister.createBlocks(StasisMod.MODID);
    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(StasisMod.MODID);

    // Activation Core - Main control block (will have tile entity)
    public static final DeferredBlock<Block> ACTIVATION_CORE = BLOCKS.register("activation_core",
        () -> new ActivationCoreBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(3.0F, 6.0F)));

    public static final DeferredItem<BlockItem> ACTIVATION_CORE_ITEM = ITEMS.registerSimpleBlockItem("activation_core", ACTIVATION_CORE);
}
