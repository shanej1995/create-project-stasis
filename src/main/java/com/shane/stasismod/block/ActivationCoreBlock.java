package com.shane.stasismod.block;

import com.shane.stasismod.blockentity.ChamberBlockEntities;
import com.shane.stasismod.blockentity.ChamberTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * The main chamber control block.
 * This block has a tile entity that manages the chamber state machine.
 * Rotates to face the direction the player is looking when placed.
 */
public class ActivationCoreBlock extends Block implements EntityBlock {
    public ActivationCoreBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.FACING, Direction.NORTH));
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        // Activation only through CC: Tweaked computer
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(
                net.minecraft.network.chat.Component.literal("§cPlace a CC: Tweaked computer on top and run 'stasis' to activate"),
                true
            );
        }

        return InteractionResult.FAIL;
    }


    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ChamberTile chamber) {
                chamber.onRemoved();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if (!level.isClientSide) {
            // Drop the block as an item
            popResource(level, pos, new ItemStack(ChamberBlocks.ACTIVATION_CORE_ITEM.get()));
        }
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChamberTile(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (blockEntityType != ChamberBlockEntities.CHAMBER_TILE.get()) {
            return null;
        }
        return level.isClientSide ? null : (lvl, pos, blockState, be) -> {
            if (be instanceof ChamberTile chamber) {
                ChamberTile.tick(lvl, pos, blockState, chamber);
            }
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        // Only allow horizontal placement (N, E, S, W)
        Direction playerLooking = context.getNearestLookingDirection().getOpposite();
        if (playerLooking == Direction.UP || playerLooking == Direction.DOWN) {
            playerLooking = Direction.NORTH; // Default to north if looking up/down
        }
        return this.defaultBlockState().setValue(BlockStateProperties.FACING, playerLooking);
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return false; // Don't cull neighboring block faces
    }

    @Override
    public net.minecraft.world.phys.shapes.VoxelShape getOcclusionShape(BlockState state, net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos pos) {
        return net.minecraft.world.phys.shapes.Shapes.empty(); // Empty occlusion shape prevents culling
    }
}
