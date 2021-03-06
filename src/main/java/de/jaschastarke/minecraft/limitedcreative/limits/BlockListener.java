/*
 * Limited Creative - (Bukkit Plugin)
 * Copyright (C) 2012 jascha@ja-s.de
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.jaschastarke.minecraft.limitedcreative.limits;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import de.jaschastarke.minecraft.lib.permissions.IDynamicPermission;
import de.jaschastarke.minecraft.limitedcreative.ModCreativeLimits;

public class BlockListener implements Listener {
    private ModCreativeLimits mod;
    public BlockListener(ModCreativeLimits mod) {
        this.mod = mod;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.isCancelled() && event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            if (mod.getConfig().getBlockBreak().isListed(event.getBlock())) {
                if (!checkPermission(event.getPlayer(), NoLimitPermissions.BREAK(event.getBlock()))) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(mod.getPlugin().getLocale().trans("blocked.break"));
                }
            }
        }
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!event.isCancelled() && event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            if (mod.getConfig().getBlockUse().isListed(event.getBlock())) {
                if (!checkPermission(event.getPlayer(), NoLimitPermissions.USE(event.getBlock()))) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(mod.getPlugin().getLocale().trans("blocked.place"));
                }
            }
        }
    }
    
    private boolean checkPermission(Player player, IDynamicPermission perm) {
        return mod.getPlugin().getPermManager().hasPermission(player, perm);
    }
}
