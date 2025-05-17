package me.isaiah.multiworld.command;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.config.FileConfiguration;
import me.isaiah.multiworld.config.WorldConfig;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.GameRules.BooleanRule;
import net.minecraft.world.GameRules.IntRule;
import net.minecraft.world.GameRules.Rule;

public class GameruleCommand {

	// TODO
	public static GameRules getGameRules(ServerWorld world) {
		return world.getGameRules();
	}
	
	@SuppressWarnings("rawtypes")
	public static HashMap<String, GameRules.Key> keys = new HashMap<>();
	
    @SuppressWarnings("unchecked")
	public static int run(MinecraftServer mc, ServerCommandSource source, String[] args) {
        ServerWorld w = source.getWorld();
        
		if (keys.isEmpty()) {
			setup(w);
		}

        // GameRules rules = new GameRules();

		if (args.length < 3) {
			Rule<?> rule = getGameRules(w).get(keys.get(args[1]));
			source.sendMessage(Text.literal("§4[Multiworld]§r Value of " + args[1] + " is: " + rule));
			return 1;
		}
		
        String a1 = args[1];
        String a2 = args[2];
        
        /*if (a1.equalsIgnoreCase("difficulty")) {
        	// Test
        	
			Difficulty d = Difficulty.NORMAL;

			// String to Difficulty
			if (a2.equalsIgnoreCase("EASY"))     { d = Difficulty.EASY; }
			else if (a2.equalsIgnoreCase("HARD"))     { d = Difficulty.HARD; }
			else if (a2.equalsIgnoreCase("NORMAL"))   { d = Difficulty.NORMAL; }
			else if (a2.equalsIgnoreCase("PEACEFUL")) { d = Difficulty.PEACEFUL; }
			else {
				MultiworldMod.message(plr, "Invalid difficulty: " + a2);
				return 1;
			}
        	
        	MultiworldMod.get_world_creator().set_difficulty(w.getRegistryKey().getValue().toString(), d);
        	
        	try {
				FileConfiguration config = CreateCommand.get_config(w);
				config.set("difficulty", a2);
				config.save();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

        	return 1;
        }*/
        
        boolean is_bol = false;
        
        if (a2.equalsIgnoreCase("true") || a2.equalsIgnoreCase("false")) {
        	is_bol = true;
        }

        if (is_bol) {
        	// Boolean Rule
        	BooleanRule rule = (BooleanRule) getGameRules(w).get(keys.get(a1));
        	rule.set(Boolean.valueOf(a2), mc);
        } else {
        	// Int Rule
        	IntRule rule = (IntRule) getGameRules(w).get(keys.get(a1));
        	rule.set(Integer.valueOf(a2), mc);
        }

        // Save to world config
    	try {
			WorldConfig.saveGamerule(mc, w, a1, a2);
		} catch (IOException e) {
			e.printStackTrace();
		}

        source.sendMessage(Text.literal("§c[Multiworld]§r: Gamerule " + a1 + " is now set to: " + a2));
        
        return 1;
    }

    /**
     * Read the Gamerule names – fetches gamerules from server
     */
    public static void setupServer(MinecraftServer server) {
        keys.clear();
        // Create a temporary GameRules instance to access the accept method
        server.getGameRules().accept(new GameRules.Visitor() {
            @Override
            public <T extends GameRules.Rule<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                String name = key.getName();
                keys.put(name, key);
            }
        });
    }
    
    /**
     * Read the Gamerule names – fetches gamerules from world
     */
    public static void setup(ServerWorld world) {
        keys.clear();
        // Create a temporary GameRules instance to access the accept method
        world.getGameRules().accept(new GameRules.Visitor() {
            @Override
            public <T extends GameRules.Rule<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                String name = key.getName();
                keys.put(name, key);
            }
        });
    }
    

    /**
     * Load gamerule from config entry
     * 
     * @param world - The ServerWorld to apply the Gamerule
     * @param key - Config key for Gamerule (ex: "gamerule_doDaylightCycle")
     * @param val - The value for the Gamerule (ex: "true", or "100")
     * @see {@link CreateCommand#reinit_world_from_config(MinecraftServer, String)}
     */
	@SuppressWarnings("unchecked")
	public static void set_gamerule_from_cfg(ServerWorld world, String key, String val) {
		if (keys.isEmpty()) {
			setup(world);
		}

        String name = key.replace("gamerule_", "").trim();
        String a1 = val.trim();
		
		boolean is_bol = false;
        
        if (a1.equalsIgnoreCase("true") || a1.equalsIgnoreCase("false")) {
        	is_bol = true;
        }
		
        if (is_bol) {
        	// Boolean Rule
        	BooleanRule rule = (BooleanRule) getGameRules(world).get(keys.get(name));
        	rule.set(Boolean.valueOf(a1), MultiworldMod.mc);
        } else {
        	// Int Rule
        	IntRule rule = (IntRule) getGameRules(world).get(keys.get(name));
        	rule.set(Integer.valueOf(a1), MultiworldMod.mc);
        }
		
	}

}
