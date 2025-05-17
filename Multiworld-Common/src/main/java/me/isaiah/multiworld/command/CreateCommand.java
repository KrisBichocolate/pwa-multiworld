package me.isaiah.multiworld.command;

import java.util.HashMap;
import java.util.Random;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import me.isaiah.multiworld.MultiworldMod;

import static me.isaiah.multiworld.MultiworldMod.text;
import static me.isaiah.multiworld.MultiworldMod.text_plain;
import net.minecraft.server.world.ServerWorld;

import java.io.File;
import me.isaiah.multiworld.config.*;

public class CreateCommand {

    // Run Command
    public static int run(MinecraftServer mc, ServerCommandSource source, String[] args) {
        if (args.length <= 2) {
            source.sendError(Text.literal("Usage: /mw create <id> <world_preset> [<dimension>]"));
            return 0;
        }

        Random r = new Random();
        long seed = r.nextInt();

        // Parse preset and dimension keys from args
        String presetKey = args[2];
        String dimensionKey = args.length > 3 ? args[3] : "minecraft:overworld";

        // Get chunk generator and dimension ID based on preset and dimension keys
        ChunkGenerator gen = get_chunk_gen(mc, presetKey, dimensionKey);
        Identifier dimTypeKey = get_dim_type(presetKey, dimensionKey);

        // Check if chunk generator or dimension type is null
        if (gen == null) {
            source.sendError(Text.literal("Could not find chunk generator for preset '" + presetKey + "' and dimension '" + dimensionKey + "'"));
            return 0;
        }

        if (dimTypeKey == null) {
            source.sendError(Text.literal("Could not find dimension type for preset '" + presetKey + "' and dimension '" + dimensionKey + "'"));
            return 0;
        }

        String arg1 = args[1];
        if (arg1.indexOf(':') == -1) {
            arg1 = "multiworld:" + arg1;
        }

        // Check if world already exists (either loaded or has config)
        if (WorldConfig.getConfigFile(mc, arg1).exists() || Util.isWorldLoaded(mc, arg1)) {
            source.sendError(Text.literal("World '" + arg1 + "' already exists!"));
            return 0;
        }

        ServerWorld world = MultiworldMod.create_world(arg1, dimTypeKey, gen, Difficulty.NORMAL, seed);
        WorldConfig.createWorldConfig(mc, world, presetKey, dimensionKey, seed);

        source.sendMessage(text("Created world with id: " + args[1] + " using preset: " + presetKey + ", dimension: " + dimensionKey, Formatting.GREEN));

        return 1;
    }

    /**
     * Return a {@link Identifier} representing the dimension from the given preset and dimension key
     * 
     * @param presetKey The world preset key
     * @param dimensionKey The dimension key within the preset
     * @return The dimension type identifier or null if not found
     */
    public static Identifier get_dim_type(String presetKey, String dimensionKey) {
        return MultiworldMod.get_world_creator().get_dim_type(presetKey, dimensionKey);
    }

    /**
     * Return a {@link ChunkGenerator} for the given preset and dimension key
     * 
     * @param mc The Minecraft server
     * @param presetKey The world preset key
     * @param dimensionKey The dimension key within the preset
     * @return The chunk generator or null if not found
     */
    public static ChunkGenerator get_chunk_gen(MinecraftServer mc, String presetKey, String dimensionKey) {
        return MultiworldMod.get_world_creator().get_chunk_gen(mc, presetKey, dimensionKey);
    }

    public static void reinit_world_from_config(MinecraftServer mc, String id) {
        try {
            FileConfiguration config = WorldConfig.getConfig(mc, id);

            // Get preset and dimension keys from config
            String presetKey = config.getString("preset_key");
            String dimensionKey = config.getString("dimension_key");

            long seed = 0;
            try {
                seed = config.getLong("seed");
            } catch (Exception e) {
                seed = config.getInt("seed");
            }

            ChunkGenerator gen = get_chunk_gen(mc, presetKey, dimensionKey);
            Identifier dimTypeKey = get_dim_type(presetKey, dimensionKey);

            // Check if chunk generator or dimension type is null
            if (gen == null) {
                System.err.println("Could not find chunk generator for preset '" + presetKey + "' and dimension '" + dimensionKey + "' for world '" + id + "'");
                return;
            }

            if (dimTypeKey == null) {
                System.err.println("Could not find dimension type for preset '" + presetKey + "' and dimension '" + dimensionKey + "' for world '" + id + "'");
                return;
            }

            Difficulty d = Difficulty.NORMAL;

            // Set saved Difficulty
            if (config.is_set("difficulty")) {
                String di = config.getString("difficulty");

                // String to Difficulty
                if (di.equalsIgnoreCase("EASY"))     { d = Difficulty.EASY; }
                if (di.equalsIgnoreCase("HARD"))     { d = Difficulty.HARD; }
                if (di.equalsIgnoreCase("NORMAL"))   { d = Difficulty.NORMAL; }
                if (di.equalsIgnoreCase("PEACEFUL")) { d = Difficulty.PEACEFUL; }
            }

            ServerWorld world = MultiworldMod.create_world(id, dimTypeKey, gen, d, seed);

            if (GameruleCommand.keys.size() == 0) {
                GameruleCommand.setupServer(MultiworldMod.mc);
            }

            // Set Gamerules
            for (String name : GameruleCommand.keys.keySet()) {
                String key = "gamerule_" + name;

                if (config.is_set(key)) {

                    Object o = config.getObject(key);

                    // BoleanRule
                    if (o instanceof Boolean) {
                        o = ((Boolean) o) ? "true" : "false";
                    }

                    // IntRule
                    if (o instanceof Integer) {
                        o = String.valueOf((Integer) o);
                    }

                    GameruleCommand.set_gamerule_from_cfg(world, key, (String) o);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
