/**
 * Multiworld Mod
 * Copyright (c) 2021-2024 by Isaiah.
 */
package me.isaiah.multiworld;

import com.mojang.brigadier.CommandDispatcher;
import java.io.File;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import me.isaiah.multiworld.command.CloneCommand;
import me.isaiah.multiworld.command.CreateCommand;
import me.isaiah.multiworld.command.DeleteCommand;
import me.isaiah.multiworld.command.DifficultyCommand;
import me.isaiah.multiworld.command.GameruleCommand;
import me.isaiah.multiworld.command.LoadCommand;
import me.isaiah.multiworld.command.SetspawnCommand;
import me.isaiah.multiworld.command.SpawnCommand;
import me.isaiah.multiworld.command.TpCommand;
import me.isaiah.multiworld.command.UnloadCommand;
import me.isaiah.multiworld.command.Util;
import me.isaiah.multiworld.config.WorldConfig;
import me.isaiah.multiworld.perm.Perm;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

/**
 * Multiworld Mod
 */
public class MultiworldMod {

    public static final String MOD_ID = "multiworld";
    public static MinecraftServer mc;
    public static String CMD = "mw";
    public static ICreator world_creator;

	// Mod Version
	public static final String VERSION = "1.8";

    public static void setICreator(ICreator ic) {
        world_creator = ic;
    }

    /**
     * Gets the Multiversion ICreator instance
     */
    public static ICreator get_world_creator() {
    	return world_creator;
    }

    public static ServerWorld create_world(String id, Identifier dim, ChunkGenerator gen, Difficulty dif, long seed) {
    	return world_creator.create_world(id, dim, gen, dif, seed);
    }

    /**
     * ModInitializer onInitialize
     * 
     * @see {@link me.isaiah.multiworld.fabric.MultiworldModFabric}
     */
    public static void init() {
        System.out.println("Multiworld init");
    }

    public static Identifier new_id(String id) {
    	// tryParse works from 1.18 to 1.21
    	return Identifier.tryParse(id);
    }

    // On server start
    public static void on_server_started(MinecraftServer mc) {
        MultiworldMod.mc = mc;
		
		WorldConfig.loadAllWorlds(mc);
    }

    public static ServerPlayerEntity get_player(ServerCommandSource s) throws CommandSyntaxException {
    	ServerPlayerEntity plr = s.getPlayer();
    	if (null == plr) {
    		// s.sendMessage(text_plain("Multiworld Mod for Minecraft " + mc.getVersion()));
    		// s.sendMessage(text_plain("These commands currently require a Player."));
    		
    		throw ServerCommandSource.REQUIRES_PLAYER_EXCEPTION.create();
    	}
    	return plr;
    }

    // On command register
    public static void register_commands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal(CMD)
                    .requires(source -> {
                        try {
                            return source.hasPermissionLevel(1) || Perm.has(get_player(source), "multiworld.cmd") ||
                                    Perm.has(get_player(source), "multiworld.admin");
                        } catch (Exception e) {
                            return source.hasPermissionLevel(1);
                        }
                    }) 
                        .executes(ctx -> {
                            return broadcast(ctx.getSource(), Formatting.AQUA, null);
                        })
                        .then(argument("message", greedyString()).suggests(new InfoSuggest())
                                .executes(ctx -> {
                                    try {
                                        return broadcast(ctx.getSource(), Formatting.AQUA, getString(ctx, "message") );
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        return 1;
                                    }
                                 }))); 
    }
   
    public static int broadcast(ServerCommandSource source, Formatting formatting, String message) throws CommandSyntaxException {
        if (null == message) {
            source.sendMessage(text("Multiworld Mod for Minecraft " + mc.getVersion(), Formatting.AQUA));
            
            if (source.isExecutedByPlayer()) {
                ServerPlayerEntity plr = source.getPlayer();
                World world = plr.getWorld();
                Identifier id = world.getRegistryKey().getValue();
                
                source.sendMessage(Text.literal("Currently in: " + id.toString()));
            }
            
            return 1;
        }

        boolean ALL = Perm.has(source, "multiworld.admin");
        String[] args = message.split(" ");
        
        /*if (args[0].equalsIgnoreCase("portaltest")) {
            BlockPos pos = plr.getBlockPos();
            pos = pos.add(2, 0, 2);
            ServerWorld w = plr.getWorld();

            Portal p = new Portal();
            for (int x = 0; x < 4; x++) {
                for (int y = 0; y < 5; y++) {
                    BlockPos pos2 = pos.add(x, y, 0);
                    if ((x > 0 && x < 3) && (y > 0 && y < 4)) {
                        p.blocks.add(pos2);
                        w.setBlockState(pos2, Blocks.NETHER_PORTAL.getDefaultState());
                    } else
                    w.setBlockState(pos2, Blocks.STONE.getDefaultState());
                }
            }
            p.addToMap();
            try {
                p.save();
            } catch (IOException e) {
                plr.sendMessage(text("Failed saving portal data. Check console for details.", Formatting.RED), false);
                e.printStackTrace();
            }
        }*/
        
        // Help Command
        if (args[0].equalsIgnoreCase("help")) {
            source.sendMessage(Text.literal("§4Multiworld Mod Commands:§r"));
            source.sendMessage(Text.literal("§a/mw spawn§r - Teleport to current world spawn"));
            source.sendMessage(Text.literal("§a/mw setspawn§r - Sets the current world spawn"));
            source.sendMessage(Text.literal("§a/mw tp <id>§r - Teleport to a world"));
            source.sendMessage(Text.literal("§a/mw list§r - List all worlds"));
            source.sendMessage(Text.literal("§a/mw gamerule <rule> <value>§r - Change a worlds Gamerules"));
            source.sendMessage(Text.literal("§a/mw create <id> <world_preset> <world_preset_dimension>§r - create a new world"));
            source.sendMessage(Text.literal("§a/mw difficulty <value> [world id]§r - Sets the difficulty of a world"));
            source.sendMessage(Text.literal("§a/mw load <id>§r - Load a world"));
            source.sendMessage(Text.literal("§a/mw unload <id>§r - Unload a world"));
            source.sendMessage(Text.literal("§a/mw clone <existing id> <new id>§r - Clones a world"));
            source.sendMessage(Text.literal("§a/mw delete <id>§r - Deletes a world and all its files permanently"));
        }
        
        // Debug
        if (args[0].equalsIgnoreCase("debugtick")) {
            ServerWorld w = (ServerWorld) source.getWorld();
            Identifier id = w.getRegistryKey().getValue();
            source.sendMessage(Text.literal("World ID: " + id.toString()));
            source.sendMessage(Text.literal("Players : " + w.getPlayers().size()));
            w.tick(() -> true);
            return 1;
        }

        // SetSpawn Command
        if (args[0].equalsIgnoreCase("setspawn") && (ALL || Perm.has(source, "multiworld.setspawn"))) {
            return SetspawnCommand.run(mc, source, args);
        }

        // Spawn Command
        if (args[0].equalsIgnoreCase("spawn") && (ALL || Perm.has(source, "multiworld.spawn"))) {
            return SpawnCommand.run(mc, source, args);
        }
        
        // Gamerule Command
        if (args[0].equalsIgnoreCase("gamerule") && (ALL || Perm.has(source, "multiworld.gamerule"))) {
            return GameruleCommand.run(mc, source, args);
        }
        
        // Difficulty Command
        if (args[0].equalsIgnoreCase("difficulty") && (ALL || Perm.has(source, "multiworld.difficulty"))) {
            return DifficultyCommand.run(mc, source, args);
        }

        // TP Command
        if (args[0].equalsIgnoreCase("tp") ) {
            if (!(ALL || Perm.has(source, "multiworld.tp"))) {
                source.sendError(Text.literal("No permission! Missing permission: multiworld.tp"));
                return 1;
            }
            if (args.length == 1) {
                source.sendError(Text.literal("Usage: /" + CMD + " tp <world>"));
                return 0;
            }
            return TpCommand.run(mc, source, args);
        }

        // List Command
        if (args[0].equalsIgnoreCase("list") ) {
            if (!(ALL || Perm.has(source, "multiworld.cmd"))) {
                source.sendError(Text.literal("No permission! Missing permission: multiworld.cmd"));
                return 1;
            }
            source.sendMessage(text("All Worlds:", Formatting.AQUA));
            
            // List loaded worlds
            mc.getWorlds().forEach(world -> {
                String name = world.getRegistryKey().getValue().toString();
                if (name.startsWith("multiworld:")) name = name.replace("multiworld:", "");

                source.sendMessage(text_plain("- " + name));
            });
            
            // List unloaded worlds
            String[] allWorldConfigs = WorldConfig.findAllWorldConfigs(mc);
            for (String worldId : allWorldConfigs) {
                if (!Util.isWorldLoaded(mc, worldId)) {
                    String displayName = worldId;
                    if (displayName.startsWith("multiworld:")) displayName = displayName.replace("multiworld:", "");
                    source.sendMessage(text_plain("- " + displayName + " (unloaded)"));
                }
            }
        }

        // Version Command
        if (args[0].equalsIgnoreCase("version") && (ALL || Perm.has(source, "multiworld.cmd"))) {
            source.sendMessage(Text.literal("Multiworld Mod version " + VERSION));
            return 1;
        }

        // Create Command
        if (args[0].equalsIgnoreCase("create") ) {
            if (!(ALL || Perm.has(source, "multiworld.create"))) {
                source.sendError(Text.literal("No permission! Missing permission: multiworld.create"));
                return 1;
            }
            return CreateCommand.run(mc, source, args);
        }

        // Delete Command
        if (args[0].equalsIgnoreCase("delete") ) {
            if (!(ALL || Perm.has(source, "multiworld.delete"))) {
                source.sendError(Text.literal("No permission! Missing permission: multiworld.delete"));
                return 1;
            }
            return DeleteCommand.run(mc, source, args);
        }

        // Clone Command
        if (args[0].equalsIgnoreCase("clone") ) {
            if (!(ALL || Perm.has(source, "multiworld.clone"))) {
                source.sendError(Text.literal("No permission! Missing permission: multiworld.clone"));
                return 1;
            }
            return CloneCommand.run(mc, source, args);
        }
        
        // Load Command
        if (args[0].equalsIgnoreCase("load") ) {
            if (!(ALL || Perm.has(source, "multiworld.load"))) {
                source.sendError(Text.literal("No permission! Missing permission: multiworld.load"));
                return 1;
            }
            return LoadCommand.run(mc, source, args);
        }
        
        // Unload Command
        if (args[0].equalsIgnoreCase("unload") ) {
            if (!(ALL || Perm.has(source, "multiworld.unload"))) {
                source.sendError(Text.literal("No permission! Missing permission: multiworld.unload"));
                return 1;
            }
            return UnloadCommand.run(mc, source, args);
        }

        return Command.SINGLE_SUCCESS; // Success
    }

    @Deprecated
	public static Text text(String txt, Formatting color) {
		return world_creator.colored_literal(txt, color);
	}
	
	public static void message(PlayerEntity player, String message) {
		try {
			player.sendMessage(Text.literal(translate_alternate_color_codes('&', message)), false);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    private static final char COLOR_CHAR = '\u00A7';
    private static String translate_alternate_color_codes(char altColorChar, String textToTranslate) {
        char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i+1]) > -1) {
                b[i] = COLOR_CHAR;
                b[i+1] = Character.toLowerCase(b[i+1]);
            }
        }
        return new String(b);
    }

	public static Text text_plain(String txt) {
		return Text.literal(txt);
	}

}
