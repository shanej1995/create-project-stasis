package com.shane.stasismod.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    // Chamber Energy Settings
    public static final ModConfigSpec.IntValue CHAMBER_CAPACITY;
    public static final ModConfigSpec.IntValue ACTIVATION_COST;
    public static final ModConfigSpec.IntValue MAX_TRANSFER_RATE;

    // Plot Settings
    public static final ModConfigSpec.IntValue MAX_PLAYERS;
    public static final ModConfigSpec.IntValue PLOT_SPACING;

    static {
        BUILDER.comment("Chamber Energy Settings - Server-side only, clients cannot edit without admin permission").push("energy");

        CHAMBER_CAPACITY = BUILDER
                .comment("Total FE capacity of chamber (Default: 100000)")
                .defineInRange("chamberCapacity", 100000, 1000, 1000000);

        ACTIVATION_COST = BUILDER
                .comment("FE cost to activate chamber and enter simulation (Default: 10000)")
                .defineInRange("activationCost", 10000, 100, 100000);

        MAX_TRANSFER_RATE = BUILDER
                .comment("Maximum FE transfer rate in and out per tick (Default: 500)")
                .defineInRange("maxTransferRate", 500, 1, 10000);

        BUILDER.pop();

        BUILDER.comment("Plot Settings - Server-side only").push("plots");

        MAX_PLAYERS = BUILDER
                .comment("Maximum number of players that can have plots. Plots are arranged in a square grid. (Default: 25)")
                .defineInRange("maxPlayers", 25, 1, 1000);

        PLOT_SPACING = BUILDER
                .comment("Distance in blocks between plot centers. Each plot is 1024 blocks. (Default: 2500)")
                .defineInRange("plotSpacing", 2500, 1500, 10000);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
