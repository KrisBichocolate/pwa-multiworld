package me.isaiah.multiworld.command;

import me.isaiah.multiworld.MultiworldMod;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class Util {
	
	// Dimension Ids
    public static final Identifier OVERWORLD_ID = id("overworld");
    public static final Identifier THE_NETHER_ID = id("the_nether");
    public static final Identifier THE_END_ID = id("the_end");

    public static Identifier id(String id) {
    	return MultiworldMod.new_id(id);
    }
    
    /**
     * Check if a world is currently loaded in the server
     * 
     * @param server The Minecraft server
     * @param worldId The world ID
     * @return Whether the world is currently loaded in the server
     */
    public static boolean isWorldLoaded(MinecraftServer server, String worldId) {
        Identifier id = MultiworldMod.new_id(worldId);
        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, id);
        return server.getWorld(worldKey) != null;
    }
}
