package com.shane.stasismod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.shane.stasismod.StasisMod;
import com.shane.stasismod.block.ChamberBlocks;
import com.shane.stasismod.blockentity.ChamberTile;
import com.shane.stasismod.player.PlayerChamberMapping;
import com.shane.stasismod.player.PlayerStateManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public class StasisCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("stasis")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("debug")
                .then(Commands.literal("enter")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("chamber", BlockPosArgument.blockPos())
                            .executes(StasisCommand::executeEnterDebug))))
                .then(Commands.literal("exit")
                    .then(Commands.argument("player", EntityArgument.player())
                        .executes(StasisCommand::executeExitDebug)))
                .then(Commands.literal("check")
                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(StasisCommand::executeCheckDebug)))
                .then(Commands.literal("resetchamber")
                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(StasisCommand::executeResetChamber)))
                .then(Commands.literal("checkerboard")
                    .then(Commands.argument("radius", net.minecraft.commands.arguments.coordinates.BlockPosArgument.blockPos())
                        .executes(StasisCommand::executeCheckerboard)))
                .then(Commands.literal("plot")
                    .then(Commands.argument("player", EntityArgument.player())
                        .executes(StasisCommand::executePlotDebug)))
                .then(Commands.literal("forcerestore")
                    .then(Commands.argument("player", EntityArgument.player())
                        .executes(StasisCommand::executeForceRestore)))));
    }

    private static int executeEnterDebug(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            BlockPos chamberPos = BlockPosArgument.getBlockPos(context, "chamber");
            CommandSourceStack source = context.getSource();
            ServerLevel level = source.getLevel();

            source.sendSuccess(
                () -> Component.literal("§e[Stasis Debug] Entering simulation: " + player.getName().getString()),
                true
            );

            // Get chamber block entity
            BlockEntity be = level.getBlockEntity(chamberPos);
            if (!(be instanceof ChamberTile chamber)) {
                source.sendFailure(Component.literal("§cNo chamber found at that position"));
                return 0;
            }

            // Activate the chamber
            if (chamber.handleActivation(player)) {
                source.sendSuccess(
                    () -> Component.literal("§aPlayer successfully entered simulation"),
                    true
                );
                return 1;
            } else {
                source.sendFailure(Component.literal("§cFailed to activate chamber"));
                return 0;
            }

        } catch (Exception e) {
            StasisMod.LOGGER.error("Debug enter command failed", e);
            context.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }

    private static int executeExitDebug(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            CommandSourceStack source = context.getSource();

            source.sendSuccess(
                () -> Component.literal("§e[Stasis Debug] Exiting simulation: " + player.getName().getString()),
                true
            );

            // Check if player has a saved state
            if (!PlayerStateManager.hasSavedState(player.getUUID())) {
                source.sendFailure(Component.literal("§cPlayer has no saved state"));
                return 0;
            }

            // Restore player from simulation
            if (PlayerStateManager.restoreFromSimulation(player)) {
                source.sendSuccess(
                    () -> Component.literal("§aPlayer successfully exited!"),
                    true
                );
                return 1;
            } else {
                source.sendFailure(Component.literal("§cFailed to restore player"));
                return 0;
            }

        } catch (Exception e) {
            StasisMod.LOGGER.error("Debug exit command failed", e);
            context.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }

    private static int executeCheckDebug(CommandContext<CommandSourceStack> context) {
        try {
            BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");
            CommandSourceStack source = context.getSource();
            ServerLevel level = source.getLevel();

            source.sendSuccess(
                () -> Component.literal("§e[Stasis Debug] Checking adjacent blocks to " + pos + ":"),
                true
            );

            // Check all 4 cardinal directions
            net.minecraft.core.Direction[] directions = {
                net.minecraft.core.Direction.NORTH,
                net.minecraft.core.Direction.SOUTH,
                net.minecraft.core.Direction.EAST,
                net.minecraft.core.Direction.WEST
            };

            boolean hasStasis = false, hasStab = false, hasConf = false;

            for (net.minecraft.core.Direction dir : directions) {
                BlockPos neighborPos = pos.relative(dir);
                net.minecraft.world.level.block.Block block = level.getBlockState(neighborPos).getBlock();
                String blockName = block.getName().getString();
                source.sendSuccess(
                    () -> Component.literal("§6  " + dir.getName() + ": " + blockName),
                    false
                );
            }

            source.sendSuccess(
                () -> Component.literal("§aMultiblock structure check disabled"),
                false
            );

            return 1;
        } catch (Exception e) {
            StasisMod.LOGGER.error("Debug check command failed", e);
            context.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }

    private static int executeResetChamber(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");
            ServerLevel level = source.getLevel();

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof ChamberTile chamber)) {
                source.sendFailure(Component.literal("§cNo chamber at that position"));
                return 0;
            }

            source.sendSuccess(
                () -> Component.literal("§aNo chamber state to reset - chamber is stateless"),
                true
            );
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }

    private static int executeCheckerboard(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            BlockPos center = BlockPosArgument.getBlockPos(context, "radius");
            ServerLevel level = source.getLevel();

            int radius = Math.max(16, Math.min(512, Math.abs(center.getX())));
            int startX = -radius;
            int startZ = -radius;
            int endX = radius;
            int endZ = radius;
            int groundY = 63;  // Ocean level

            source.sendSuccess(
                () -> Component.literal("§eGenerating checkerboard pattern (radius " + radius + ")..."),
                true
            );

            int blocksPlaced = 0;
            for (int x = startX; x <= endX; x++) {
                for (int z = startZ; z <= endZ; z++) {
                    // Checkerboard: alternate white and light gray per block
                    boolean isWhite = ((x >> 4) + (z >> 4)) % 2 == 0;  // Divide by 16 using bit shift
                    net.minecraft.world.level.block.Block block = isWhite ?
                        net.minecraft.world.level.block.Blocks.WHITE_CONCRETE :
                        net.minecraft.world.level.block.Blocks.LIGHT_GRAY_CONCRETE;

                    level.setBlock(new BlockPos(x, groundY, z), block.defaultBlockState(), 3);
                    blocksPlaced++;
                }
            }

            final int finalCount = blocksPlaced;
            source.sendSuccess(
                () -> Component.literal("§aPlaced " + finalCount + " blocks at Y=" + groundY),
                true
            );

            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }

    private static int executePlotDebug(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            CommandSourceStack source = context.getSource();

            source.sendSuccess(
                () -> Component.literal("§e[Stasis Debug] Plot info for: " + player.getName().getString()),
                true
            );

            // TODO: Implement actual plot info logic
            source.sendSuccess(
                () -> Component.literal("§cNOT YET IMPLEMENTED - Plot system incomplete"),
                false
            );

            return 1;
        } catch (Exception e) {
            StasisMod.LOGGER.error("Debug plot command failed", e);
            context.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * Force restores a player from simulation if they're stuck.
     * Admin-only command for player recovery. Verifies player is in simulation before restoring.
     */
    private static int executeForceRestore(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            CommandSourceStack source = context.getSource();

            // Check if player has a saved state
            if (!PlayerStateManager.hasSavedState(player.getUUID())) {
                source.sendFailure(Component.literal("§cNo saved state for " + player.getName().getString()));
                return 0;
            }

            // Check if player is in simulation dimension
            if (!player.level().dimension().equals(com.shane.stasismod.dimension.StasisDimensions.SIMULATION_LEVEL)) {
                source.sendFailure(Component.literal("§cPlayer is not in simulation dimension"));
                return 0;
            }

            source.sendSuccess(
                () -> Component.literal("§e[Admin] Force restoring " + player.getName().getString() + " from simulation..."),
                true
            );

            // Restore player from simulation
            if (PlayerStateManager.restoreFromSimulation(player)) {
                // Verify player is now in overworld (not simulation)
                if (!player.level().dimension().equals(com.shane.stasismod.dimension.StasisDimensions.SIMULATION_LEVEL)) {
                    // Only clear snapshot after successful restoration AND verification they're out
                    com.shane.stasismod.player.PlayerDataCache.getInstance().removeSnapshot(player.getUUID());
                    PlayerChamberMapping.getInstance().removeChamber(player.getUUID());

                    source.sendSuccess(
                        () -> Component.literal("§aForce restore successful! Player restored to original location."),
                        true
                    );
                    StasisMod.LOGGER.warn("Admin {} force-restored player {}", source.getEntity() != null ? source.getEntity().getName().getString() : "console", player.getName().getString());
                    return 1;
                } else {
                    source.sendFailure(Component.literal("§cRestore failed: Player still in simulation dimension"));
                    return 0;
                }
            } else {
                source.sendFailure(Component.literal("§cFailed to restore player state"));
                return 0;
            }

        } catch (Exception e) {
            StasisMod.LOGGER.error("Force restore command failed", e);
            context.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }
}
