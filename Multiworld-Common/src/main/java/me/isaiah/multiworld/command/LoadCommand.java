package me.isaiah.multiworld.command;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static me.isaiah.multiworld.MultiworldMod.text;
import me.isaiah.multiworld.config.WorldConfig;

public class LoadCommand {

    // Run Command
    public static int run(MinecraftServer mc, ServerCommandSource source, String[] args) {
        if (args.length != 2) {
            source.sendError(Text.literal("Usage: /mw load <id>"));
            return 0;
        }

        String worldId = args[1];
        if (worldId.indexOf(':') == -1) {
            worldId = "multiworld:" + worldId;
        }

        try {
            // Check if world config exists
            if (!WorldConfig.getConfigFile(mc, worldId).exists()) {
                source.sendError(Text.literal("World '" + worldId + "' does not exist!"));
                return 0;
            }

            // Check if world is already loaded in the server
            if (Util.isWorldLoaded(mc, worldId)) {
                source.sendError(Text.literal("World '" + worldId + "' is already loaded in the server!"));
                return 0;
            }

            // Mark world as loaded in config
            WorldConfig.setLoaded(mc, worldId, true);

            // Load the world
            CreateCommand.reinit_world_from_config(mc, worldId);

            source.sendMessage(text("Loaded world: " + worldId, Formatting.GREEN));
            return 1;
        } catch (Exception e) {
            source.sendError(Text.literal("Failed to load world: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }
}
