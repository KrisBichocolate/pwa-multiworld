package me.isaiah.multiworld;

import me.isaiah.multiworld.command.Util;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public interface ICreator {

	/**
	 */
	public ServerWorld create_world(String id, Identifier dim, ChunkGenerator gen, Difficulty dif, long seed);

	public void delete_world(String id);

	public default boolean unload_world(String id) {
		return false; // unsupported
	}

	/**
	 */
	public BlockPos get_pos(double x, double y, double z);

	/**
	 */
	public default Text colored_literal(String txt, Formatting color) {
		try {
			return Text.of(txt).copy().formatted(color);
		} catch (Exception | IncompatibleClassChangeError e) {
			// MutableText interface was changed to a class in 1.19;
			// Incase for 1.18:
			return Text.of(txt);
		}
	}

	/**
	 */
	void teleleport(ServerPlayerEntity player, ServerWorld world, double x, double y, double z);

	/**
	 */
	void set_difficulty(String id, Difficulty dif);

    /**
     * Get the DimensionOptions for a given world preset and dimension
     * 
     * @param mc The Minecraft server
     * @param presetKey The world preset key
     * @param dimensionKey The dimension key within the preset
     * @return The DimensionOptions or null if not found
     */
    default DimensionOptions getDimensionOptions(MinecraftServer mc, String presetKey, String dimensionKey) {
        try {
            // Look up the WorldPreset from the registry
            Identifier presetId = Util.id(presetKey);
            RegistryKey<WorldPreset> presetRegistryKey = RegistryKey.of(RegistryKeys.WORLD_PRESET, presetId);
            Registry<WorldPreset> presetRegistry = mc.getRegistryManager().get(RegistryKeys.WORLD_PRESET);
            
            WorldPreset preset = presetRegistry.get(presetRegistryKey);
            if (preset == null) {
                return null;
            }
            
            // Create dimensions registry holder to access dimension options
            var dimensionsHolder = preset.createDimensionsRegistryHolder();
            
            // Get the dimension options from the preset
            Identifier dimId = Util.id(dimensionKey);
            RegistryKey<DimensionOptions> dimKey = RegistryKey.of(RegistryKeys.DIMENSION, dimId);
            
            return dimensionsHolder.getOrEmpty(dimKey).orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return a {@link ChunkGenerator} for the given world preset and dimension.
     * null if not found
     */
    default ChunkGenerator get_chunk_gen(MinecraftServer mc, String presetKey, String dimensionKey) {
        DimensionOptions dimOptions = getDimensionOptions(mc, presetKey, dimensionKey);
        return dimOptions != null ? dimOptions.chunkGenerator() : null;
    }

    /**
     * Return a dimension type key for the given world preset and dimension.
     * null if not found
     */
    default Identifier get_dim_type(String presetKey, String dimensionKey) {
        DimensionOptions dimOptions = getDimensionOptions(MultiworldMod.mc, presetKey, dimensionKey);
        if (dimOptions != null) {
            RegistryEntry<DimensionType> dimensionTypeEntry = dimOptions.dimensionTypeEntry();
            return dimensionTypeEntry.getKey().orElseThrow().getValue();
        }

        return null;
    }

    // TODO: move to icommonlib:
	public BlockPos get_spawn(ServerWorld world);
	public boolean is_the_end(ServerWorld world);
	
}
