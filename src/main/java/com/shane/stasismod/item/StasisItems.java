package com.shane.stasismod.item;

import com.shane.stasismod.StasisMod;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registry for all Stasis Mod items.
 */
public class StasisItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(StasisMod.MODID);

    // Exit beacon - given to players in simulation dimension to exit
    public static final DeferredItem<ExitBeacon> EXIT_BEACON = ITEMS.register("exit_beacon",
        () -> new ExitBeacon(new Item.Properties()));
}
