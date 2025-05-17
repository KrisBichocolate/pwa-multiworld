package me.isaiah.multiworld.command;

import java.io.File;
import java.io.IOException;

import me.isaiah.multiworld.config.FileConfiguration;
import me.isaiah.multiworld.config.WorldConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static me.isaiah.multiworld.MultiworldMod.text;
import static me.isaiah.multiworld.MultiworldMod.text_plain;

public class SetspawnCommand {

    public static int run(MinecraftServer mc, ServerCommandSource source, String[] args) {
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("This command must be executed by a player"));
            return 0;
        }
        
        ServerPlayerEntity plr = source.getPlayer();
        World w = source.getWorld();
        BlockPos pos = plr.getBlockPos();
        try {
            WorldConfig.setSpawn(mc, w, pos);
			
			String txt = "Spawn for world \"" + w.getRegistryKey().getValue() + "\" changed to " + pos.toShortString();
			
            source.sendMessage(text(txt, Formatting.GOLD));
        } catch (IOException e) {
            source.sendError(Text.literal("Error: " + e.getMessage()));
            e.printStackTrace();
        }
        return 1;
    }
}
