package de.jaschastarke.minecraft.limitedcreative;

import org.bukkit.Bukkit;

import de.jaschastarke.hooking.BooleanHooker;
import de.jaschastarke.hooking.GetHooker;
import de.jaschastarke.minecraft.limitedcreative.hooks.AuthMeHooks;
import de.jaschastarke.minecraft.limitedcreative.hooks.MultiVerseHooks;
import de.jaschastarke.minecraft.limitedcreative.hooks.PlayerCheckHooker;
import de.jaschastarke.minecraft.limitedcreative.hooks.WorldTypeHooker;
import de.jaschastarke.minecraft.limitedcreative.hooks.xAuthHooks;

public final class Hooks {
    public static PlayerCheckHooker IsLoggedIn = new PlayerCheckHooker(true);
    public static WorldTypeHooker DefaultWorldGameMode = new WorldTypeHooker();
    public static BooleanHooker IsMultiVerse = new BooleanHooker(false);
    public static GetHooker<String> InventoryIncompatible = new GetHooker<String>();

    public static boolean isPluginEnabled(String pluginName) {
        return Bukkit.getServer().getPluginManager().isPluginEnabled(pluginName);
    }
    
    public static void inizializeHooks(LimitedCreative plugin) {
        IsLoggedIn.clearHooks();
        DefaultWorldGameMode.clearHooks();
        IsMultiVerse.clearHooks();
        InventoryIncompatible.clearHooks();
        
        if (isPluginEnabled("AuthMe")) {
            new AuthMeHooks(plugin);
        }
        if (isPluginEnabled("xAuth")) {
            new xAuthHooks(plugin);
        }
        if (isPluginEnabled("Multiverse-Core")) {
            new MultiVerseHooks(plugin);
        }
        
        InventoryIncompatible.register(new GetHooker.Check<String>() {
            @Override
            public String test() {
                if (isPluginEnabled("MultiInv"))
                    return "MultiInv";
                if (isPluginEnabled("Multiverse-Inventories"))
                    return "Multiverse-Inventories";
                return null;
            }
        });
    }
}
