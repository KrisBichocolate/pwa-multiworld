package me.isaiah.multiworld.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.command.CreateCommand;
import me.isaiah.multiworld.command.Util;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.World;
import net.minecraft.world.level.storage.LevelStorage;
import xyz.nucleoid.fantasy.mixin.MinecraftServerAccess;

/**
 * Helper class for managing world-specific configuration files
 */
public class WorldConfig {

    /**
     * Get the configuration file for a world
     * 
     * @param server The Minecraft server
     * @param world The world to get the config for
     * @return The FileConfiguration for the world
     * @throws IOException If there's an error creating or reading the file
     */
    public static FileConfiguration getConfig(MinecraftServer server, World world) throws IOException {
        File configFile = getConfigFile(server, world.getRegistryKey());
        configFile.getParentFile().mkdirs();
        if (!configFile.exists()) {
            configFile.createNewFile();
        }
        return new FileConfiguration(configFile);
    }

    /**
     * Get the configuration file for a world by its registry key
     * 
     * @param server The Minecraft server
     * @param worldKey The registry key of the world
     * @return The FileConfiguration for the world
     * @throws IOException If there's an error creating or reading the file
     */
    public static FileConfiguration getConfig(MinecraftServer server, RegistryKey<World> worldKey) throws IOException {
        File configFile = getConfigFile(server, worldKey);
        configFile.getParentFile().mkdirs();
        if (!configFile.exists()) {
            configFile.createNewFile();
        }
        return new FileConfiguration(configFile);
    }

    /**
     * Get the configuration file for a world by its ID string
     * 
     * @param server The Minecraft server
     * @param worldId The world ID (e.g. "multiworld:myworld")
     * @return The FileConfiguration for the world
     * @throws IOException If there's an error creating or reading the file
     */
    public static FileConfiguration getConfig(MinecraftServer server, String worldId) throws IOException {
        Identifier id = MultiworldMod.new_id(worldId);
        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, id);
        return getConfig(server, worldKey);
    }

    /**
     * Get the File object for a world's config file
     * 
     * @param server The Minecraft server
     * @param worldKey The registry key of the world
     * @return The File object for the world's config
     */
    public static File getConfigFile(MinecraftServer server, RegistryKey<World> worldKey) {
        File worldDir = getWorldDirectory(server, worldKey);
        return new File(worldDir, "multiworld.yml");
    }

    /**
     * Get the directory for a world
     * 
     * @param server The Minecraft server
     * @param worldKey The registry key of the world
     * @return The File object for the world's directory
     */
    public static File getWorldDirectory(MinecraftServer server, RegistryKey<World> worldKey) {
        LevelStorage.Session session = ((MinecraftServerAccess) server).getSession();
        return session.getWorldDirectory(worldKey).toFile();
    }

    /**
     * Get the File object for a world's config file
     * 
     * @param server The Minecraft server
     * @param worldId The world ID (e.g. "multiworld:myworld")
     * @return The File object for the world's config
     */
    public static File getConfigFile(MinecraftServer server, String worldId) {
        Identifier id = MultiworldMod.new_id(worldId);
        RegistryKey<World> worldKey = RegistryKey.of(net.minecraft.registry.RegistryKeys.WORLD, id);
        return getConfigFile(server, worldKey);
    }

    /**
     * Set the spawn position for a world
     * 
     * @param server The Minecraft server
     * @param world The world to set the spawn for
     * @param spawn The spawn position
     * @throws IOException If there's an error saving the config
     */
    public static void setSpawn(MinecraftServer server, World world, BlockPos spawn) throws IOException {
        FileConfiguration config = getConfig(server, world);
        config.set("spawnpos", spawn.asLong());
        config.save();
    }

    /**
     * Get the spawn position for a world
     * 
     * @param server The Minecraft server
     * @param world The world to get the spawn for
     * @return The spawn position, or null if not set
     */
    public static BlockPos getSpawn(MinecraftServer server, ServerWorld world) {
        try {
            FileConfiguration config = getConfig(server, world);
            if (config.is_set("spawnpos")) {
                return BlockPos.fromLong(config.getLong("spawnpos"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Save a gamerule to the world config
     * 
     * @param server The Minecraft server
     * @param world The world to save the gamerule for
     * @param ruleName The name of the gamerule
     * @param value The value of the gamerule
     * @throws IOException If there's an error saving the config
     */
    public static void saveGamerule(MinecraftServer server, World world, String ruleName, String value) throws IOException {
        FileConfiguration config = getConfig(server, world);

        if (!config.is_set("gamerules")) {
            config.set("gamerules", new ArrayList<String>());
        }

        config.set("gamerule_" + ruleName, value);
        config.save();
    }

    /**
     * Create or update the config file for a world
     * 
     * @param server The Minecraft server
     * @param world The world to create the config for
     * @param presetKey The world preset key
     * @param dimensionKey The dimension key within the preset
     * @param seed The world seed
     */
    public static void createWorldConfig(MinecraftServer server, ServerWorld world, String presetKey, String dimensionKey, long seed) {
        try {
            FileConfiguration config = getConfig(server, world);
            Identifier id = world.getRegistryKey().getValue();

            // Store preset and dimension keys instead of environment
            config.set("preset_key", presetKey);
            config.set("dimension_key", dimensionKey);

            // Keep environment for backward compatibility
            config.set("environment", presetKey);

            config.set("seed", seed);
            config.set("loaded", true);
            config.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Find all world config files in the save directory
     * 
     * @param server The Minecraft server
     * @return An array of world IDs that have config files
     */
    public static String[] findAllWorldConfigs(MinecraftServer server) {
        LevelStorage.Session session = ((MinecraftServerAccess) server).getSession();
        File saveDir = session.getDirectory(WorldSavePath.ROOT).toFile();
        File dimensionsDir = new File(saveDir, "dimensions");

        if (!dimensionsDir.exists() || !dimensionsDir.isDirectory()) {
            return new String[0];
        }

        ArrayList<String> worldIds = new ArrayList<>();

        // Iterate through namespace directories
        for (File namespaceDir : dimensionsDir.listFiles()) {
            if (!namespaceDir.isDirectory()) continue;

            String namespace = namespaceDir.getName();

            // Recursively find all world directories with config files
            findWorldsInDirectory(namespaceDir, namespace, "", worldIds);
        }

        return worldIds.toArray(new String[0]);
    }

    /**
     * Recursively find all world directories with config files
     * 
     * @param directory The directory to search in
     * @param namespace The namespace of the worlds
     * @param pathSoFar The path accumulated so far (for nested directories)
     * @param worldIds The list to add found world IDs to
     */
    private static void findWorldsInDirectory(File directory, String namespace, String pathSoFar, ArrayList<String> worldIds) {
        File[] files = directory.listFiles();
        if (files == null) return;

        // Check if this directory has a config file
        File configFile = new File(directory, "multiworld.yml");
        if (configFile.exists() && !pathSoFar.isEmpty()) {
            // Remove leading slash if present
            if (pathSoFar.startsWith("/")) {
                pathSoFar = pathSoFar.substring(1);
            }
            worldIds.add(namespace + ":" + pathSoFar);
        }

        // Recursively check subdirectories
        for (File file : files) {
            if (file.isDirectory()) {
                String newPath = pathSoFar.isEmpty() ? file.getName() : pathSoFar + "/" + file.getName();
                findWorldsInDirectory(file, namespace, newPath, worldIds);
            }
        }
    }

    /**
     * Load all worlds from their config files
     * 
     * @param server The Minecraft server
     */
    public static void loadAllWorlds(MinecraftServer server) {
        String[] worldIds = findAllWorldConfigs(server);
        for (String worldId : worldIds) {
            try {
                // Check if world should be loaded and isn't already loaded
                if (isLoaded(server, worldId) && !Util.isWorldLoaded(server, worldId)) {
                    CreateCommand.reinit_world_from_config(server, worldId);
                }
            } catch (Exception e) {
                System.err.println("Failed to load world: " + worldId);
                e.printStackTrace();
            }
        }
    }

    /**
     * Set the loaded state of a world
     * 
     * @param server The Minecraft server
     * @param worldId The world ID
     * @param loaded Whether the world should be loaded on startup
     * @throws IOException If there's an error saving the config
     */
    public static void setLoaded(MinecraftServer server, String worldId, boolean loaded) throws IOException {
        FileConfiguration config = getConfig(server, worldId);
        config.set("loaded", loaded);
        config.save();
    }

    /**
     * Check if a world is marked as loaded in its config
     * 
     * @param server The Minecraft server
     * @param worldId The world ID
     * @return Whether the world is marked as loaded in config
     */
    public static boolean isLoaded(MinecraftServer server, String worldId) {
        try {
            FileConfiguration config = getConfig(server, worldId);
            return config.is_set("loaded") && config.getBoolean("loaded");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
