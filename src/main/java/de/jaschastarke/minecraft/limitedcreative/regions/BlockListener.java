package de.jaschastarke.minecraft.limitedcreative.regions;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;

import de.jaschastarke.bukkit.lib.Utils;
import de.jaschastarke.minecraft.limitedcreative.ModRegions;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.ApplicableRegions;
import de.jaschastarke.minecraft.limitedcreative.regions.worldguard.CustomRegionManager;

public class BlockListener implements Listener {
    private ModRegions mod;
    private CustomRegionManager rm;
    
    public BlockListener(ModRegions mod) {
        this.mod = mod;
        rm = mod.getRegionManager();
    }

    private ApplicableRegions regionSet(Location loc) {
        return rm.getRegionSet(loc);
    }
    private ApplicableRegions regionSet(Block block) {
        return rm.getRegionSet(block);
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled())
            return;
        whenBlockBreak(event, event.getBlock(), event.getPlayer());
    }
    
    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Player) {
            Player eventPlayer = (Player) event.getRemover();
            whenBlockBreak(event, event.getEntity().getLocation().getBlock(), eventPlayer);
        }
    }
    
    private void whenBlockBreak(Cancellable event, Block block, Player player) {
        boolean diffrent_region = rm.isDiffrentRegion(player, block.getLocation());
        
        PlayerData.Data pdata = mod.getPlayerData(player);
        
        if (pdata.isActiveRegionGameMode() && diffrent_region) {
            // do not break outside of "gamemod-change-region" when in the region
            if (rm.getRegionSet(block).getFlag(Flags.GAMEMODE, player) != pdata.getActiveRegionGameMode()) {
                player.sendMessage(L("blocked.outside_break"));
                event.setCancelled(true);
            }
        } else if (diffrent_region) {
            // do not break inside of "survial-region in creative world" when outside
            if (rm.getRegionSet(block).getFlag(Flags.GAMEMODE) != null) {
                player.sendMessage(L("blocked.inside_break"));
                event.setCancelled(true);
            }
        }
        if (!event.isCancelled()) {
            // prevent any drops for survival players in creative regions
            if (player.getGameMode() != GameMode.CREATIVE && rm.getRegionSet(block).getFlag(Flags.GAMEMODE) == GameMode.CREATIVE) {
                mod.getBlockSpawn().block(block, player);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled())
            return;
        
        PlayerData.Data pdata = mod.getPlayerData(event.getPlayer());
        boolean diffrent_region = rm.isDiffrentRegion(event.getPlayer(), event.getBlock().getLocation());
        
        if (pdata.isActiveRegionGameMode() && diffrent_region) {
            // do not build outside of "gamemod-change-region" when in the region
            if (rm.getRegionSet(event.getBlock()).getFlag(Flags.GAMEMODE, event.getPlayer()) != pdata.getActiveRegionGameMode()) { 
                event.getPlayer().sendMessage(L("blocked.outside_place"));
                event.setCancelled(true);
            }
        } else if (diffrent_region) {
            // do not build inside of "survial-region in creative world" when outside
            if (rm.getRegionSet(event.getBlock()).getFlag(Flags.GAMEMODE) != null) {
                event.getPlayer().sendMessage(L("blocked.inside_place"));
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (event.isCancelled())
            return;
        
        Block source = event.getBlock().getRelative(event.getDirection());
        if (mod.isDebug())
            mod.getLog().debug("PistonExtend "+source.getType()+" "+event.getDirection());
        if (source.getType() != Material.AIR) {
            if (regionSet(source).getFlag(Flags.GAMEMODE) == GameMode.CREATIVE) {
                for (int i = 1; i <= 12; i++) {
                    Block dest = source.getRelative(event.getDirection(), i);
                    if (mod.isDebug())
                        mod.getLog().debug("dest "+i+": "+dest.getType());
                    if (regionSet(dest).getFlag(Flags.GAMEMODE) != GameMode.CREATIVE) {
                        mod.getLog().warn(L("blocked.piston", source.getRelative(event.getDirection(), i - 1).getType().toString(), Utils.toString(source.getLocation())));
                        event.setCancelled(true);
                        break;
                    } else if (dest.getType() == Material.AIR) {
                        break;
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (event.isCancelled())
            return;
        Block source = event.getBlock().getRelative(event.getDirection(), 2);
        Block dest = source.getRelative(event.getDirection().getOppositeFace());
        if (mod.isDebug())
            mod.getLog().debug("PistonRetract "+source.getType()+" "+event.getDirection() + " " + event.isSticky());
        if (event.isSticky() && source.getType() != Material.AIR) { 
            if (mod.isDebug())
                mod.getLog().debug("dest "+dest.getType());
            if (regionSet(source).getFlag(Flags.GAMEMODE) == GameMode.CREATIVE) {
                if (regionSet(dest).getFlag(Flags.GAMEMODE) != GameMode.CREATIVE) {
                    mod.getLog().warn(L("blocked.piston", source.getType().toString(), Utils.toString(source.getLocation())));
                    event.setCancelled(true);
                }
            } else if (regionSet(dest).getFlag(Flags.GAMEMODE) == GameMode.CREATIVE) {
                // source isn't creative
                mod.getLog().warn(L("blocked.piston_in", source.getType().toString(), Utils.toString(source.getLocation())));
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        if (event.isCancelled())
            return;
        if (event.getEntity() instanceof Item) {
            if (!regionSet(event.getLocation()).allows(Flags.SPAWNDROPS))
                event.setCancelled(true);
        }
    }
    
    private String L(String msg, Object... args) {
        return mod.getPlugin().getLocale().trans(msg, args);
    }
}