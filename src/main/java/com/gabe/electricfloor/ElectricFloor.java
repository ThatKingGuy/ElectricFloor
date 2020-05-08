package com.gabe.electricfloor;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import java.util.Set;

public final class ElectricFloor extends JavaPlugin {

    private static ArenaManager arenaManager;
    public String prefix = "&3Electric&bFloor &7&l⋙ &r&6";

    public void onEnable() {

        getServer().getPluginManager().registerEvents(new Events(), this);
        arenaManager = new ArenaManager(this);
        arenaManager.deserialise();
    }

    public void onDisable() {
        arenaManager.serialise();
    }

    public static ArenaManager getArenaManager() {
        return arenaManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player player = (Player) sender;
            if(label.equalsIgnoreCase("ef")){
                if(args.length ==0){
                    player.sendMessage(format("version 1.0"));
                }
                if(args.length > 0){
                    if(args[0].equalsIgnoreCase("list")){
                        Set<Arena> arenasSet = getArenaManager().getArenaList();
                        if(arenasSet.size() == 0){
                            player.sendMessage(format("There are no arenas."));
                        }else {
                            player.sendMessage(color("&b----- &3Arenas&b -----"));
                            for (Arena a : arenasSet) {
                                player.sendMessage(color("&d"+a.getName()));
                            }
                            player.sendMessage(color("&b-----------------"));
                        }
                    }else if(args[0].equalsIgnoreCase(  "create")){
                        if(args.length > 1){
                            String arenaName = args[1];
                            Arena check = getArenaManager().getArena(arenaName);
                            if(check != null){
                                player.sendMessage(format("There is already an arena with this name."));
                            }else{
                                Arena arena = new Arena(arenaName);
                                getArenaManager().addArena(arena);
                                player.sendMessage(format("Created arena &d\""+arenaName+"\"&6."));
                            }
                        }else{
                            player.sendMessage(format("&cIncorrect usage. do /ef create <name>"));
                        }
                    }else if(args[0].equalsIgnoreCase("remove")){
                        if(args.length >1){
                            Arena arena = getArenaManager().getArena(args[1]);
                            if(arena != null){
                                player.sendMessage(format("Removed arena "+args[1]+"."));
                                arenaManager.removeArena(arena);
                            }else {
                                player.sendMessage(format("There is no arena with that name."));
                            }
                        }else{
                            player.sendMessage(format("&cIncorrect usage. do /ef remove <name>"));
                        }
                    }else if(args[0].equalsIgnoreCase("join")){
                        if(getArenaManager().getArena(player) == null) {
                            if (args.length > 1) {
                                Arena arena = getArenaManager().getArena(args[1]);
                                if (arena != null) {
                                    arena.addPlayer(player, this);

                                } else {
                                    player.sendMessage(format("There is no arena with that name."));
                                }
                            } else {
                                player.sendMessage(format("&cIncorrect usage. do /ef join <name>"));
                            }
                        }else{
                            player.sendMessage(format("&cYou are already in a match!"));
                        }
                    }else if(args[0].equalsIgnoreCase("edit")){
                        if (args.length > 1) {
                            Arena arena = getArenaManager().getArena(args[1]);
                            if (arena != null) {
                                arena.edit(player);
                                player.sendMessage(format("Editing Arena."));
                            } else {
                                player.sendMessage(format("There is no arena with that name."));
                            }
                        } else {
                            player.sendMessage(format("&cIncorrect usage. do /ef edit <name>"));
                        }
                    }else if(args[0].equalsIgnoreCase("leave")){
                        boolean inArena = false;
                        for(Arena arena : getArenaManager().getArenaList()) {
                            if(arena.getPlayers().contains(player)) {
                                arena.removePlayer(player);
                                inArena = true;
                                player.getScoreboard().getObjective(DisplaySlot.SIDEBAR).unregister();
                                player.sendMessage(format("Left arena "+arena.getName()+"."));
                            }
                        }

                        if(!inArena){
                            player.sendMessage(format("You are not in a game!"));
                        }
                    }
                }
            }

        }else{
            sender.sendMessage(format("&cOnly players can execute this command."));
        }
        return true;
    }

    public String format(String text){
        return ChatColor.translateAlternateColorCodes('&', prefix + text);
    }

    public String color(String text){
        return ChatColor.translateAlternateColorCodes('&', text);
    }

}
