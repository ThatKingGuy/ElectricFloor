package com.gabe.electricfloor;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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

    private ElectricFloor plugin;

    public Events(ElectricFloor plugin){
        this.plugin = plugin;
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
        arena.checkWinner(plugin);
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

        if (event.getDamager() instanceof Firework && event.getEntity() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if(block != null) {
            if (block.getState() != null) {
                if (block.getState() instanceof Sign) {
                    Sign sign = (Sign) block.getState();
                    String line1 = ChatColor.stripColor(sign.getLine(0));
                    if (line1.equalsIgnoreCase("[ElectricFloor]")) {
                        Arena arena = ElectricFloor.getArenaManager().getArena(ChatColor.stripColor(sign.getLine(2)));
                        if (arena == null) {
                            player.sendMessage(format("&cArena dosent exist!"));
                            return;
                        }
                        player.sendMessage(format("&aJoining arena " + arena.getName()));
                        player.performCommand("ef join " + arena.getName());
                    }
                }
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
        if(clickedInv.getItem(26) != null) {
            if (clickedInv.getItem(26).getType() == Material.PAPER) {
                if (clickedInv.getItem(26).getItemMeta().getDisplayName().equalsIgnoreCase(identifier)) {
                    isEditor = true;
                }
            }
        }

        if(isEditor){
            String arenaText = ChatColor.stripColor(clickedInv.getItem(26).getItemMeta().getLore().get(0));
            String arenaName = arenaText.substring(7);
            Arena arena = ElectricFloor.getArenaManager().getArena(arenaName);
            if(arena == null){
                player.sendMessage(format("&cArena does not exist!"));
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
            if(clickedItem.getType() == Material.RED_STAINED_GLASS){
                arena.setGlassHeight(player.getLocation().getBlockY());
                player.sendMessage(format("&aSet glass height to y: "+player.getLocation().getBlockY()));
                player.closeInventory();
            }
            if(clickedItem.getType() == Material.FEATHER){
                ClickType clickType = event.getClick();
                if(clickType == ClickType.LEFT){
                    if(arena.getMinPlayers()>2){
                        arena.setMinPlayers(arena.getMinPlayers()-1);
                    }
                }else if(clickType == ClickType.RIGHT){
                    if(arena.getMinPlayers()<20){
                        arena.setMinPlayers(arena.getMinPlayers()+1);
                    }
                }
                player.closeInventory();
            }
            if(clickedItem.getType() == Material.BONE){
                ClickType clickType = event.getClick();
                if(clickType == ClickType.LEFT){
                    if(arena.getMaxPlayers()>2){
                        arena.setMaxPlayers(arena.getMaxPlayers()-1);
                    }
                }else if(clickType == ClickType.RIGHT){
                    if(arena.getMaxPlayers()<20){
                        arena.setMaxPlayers(arena.getMaxPlayers()+1);
                    }
                }
                player.closeInventory();
            }

            if(clickedItem.getType() == Material.OAK_SIGN){
                Block block = player.getTargetBlock(null, 5);
                if(block.getType() == Material.OAK_SIGN || block.getType() == Material.OAK_WALL_SIGN){
                    Sign sign = (Sign) block.getState();
                    sign.setLine(0,ChatColor.translateAlternateColorCodes('&', "&6[&3Electric&bFloor&6]"));
                    sign.setLine(1,ChatColor.translateAlternateColorCodes('&', "&aJoin"));
                    sign.setLine(2,ChatColor.translateAlternateColorCodes('&', "&6"+arena.getName()));
                    sign.update();
                }else{
                    player.sendMessage(format("&cThis is not a sign."));
                }
                player.closeInventory();
            }
        }
    }
}
