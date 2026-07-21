# Multiblock Structure Reference

This file contains the original multiblock validation code in case you want to add it back in the future.

## Original Multiblock Validator

```java
package com.shane.stasismod.multiblock;

import com.shane.stasismod.StasisMod;
import com.shane.stasismod.block.ChamberBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Validates chamber multiblock structure.
 * Requires all 4 chamber block types adjacent to Activation Core:
 * - Stasis Tellurium
 * - Stabilizer Block
 * - Confinement Shell
 */
public class ChamberMultiblockValidator {

    public static boolean isValidChamber(BlockGetter level, BlockPos activationCorePos) {
        boolean hasStasis = false;
        boolean hasStabilizer = false;
        boolean hasConfinement = false;
        boolean hasAdjacent = false;

        // Check all horizontal adjacent blocks (north, south, east, west)
        for (Direction dir : Direction.values()) {
            if (dir.getAxis().isVertical()) continue;
            BlockPos neighborPos = activationCorePos.relative(dir);
            BlockState neighborState = level.getBlockState(neighborPos);
            String blockName = neighborState.getBlock().getName().getString();

            if (neighborState.getBlock() == ChamberBlocks.STASIS_TELLURIUM.get()) {
                hasStasis = true;
                hasAdjacent = true;
            } else if (neighborState.getBlock() == ChamberBlocks.STABILIZER_BLOCK.get()) {
                hasStabilizer = true;
                hasAdjacent = true;
            } else if (neighborState.getBlock() == ChamberBlocks.CONFINEMENT_SHELL.get()) {
                hasConfinement = true;
                hasAdjacent = true;
            }
        }

        // All 3 required blocks must be present and adjacent
        return hasStasis && hasStabilizer && hasConfinement && hasAdjacent;
    }

    public static String getValidationStatus(BlockGetter level, BlockPos activationCorePos) {
        int count = 0;
        StringBuilder status = new StringBuilder("§6Chamber Status: ");

        if (hasBlockType(level, activationCorePos, ChamberBlocks.STASIS_TELLURIUM.get())) {
            status.append("§aStasis ");
            count++;
        } else {
            status.append("§cStasis ");
        }

        if (hasBlockType(level, activationCorePos, ChamberBlocks.STABILIZER_BLOCK.get())) {
            status.append("§aStabilizer ");
            count++;
        } else {
            status.append("§cStabilizer ");
        }

        if (hasBlockType(level, activationCorePos, ChamberBlocks.CONFINEMENT_SHELL.get())) {
            status.append("§aConfinement");
            count++;
        } else {
            status.append("§cConfinement");
        }

        return status.toString() + " (" + count + "/3)";
    }

    private static boolean hasBlockType(BlockGetter level, BlockPos pos, net.minecraft.world.level.block.Block blockType) {
        for (Direction dir : Direction.values()) {
            if (dir.getAxis().isVertical()) continue;
            BlockPos neighborPos = pos.relative(dir);
            if (level.getBlockState(neighborPos).getBlock() == blockType) {
                return true;
            }
        }
        return false;
    }
}
```

## Where it was used

The multiblock validator was called in `ActivationCoreBlock.java` in the `use()` method to check if the chamber structure was complete before allowing activation. To re-enable, add the validation check back to that method.

## How to re-enable

1. Move this code back to `src/main/java/com/shane/stasismod/multiblock/ChamberMultiblockValidator.java`
2. In `ActivationCoreBlock.java`, add a check: `if (!ChamberMultiblockValidator.isValidChamber(level, pos)) { return InteractionResultHolder.fail(itemStack); }`
3. Display the validation status with: `ChamberMultiblockValidator.getValidationStatus(level, pos)`
4. The blocks (Stasis Tellurium, Stabilizer Block, Confinement Shell) will need to be registered in `ChamberBlocks.java`
