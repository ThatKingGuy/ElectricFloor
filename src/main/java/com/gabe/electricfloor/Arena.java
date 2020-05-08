package com.gabe.electricfloor;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.DisplaySlot;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Arena {

    private final String name;
    private Location lobbySpawn = null;
    private Location gameSpawn = null;
    private Location endSpawn = null;
    private int deathHeight;
    public GameState state;
    private int maxPlayers = 10;
    private int minPlayers = 2;
    private final Set<Player> players;
    private final Set<Player> playersOut;
    private int countdown  = -1;

    private String prefix = "&3Electric&bFloor &7&l⋙ &r&6";

    private String format(String text){
        return ChatColor.translateAlternateColorCodes('&', prefix + text);
    }

    public Arena(String name) {
        this.playersOut = new HashSet<>();
        this.state = GameState.WAITING;
        this.name = name;
        this.players = new HashSet<>();
    }

    public int getCountdown(){
        return countdown;
    }

    public String getName() {
        return name;
    }

    public void edit(Player player){
        Inventory editMenu = Bukkit.createInventory(null,27, ChatColor.BOLD + "Editing "+getName());

        ItemStack identifier = new ItemStack(Material.PAPER);
        ItemMeta im = identifier.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.RESET + "Arena: "+getName());
        im.setLore(lore);
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&',"&6&lThis is the Arena Editor!"));
        identifier.setItemMeta(im);

        ItemStack setlspawn = new ItemStack(Material.LAPIS_BLOCK);
        ItemMeta setlspawnm = setlspawn.getItemMeta();
        setlspawnm.setDisplayName(ChatColor.translateAlternateColorCodes('&',"&6⊳ Set&9 lobby &6location"));
        List<String> loreL = new ArrayList<>();
        loreL.add(ChatColor.GRAY+"Click to add the lobby location");
        loreL.add(ChatColor.GRAY+"on the place where you are standing.");
        if(getEndSpawn() == null) {
            loreL.add(ChatColor.GOLD + "Done: " + ChatColor.RED+"No");
        }else{
            loreL.add(ChatColor.GOLD + "Done: " + ChatColor.GREEN+"Yes");
        }
        setlspawnm.setLore(loreL);
        setlspawn.setItemMeta(setlspawnm);
        editMenu.setItem(0, setlspawn);

        ItemStack setgspawn = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta setgspawnm = setgspawn.getItemMeta();
        setgspawnm.setDisplayName(ChatColor.translateAlternateColorCodes('&',"&6⊳ Set&e game &6spawn"));
        List<String> loreG = new ArrayList<>();
        loreG.add(ChatColor.GRAY+"Click to add the game location");
        loreG.add(ChatColor.GRAY+"on the place where you are standing.");
        if(getGameSpawn() == null) {
            loreG.add(ChatColor.GOLD + "Done: " + ChatColor.RED+"No");
        }else{
            loreG.add(ChatColor.GOLD + "Done: " + ChatColor.GREEN+"Yes");
        }
        setgspawnm.setLore(loreG);
        setgspawn.setItemMeta(setgspawnm);

        ItemStack setespawn = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta setespawnm = setespawn.getItemMeta();
        setespawnm.setDisplayName(ChatColor.translateAlternateColorCodes('&',"&6⊳ Set&c ending &6location"));
        List<String> loreE = new ArrayList<>();
        loreE.add(ChatColor.GRAY+"Click to add the starting location");
        loreE.add(ChatColor.GRAY+"on the place where you are standing.");
        if(getEndSpawn() == null) {
            loreE.add(ChatColor.GOLD + "Done: " + ChatColor.RED+"No");
        }else{
            loreE.add(ChatColor.GOLD + "Done: " + ChatColor.GREEN+"Yes");
        }
        setespawnm.setLore(loreE);
        setespawn.setItemMeta(setespawnm);

        editMenu.setItem(2, setespawn);
        editMenu.setItem(1, setgspawn);
        editMenu.setItem(0, setlspawn);

        editMenu.setItem(26, identifier);

        player.openInventory(editMenu);

    }

    public int getDeathHeight(){
        return deathHeight;
    }

    public void setDeathHeight(int deathHeight){
        this.deathHeight = deathHeight;
    }

    public int getMaxPlayers(){
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers1){
        this.maxPlayers = maxPlayers1;
    }

    public int getMinPlayers(){
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers1){
        this.minPlayers = minPlayers1;
    }

    public Location getLobbySpawn() {
        return lobbySpawn;
    }

    public void setLobbySpawn(Location spawn) {
        this.lobbySpawn = spawn;
    }

    public Location getGameSpawn() {
        return gameSpawn;
    }

    public void setGameSpawn(Location spawn) {
        this.gameSpawn = spawn;
    }

    public Location getEndSpawn() {
        return endSpawn;
    }

    public void setEndSpawn(Location spawn) {
        this.endSpawn = spawn;
    }

    public Set<Player> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    public Set<Player> getDeadPlayers() {
        return Collections.unmodifiableSet(playersOut);
    }



    public void killPlayer(Player player, Plugin plugin){
        this.players.remove(player);
        this.playersOut.add(player);
        player.teleport(getLobbySpawn());

        if(state == GameState.INGAME){
            if(getPlayers().size() == 1){
                Player winner = null;
                for(Player p : getPlayers()){
                    winner = p;
                }
                state = GameState.ENDING;
                winner.sendMessage(format("&cYou have won the game!"));
                winner.getScoreboard().getObjective(DisplaySlot.SIDEBAR).unregister();
                winner.teleport(getEndSpawn());
                for(Player p : getDeadPlayers()){
                    //players = null;
                    p.getScoreboard().getObjective(DisplaySlot.SIDEBAR).unregister();
                    p.sendMessage(format("&a"+winner.getDisplayName()+" has won the game!"));
                    p.teleport(getEndSpawn());
                    Bukkit.getScheduler().cancelTask(e);
                }
            }
        }
    }

    public void addPlayer(Player player, Plugin plugin) {
        if (state.canJoin() == true) {
            if (getPlayers().size() < getMaxPlayers()) {
                this.players.add(player);
                if(getPlayers().size() >= getMinPlayers()){
                    if(countdown == -1){
                        startCountDown(plugin);
                    }
                }
                updateScoreboard();
                player.teleport(getLobbySpawn());
                for (Player p : getPlayers()) {
                    p.sendMessage(format("&a&l" + player.getDisplayName() + "&r&7 has joined the game (" + getPlayers().size() + "/" + getMaxPlayers() + ")"));
                }

            } else {
                player.sendMessage(format("&cThis game is full!"));
            }
        }else {
            player.sendMessage(format("&cYou can't join this game right now."));
            player.sendMessage(String.valueOf(state)+" "+ String.valueOf(state.canJoin()));
        }
    }

    public void updateScoreboard(){
      for(Player p : getPlayers()){
          GameScoreBoard.SetPlayerBoard(p, new GameScoreBoard(this));
      }
    }

    public void removePlayer(Player player) {
        this.players.remove(player);
        if(getPlayers().size() < getMinPlayers()){
            if(state == GameState.WAITING){
                countdown = -1;
                Bukkit.getScheduler().cancelTask(c);
            }
        }
        updateScoreboard();
        for (Player p : getPlayers()) {
            p.sendMessage(format("&c&l" + player.getDisplayName() + "&r&7 has left the game (" + getPlayers().size() + "/" + getMaxPlayers() + ")"));
        }

    }
    int c = 0;
    public void startCountDown(Plugin plugin){
        countdown = 30;
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        c = scheduler.scheduleSyncRepeatingTask(plugin, () -> {
            if(state == GameState.WAITING) {
                for (Player player : getPlayers()) {
                    updateScoreboard();
                }
                if(countdown >0) {
                    countdown--;
                }else{
                    for (Player player : getPlayers()) {
                        player.teleport(getGameSpawn());
                    }
                    startCooldown(plugin);
                    state = GameState.INGAME;
                    Bukkit.getScheduler().cancelTask(c);
                }
            }
        }, 0L, 1 * 20L);
    }

    int f = 0;
    public void startCooldown(Plugin plugin){
        countdown = 5;
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        f = scheduler.scheduleSyncRepeatingTask(plugin, () -> {
            for (Player player : getPlayers()) {
                player.sendMessage(format("The floor will start breaking in: "+ countdown));
            }
            if(countdown >0) {
                countdown--;
            }else{
                Bukkit.getScheduler().cancelTask(f);
                startFloorDecay(plugin);
            }
        }, 0L, 1 * 20L);
    }

    public void endGame(Plugin plugin){

    }

    int e = 0;
    public void startFloorDecay(Plugin plugin){
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        e = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                if(state == GameState.INGAME) {
                    for (Player player : getPlayers()) {

                        int x = player.getLocation().getBlockX();
                        int y = player.getLocation().getBlockY();
                        int z = player.getLocation().getBlockZ();
                        World w = player.getLocation().getWorld();

                        Location blockUnder = new Location(w, x, y - 1, z);
                        if (blockUnder.getBlock().getType() == Material.WHITE_STAINED_GLASS) {
                            blockUnder.getBlock().setType(Material.LIME_STAINED_GLASS);
                        } else if (blockUnder.getBlock().getType() == Material.LIME_STAINED_GLASS) {
                            blockUnder.getBlock().setType(Material.ORANGE_STAINED_GLASS);
                        } else if (blockUnder.getBlock().getType() == Material.ORANGE_STAINED_GLASS) {
                            blockUnder.getBlock().setType(Material.RED_STAINED_GLASS);
                        } else if (blockUnder.getBlock().getType() == Material.RED_STAINED_GLASS) {
                            killPlayer(player, plugin);
                        }
                    }
                }
            }
        }, 0L, 1 * 5L);
    }
}