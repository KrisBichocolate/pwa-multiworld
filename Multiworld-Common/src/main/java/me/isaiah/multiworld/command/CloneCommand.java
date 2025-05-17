package me.isaiah.multiworld.command;

import java.util.HashMap;
import java.util.Random;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
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
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.commons.io.FileUtils;

import static me.isaiah.multiworld.MultiworldMod.text;
import static me.isaiah.multiworld.MultiworldMod.text_plain;
import net.minecraft.server.world.ServerWorld;

import java.io.File;
import me.isaiah.multiworld.config.*;
import xyz.nucleoid.fantasy.mixin.MinecraftServerAccess;

public class CloneCommand {
    // Run Command
    public static int run(MinecraftServer mc, ServerCommandSource source, String[] args) {
        if (args.length != 3) {
            source.sendError(Text.literal("Usage: /mw clone <existing-id> <new-id>"));
            return 0;
        }

        String existing_str = args[1];
        String new_str = args[2];

        // Add namespace if missing
        if (existing_str.indexOf(':') == -1) {
            existing_str = "multiworld:" + existing_str;
        }
        if (new_str.indexOf(':') == -1) {
            new_str = "multiworld:" + new_str;
        }

        // Verify that existing_id exists
        if (!WorldConfig.getConfigFile(mc, existing_str).exists()) {
            source.sendError(Text.literal("Source world '" + existing_str + "' does not exist!"));
            return 0;
        }

        // Verify that new_id doesn't exist yet (either as config or loaded world)
        if (WorldConfig.getConfigFile(mc, new_str).exists()) {
            source.sendError(Text.literal("Destination world '" + new_str + "' already exists!"));
            return 0;
        }

        // Also check if world is loaded in server
        if (Util.isWorldLoaded(mc, new_str)) {
            source.sendError(Text.literal("Destination world '" + new_str + "' is already loaded in the server!"));
            return 0;
        }

        copy_world(mc, existing_str, new_str);
        copy_config(existing_str, new_str);

        // Set the new world as loaded
        try {
            WorldConfig.setLoaded(mc, new_str, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        CreateCommand.reinit_world_from_config(mc, new_str);

        source.sendMessage(text("Created world " + new_str + " as a clone of " + existing_str, Formatting.GREEN));

        return 1;
    }

    public static void copy_config(String existing_str, String new_str) {
        try {
            File existing_file = WorldConfig.getConfigFile(MultiworldMod.mc, existing_str);
            File new_file = WorldConfig.getConfigFile(MultiworldMod.mc, new_str);

            if (existing_file.exists()) {
                new_file.getParentFile().mkdirs();
                FileUtils.copyFile(existing_file, new_file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copy_world(MinecraftServer mc, String existing_str, String new_str) {
        RegistryKey<World> existing_key = RegistryKey.of(RegistryKeys.WORLD, Util.id(existing_str));
        RegistryKey<World> new_key = RegistryKey.of(RegistryKeys.WORLD, Util.id(new_str));

        LevelStorage.Session session = ((MinecraftServerAccess) mc).getSession();
        File existing_path = session.getWorldDirectory(existing_key).toFile();
        File new_path = session.getWorldDirectory(new_key).toFile();

        // Force existing world save
        ServerWorld world = mc.getWorld(existing_key);
        if (world != null) {
            // TODO: better error handling all around
            world.save(null, true, true);
        }

        try {
            FileUtils.copyDirectory(existing_path, new_path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
