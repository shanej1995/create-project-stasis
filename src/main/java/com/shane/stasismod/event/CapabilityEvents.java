package com.shane.stasismod.event;

import com.shane.stasismod.StasisMod;
import com.shane.stasismod.blockentity.ChamberBlockEntities;
import com.shane.stasismod.blockentity.ChamberTile;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = StasisMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class CapabilityEvents {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            ChamberBlockEntities.CHAMBER_TILE.get(),
            (blockEntity, side) -> blockEntity
        );
    }
}
