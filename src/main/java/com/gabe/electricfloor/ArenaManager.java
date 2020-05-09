package com.gabe.electricfloor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class ArenaManager {

    private final ElectricFloor plugin;
    private final FileConfiguration config;
    private final Set<Arena> arenaSet;



    public ArenaManager(ElectricFloor plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.arenaSet = new HashSet<>();
    }

    public void deserialise() {
        Logger logger = Bukkit.getLogger();
        ConfigurationSection configSection = config.getConfigurationSection("Arenas");
        if(configSection == null) return;

        //configSection.getKeys(false).forEach(s -> arenaSet.add(new Arena(s)));

        for (String name : configSection.getKeys(false)) {
            Arena arena = new Arena(name);
            if(config.get("Arenas."+name+".maxPlayers") != null){
                arena.setMaxPlayers(config.getInt("Arenas."+name+".maxPlayers"));
            }

            if(config.get("Arenas."+name+".minPlayers") != null){
                arena.setMinPlayers(config.getInt("Arenas."+name+".minPlayers"));
            }

            if(config.get("Arenas."+name+".lobbySpawn") != null){
                arena.setLobbySpawn((Location) config.get("Arenas."+name+".lobbySpawn"));
            }

            if(config.get("Arenas."+name+".gameSpawn") != null){
                arena.setGameSpawn((Location) config.get("Arenas."+name+".gameSpawn"));
            }

            if(config.get("Arenas."+name+".endSpawn") != null){
                arena.setEndSpawn((Location) config.get("Arenas."+name+".endSpawn"));
            }

            if(config.get("Arenas."+name+".glassHeight") != null){
                arena.setGlassHeight((Integer) config.get("Arenas."+name+".glassHeight"));
            }



            arenaSet.add(arena);
        }
    }

    public void serialise() {
        arenaSet.forEach(arena -> config.set("Arenas." + arena.getName()+".maxPlayers", arena.getMaxPlayers()));
        arenaSet.forEach(arena -> config.set("Arenas." + arena.getName()+".minPlayers", arena.getMinPlayers()));
        arenaSet.forEach(arena -> config.set("Arenas." + arena.getName()+".glassHeight", arena.getGlassHeight()));
        arenaSet.forEach(arena -> config.set("Arenas." + arena.getName()+".lobbySpawn", arena.getLobbySpawn()));
        arenaSet.forEach(arena -> config.set("Arenas." + arena.getName()+".gameSpawn", arena.getGameSpawn()));
        arenaSet.forEach(arena -> config.set("Arenas." + arena.getName()+".endSpawn", arena.getEndSpawn()));
        plugin.saveConfig();
    }

    public Arena getArena(Player player) {
        return arenaSet.stream().filter(arena -> arena.getPlayers().contains(player)).findFirst().orElse(null);
    }

    public Arena getArena(String key) {
        return arenaSet.stream().filter(arena -> arena.getName().equals(key)).findFirst().orElse(null);
    }

    public Set<Arena> getArenaList() {
        return Collections.unmodifiableSet(arenaSet);
    }

    public void addArena(Arena arena) {
        this.arenaSet.add(arena);
        serialise();
    }

    public void removeArena(Arena arena) {
        this.arenaSet.remove(arena);
        config.set("Arenas." + arena.getName(), null);
        serialise();
    }
}