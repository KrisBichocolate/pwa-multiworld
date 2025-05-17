package me.isaiah.multiworld.command;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import static me.isaiah.multiworld.MultiworldMod.text;
import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.config.WorldConfig;
import me.isaiah.multiworld.command.Util;

public class UnloadCommand {

    // Run Command
    public static int run(MinecraftServer mc, ServerCommandSource source, String[] args) {
        if (args.length != 2) {
            source.sendError(Text.literal("Usage: /mw unload <id>"));
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

            // Get world key
            RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, Util.id(worldId));
            ServerWorld world = mc.getWorld(worldKey);

            // Check if world is already unloaded from the server
            if (world == null) {
                source.sendError(Text.literal("World '" + worldId + "' is not currently loaded in the server!"));
                return 0;
            }

            // Mark world as unloaded in config
            if (MultiworldMod.get_world_creator().unload_world(worldId)) {
                // Technically unloading could be delayed, so this is a bit dubious
                WorldConfig.setLoaded(mc, worldId, false);
            } else {
                source.sendError(Text.literal("World unloading failed or unsupported"));
                return 0;
            }

            source.sendMessage(text("Unloaded world: " + worldId, Formatting.GREEN));
            return 1;
        } catch (Exception e) {
            source.sendError(Text.literal("Failed to unload world: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }
}
