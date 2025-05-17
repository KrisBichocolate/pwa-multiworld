package me.isaiah.multiworld.perm;

import me.isaiah.multiworld.MultiworldMod;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class Perm {

    public static Perm INSTANCE;
    public static void setPerm(Perm p) {INSTANCE = p;}
    
    public boolean has_impl(ServerPlayerEntity plr, String perm) {
        System.out.println("Platform Permission Handler not found!");
        return false;
    }

    public static boolean has(ServerPlayerEntity plr, String perm) {
        if (null == INSTANCE) {
            System.out.println("Platform Permission Handler not found!");
            return plr != null && plr.hasPermissionLevel(4);
        }
        return plr != null && (INSTANCE.has_impl(plr, perm) || plr.hasPermissionLevel(4));
    }

    public static boolean has(ServerCommandSource s, String perm) {
        try {
            return has(MultiworldMod.get_player(s), perm) || s.hasPermissionLevel(4);
        } catch (Exception e) {
            return s.hasPermissionLevel(4);
        }
    }

}
