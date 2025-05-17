package me.isaiah.multiworld.command;

import java.util.HashMap;
import java.util.Random;

import me.isaiah.multiworld.ICreator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.Difficulty;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import me.isaiah.multiworld.MultiworldMod;

import static me.isaiah.multiworld.MultiworldMod.text;
import static me.isaiah.multiworld.MultiworldMod.text_plain;
import net.minecraft.server.world.ServerWorld;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;
import me.isaiah.multiworld.config.*;

public class DeleteCommand {

    // Run Command
    public static int run(MinecraftServer mc, ServerCommandSource source, String[] args) {
        if (args.length != 2) {
            source.sendError(Text.literal("Usage: /mw delete <id>"));
            return 0;
        }

        String worldId = args[1];
        if (worldId.indexOf(':') == -1) {
            worldId = "multiworld:" + worldId;
        }

        // Check if world config exists
        boolean isLoaded = Util.isWorldLoaded(mc, worldId);
        if (!WorldConfig.getConfigFile(mc, worldId).exists() && !isLoaded) {
            source.sendError(Text.literal("World '" + worldId + "' does not exist!"));
            return 0;
        }

        // If the world is loaded in the server, unload it first
        if (isLoaded) {
            MultiworldMod.get_world_creator().delete_world(worldId);
        } else {
            // wipe the whole world directory
            try {
                Identifier id = MultiworldMod.new_id(worldId);
                RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, id);
                File worldDir = WorldConfig.getWorldDirectory(mc, worldKey);
                if (worldDir.exists()) {
                    FileUtils.deleteDirectory(worldDir);
                }
            } catch (IOException e) {
                source.sendError(Text.literal("Error deleting world directory: " + e.getMessage()));
                e.printStackTrace();
                return 0;
            }
        }

        source.sendMessage(text("Deleted world with id: " + worldId, Formatting.GREEN));

        return 1;
    }
}
