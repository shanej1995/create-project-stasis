package com.shane.stasismod.event;

import com.shane.stasismod.StasisMod;
import com.shane.stasismod.blockentity.ChamberTile;
import com.shane.stasismod.blockentity.ChamberBlockEntities;
import com.shane.stasismod.peripheral.ChamberPeripheral;
import dan200.computercraft.api.peripheral.PeripheralCapability;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Handles capability registration for the Stasis Chamber peripheral.
 */
@EventBusSubscriber(modid = StasisMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ChamberCapabilityHandler {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Register the CC: Tweaked peripheral capability for the chamber block entity
        event.registerBlockEntity(
            PeripheralCapability.get(),
            ChamberBlockEntities.CHAMBER_TILE.get(),
            (chamber, direction) -> new ChamberPeripheral(chamber, chamber.getLevel())
        );
    }
}
