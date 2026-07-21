package com.shane.stasismod.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Settings for the checkerboard dimension generator.
 * Defines which blocks to use for the 1-block alternating pattern.
 */
public class CheckerboardGeneratorSettings {

    public static final Codec<CheckerboardGeneratorSettings> CODEC =
        RecordCodecBuilder.create(builder ->
            builder.group(
                BuiltInRegistries.BLOCK.byNameCodec()
                    .fieldOf("block_light")
                    .orElse(Blocks.WHITE_CONCRETE)
                    .forGetter(CheckerboardGeneratorSettings::getBlockLight),
                BuiltInRegistries.BLOCK.byNameCodec()
                    .fieldOf("block_dark")
                    .orElse(Blocks.SNOW_BLOCK)
                    .forGetter(CheckerboardGeneratorSettings::getBlockDark)
            ).apply(builder, CheckerboardGeneratorSettings::new)
        );

    private final Block blockLight;
    private final Block blockDark;

    public CheckerboardGeneratorSettings(Block blockLight, Block blockDark) {
        this.blockLight = blockLight;
        this.blockDark = blockDark;
    }

    public CheckerboardGeneratorSettings() {
        this(Blocks.WHITE_CONCRETE, Blocks.SNOW_BLOCK);
    }

    public Block getBlockLight() {
        return blockLight;
    }

    public Block getBlockDark() {
        return blockDark;
    }

    /**
     * Get the block state for a given coordinate.
     * Creates a 3D checkerboard pattern: (x + y + z) % 2
     */
    public BlockState getBlockState(int x, int y, int z) {
        // 3D checkerboard: alternates based on (x + y + z) coordinate sum
        boolean isLight = (x + y + z) % 2 == 0;
        return isLight ? blockLight.defaultBlockState() : blockDark.defaultBlockState();
    }
}
