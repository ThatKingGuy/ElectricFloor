package com.gabe.electricfloor;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Events implements Listener {
    public String prefix = "&3Electric&bFloor &7&l⋙ &r&6";

    public String format(String text){
        return ChatColor.translateAlternateColorCodes('&', prefix + text);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        ArenaManager am = ElectricFloor.getArenaManager();
        Arena playerArena = am.getArena(player);
        if(playerArena != null){
            event.setCancelled(true);
            player.sendMessage(format("&cHey! You cant break that block."));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        ArenaManager am = ElectricFloor.getArenaManager();
        Arena playerArena = am.getArena(player);
        if(playerArena != null){
            event.setCancelled(true);
            player.sendMessage(format("&cHey! You cant place blocks here."));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Arena arena = ElectricFloor.getArenaManager().getArena(player);
        if(arena == null) return;
        arena.removePlayer(player);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        Player player = event.getPlayer();
        Arena arena = ElectricFloor.getArenaManager().getArena(player);
        if (arena == null) return;
        if (player.getLocation().getBlockY() <= arena.getDeathHeight()){
            arena.removePlayer(player);
        }
    }

    @EventHandler
    public void onPvp(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player damaged = (Player) event.getEntity();

            Arena a = ElectricFloor.getArenaManager().getArena(damager);
            if(a != null){
                event.setCancelled(true);
                damager.sendMessage(format("&cYou cant fight here."));
            }
        }
    }



    @EventHandler
    public void InventoryClickEvent(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInv = event.getClickedInventory();
        ItemStack clickedItem = clickedInv.getItem(event.getSlot());

        String identifier = ChatColor.translateAlternateColorCodes('&',"&6&lThis is the Arena Editor!");
        boolean isEditor = false;
        if(clickedInv.getItem(26).getType() == Material.PAPER){
            if(clickedInv.getItem(26).getItemMeta().getDisplayName().equalsIgnoreCase(identifier)) {
                isEditor = true;
            }
        }

        if(isEditor){
            String arenaText = ChatColor.stripColor(clickedInv.getItem(26).getItemMeta().getLore().get(0));
            String arenaName = arenaText.substring(7);
            Arena arena = ElectricFloor.getArenaManager().getArena(arenaName);
            if(arena == null){
                player.sendMessage(format("&cERROR"));
                return;
            }
            event.setCancelled(true);
            if(clickedItem.getType() == Material.LAPIS_BLOCK){
                Location spawn = player.getLocation();
                arena.setLobbySpawn(spawn);
                player.sendMessage(format("&aSet lobby location to x: "+spawn.getBlockX()+", y: "+spawn.getBlockY()+", z: "+spawn.getBlockZ()));
                player.closeInventory();
            }
            if(clickedItem.getType() == Material.EMERALD_BLOCK){
                Location spawn = player.getLocation();
                player.sendMessage(format("&aSet game location to x: "+spawn.getBlockX()+", y: "+spawn.getBlockY()+", z: "+spawn.getBlockZ()));
                arena.setGameSpawn(player.getLocation());
                player.closeInventory();
            }
            if(clickedItem.getType() == Material.REDSTONE_BLOCK){
                arena.setEndSpawn(player.getLocation());
                Location spawn = player.getLocation();
                player.sendMessage(format("&aSet end location to x: "+spawn.getBlockX()+", y: "+spawn.getBlockY()+", z: "+spawn.getBlockZ()));
                player.closeInventory();
            }
        }
    }
}
