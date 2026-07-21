package com.shane.stasismod.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Custom chunk generator for the checkerboard dimension.
 * Generates a 1-block checkerboard pattern of white and light grey concrete
 * from Y=1 to Y=63, with bedrock at Y=0.
 */
public class CheckerboardLevelSource extends ChunkGenerator {

    public static final MapCodec<CheckerboardLevelSource> CODEC =
        RecordCodecBuilder.mapCodec(builder ->
            builder.group(
                RegistryOps.retrieveElement(Biomes.PLAINS),
                CheckerboardGeneratorSettings.CODEC
                    .fieldOf("settings")
                    .orElse(new CheckerboardGeneratorSettings())
                    .forGetter(CheckerboardLevelSource::getSettings)
            ).apply(builder, CheckerboardLevelSource::new)
        );

    private final CheckerboardGeneratorSettings settings;

    public CheckerboardLevelSource(Holder<Biome> biome, CheckerboardGeneratorSettings settings) {
        super(new FixedBiomeSource(biome));
        this.settings = settings;
    }

    public CheckerboardGeneratorSettings getSettings() {
        return settings;
    }

    @Override
    protected @NotNull MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void buildSurface(WorldGenRegion level, StructureManager structureManager, RandomState random, ChunkAccess chunk) {
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
    }

    @Override
    public @NotNull CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // Generate checkerboard from Y=0 to Y=63
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y <= 63; y++) {
                    pos.set(
                        chunk.getPos().getBlockX(x),
                        y,
                        chunk.getPos().getBlockZ(z)
                    );

                    BlockState blockState;
                    if (y == 0) {
                        blockState = net.minecraft.world.level.block.Blocks.BEDROCK.defaultBlockState();
                    } else {
                        blockState = settings.getBlockState(pos.getX(), y, pos.getZ());
                    }

                    chunk.setBlockState(pos, blockState, false);
                }
            }
        }

        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        return 63;
    }

    @Override
    public @NotNull NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor height, RandomState random) {
        return new NoiseColumn(height.getMinBuildHeight(), new BlockState[0]);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        info.add("Generator: Checkerboard");
    }

    @Override
    public void applyCarvers(WorldGenRegion level, long seed, RandomState random, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public int getGenDepth() {
        return 384;
    }

    @Override
    public int getSeaLevel() {
        return -63;
    }
}
