package com.shane.stasismod.event;

import com.shane.stasismod.StasisMod;
import com.shane.stasismod.dimension.StasisDimensions;
import com.shane.stasismod.plot.PlotBarrierBlocks;
import com.shane.stasismod.plot.PlotBorderRenderer;
import com.shane.stasismod.plot.PlotData;
import com.shane.stasismod.plot.PlotManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Renders plot borders when players are in the stasis dimension.
 */
@EventBusSubscriber(modid = StasisMod.MODID)
public class PlotBorderTickEvent {
    private static final Set<Integer> barriersGenerated = new HashSet<>();

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer() == null) {
            return;
        }

        // Get the stasis dimension
        ServerLevel stasisDim = event.getServer().getLevel(StasisDimensions.SIMULATION_LEVEL);
        if (stasisDim == null) {
            return;
        }

        // Render borders for all players in stasis dimension
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (player.level().dimension().equals(StasisDimensions.SIMULATION_LEVEL)) {
                // Get or allocate plot if needed
                PlotData plot = PlotManager.getInstance().getOrAllocatePlot(player.getUUID());
                if (plot != null) {
                    // Render particle borders
                    PlotBorderRenderer.renderPlotBorder(player, plot);
                }
            }
        }
    }
}
