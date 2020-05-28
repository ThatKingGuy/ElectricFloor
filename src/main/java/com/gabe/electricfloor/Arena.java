package com.gabe.electricfloor;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.DisplaySlot;
import java.util.*;

public class Arena {

    Map<UUID, ItemStack[]> items = new HashMap<UUID, ItemStack[]>();
    Map<UUID, ItemStack[]> armor = new HashMap<UUID, ItemStack[]>();

    private final String name;
    private Location lobbySpawn = null;
    private Location gameSpawn = null;
    private Location endSpawn = null;
    private int glassHeight;
    public GameState state;
    private int maxPlayers = 10;
    private int minPlayers = 2;
    private final Set<Player> players;
    private final Set<Player> playersOut;
    private int countdown  = -1;

    private String prefix = "&3Electric&bFloor &7&l⋙ &r&6";

    public void storeAndClearInventory(Player player){
        UUID uuid = player.getUniqueId();

        ItemStack[] contents = player.getInventory().getContents();
        ItemStack[] armorContents = player.getInventory().getArmorContents();

        items.put(uuid, contents);
        armor.put(uuid, armorContents);

        player.getInventory().clear();

        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);
    }

    public void restoreInventory(Player player){
        UUID uuid = player.getUniqueId();

        ItemStack[] contents = items.get(uuid);
        ItemStack[] armorContents = armor.get(uuid);

        if(contents != null){
            player.getInventory().setContents(contents);
        }
        else{//if the player has no inventory contents, clear their inventory
            player.getInventory().clear();
        }

        if(armorContents != null){
            player.getInventory().setArmorContents(armorContents);
        }
        else{//if the player has no armor, set the armor to null
            player.getInventory().setHelmet(null);
            player.getInventory().setChestplate(null);
            player.getInventory().setLeggings(null);
            player.getInventory().setBoots(null);
        }
    }


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
        if(getLobbySpawn() == null) {
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
        loreE.add(ChatColor.GRAY+"Click to add the ending location");
        loreE.add(ChatColor.GRAY+"on the place where you are standing.");
        if(getEndSpawn() == null) {
            loreE.add(ChatColor.GOLD + "Done: " + ChatColor.RED+"No");
        }else{
            loreE.add(ChatColor.GOLD + "Done: " + ChatColor.GREEN+"Yes");
        }
        setespawnm.setLore(loreE);
        setespawn.setItemMeta(setespawnm);

        ItemStack sg = new ItemStack(Material.RED_STAINED_GLASS);
        ItemMeta sgm = sg.getItemMeta();
        sgm.setDisplayName(ChatColor.translateAlternateColorCodes('&',"&6⊳ Set&c glass &6height"));
        List<String> loresg = new ArrayList<>();
        loresg.add(ChatColor.GRAY+"Click to add the glass height");
        loresg.add(ChatColor.GRAY+"on the height where you are standing.");
        loresg.add(ChatColor.GOLD + "Currently: " + ChatColor.GREEN+getGlassHeight());

        sgm.setLore(loresg);
        sg.setItemMeta(sgm);


        ItemStack minpl = new ItemStack(Material.FEATHER, getMinPlayers());
        ItemMeta minplm = setespawn.getItemMeta();
        minplm.setDisplayName(ChatColor.translateAlternateColorCodes('&',"&6⊳ Set min players"));
        List<String> minplL = new ArrayList<>();
        minplL.add(ChatColor.GRAY+"Currently: "+ChatColor.GREEN + getMinPlayers());
        minplL.add(ChatColor.GRAY+"Left -1 Right +1");
        minplm.setLore(minplL);
        minpl.setItemMeta(minplm);

        ItemStack maxpl = new ItemStack(Material.BONE, getMaxPlayers());
        ItemMeta maxplm = setespawn.getItemMeta();
        maxplm.setDisplayName(ChatColor.translateAlternateColorCodes('&',"&6⊳ Set max players"));
        List<String> maxplL = new ArrayList<>();
        maxplL.add(ChatColor.GRAY+"Currently: "+ChatColor.GREEN + getMaxPlayers());
        maxplL.add(ChatColor.GRAY+"Left -1 Right +1");
        maxplm.setLore(maxplL);
        maxpl.setItemMeta(maxplm);

        ItemStack sign = new ItemStack(Material.OAK_SIGN, 1);
        ItemMeta smeta = sign.getItemMeta();
        smeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6⊳ Create Sign"));
        sign.setItemMeta(smeta);
        editMenu.setItem(6, sign);
        editMenu.setItem(5, maxpl);
        editMenu.setItem(4, minpl);
        editMenu.setItem(3, sg);
        editMenu.setItem(2, setespawn);
        editMenu.setItem(1, setgspawn);
        editMenu.setItem(0, setlspawn);

        editMenu.setItem(26, identifier);

        player.openInventory(editMenu);

    }

    public String color(String txt){
        return ChatColor.translateAlternateColorCodes('&', txt);
    }

    public void spectate(Player player){
        Inventory spectatorMenu = Bukkit.createInventory(null,27, ChatColor.BOLD + "Spectating");

        List<ItemStack> heads = new ArrayList<>();
        for(Player p : getPlayers()){
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1, (short) SkullType.PLAYER.ordinal());

            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwningPlayer(p);
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + p.getName());
            skull.setItemMeta(meta);

            heads.add(skull);
        }
        int index = 0;
        for(ItemStack i : heads){
            spectatorMenu.setItem(index, i);
            index++;
        }

        player.openInventory(spectatorMenu);

    }

    public int getGlassHeight(){
        return glassHeight;
    }

    public void setGlassHeight(int glassHeight){
        this.glassHeight = glassHeight;
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

    public void checkWinner(Plugin plugin){
        if(state == GameState.INGAME) {
            if (getPlayers().size() == 1) {
                Player winner = null;
                for (Player p : getPlayers()) {
                    winner = p;
                }
                state = GameState.ENDING;
                winner.sendMessage(format("&aYou have won the game!"));
                int wins = plugin.getConfig().getInt("stats."+winner.getUniqueId()+".wins");
                plugin.getConfig().set("stats."+winner.getUniqueId()+".wins", wins + 1);
                int games = plugin.getConfig().getInt("stats."+winner.getUniqueId()+".games");
                plugin.getConfig().set("stats."+winner.getUniqueId()+".games", games + 1);
                plugin.saveConfig();

                for (Player p : getDeadPlayers()) {
                    int pgames = plugin.getConfig().getInt("stats."+p.getUniqueId()+".games");
                    plugin.getConfig().set("stats."+p.getUniqueId()+".games", pgames + 1);
                    plugin.saveConfig();
                    p.sendMessage(format("&a" + winner.getDisplayName() + " has won the game!"));

                }

                startCelebration(plugin, winner);
                Bukkit.getScheduler().cancelTask(e);
                endGame(plugin);
            }
        }
    }

    public void killPlayer(Player player, Plugin plugin){
        for (Player p : playersOut) {

            p.sendMessage(format("&c" + player.getDisplayName() + " has been eliminated!"));
        }
        for (Player p : players) {

            p.sendMessage(format("&c" + player.getDisplayName() + " has been eliminated!"));
        }

        players.remove(player);
        playersOut.add(player);
        player.teleport(getLobbySpawn());

        if(state == GameState.INGAME){
            checkWinner(plugin);
        }
    }

    public void spawnFireworks(Location location, int amount){
        Location loc = location;
        Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();

        fwm.setPower(2);
        Random rand = new Random();
        int randomNum = rand.nextInt((5 - 1) + 1) + 1;

        if(randomNum == 1) {
            fwm.addEffect(FireworkEffect.builder().withColor(Color.LIME).flicker(true).build());
        }else if(randomNum == 2) {
            fwm.addEffect(FireworkEffect.builder().withColor(Color.RED).flicker(true).build());
        }else if(randomNum == 3) {
            fwm.addEffect(FireworkEffect.builder().withColor(Color.BLUE).flicker(true).build());
        }else if(randomNum == 4) {
            fwm.addEffect(FireworkEffect.builder().withColor(Color.ORANGE).flicker(true).build());
        }else if(randomNum == 5) {
            fwm.addEffect(FireworkEffect.builder().withColor(Color.YELLOW).flicker(true).build());
        }

        fw.setFireworkMeta(fwm);
        fw.detonate();

        for(int i = 0;i<amount; i++){
            Firework fw2 = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
            fw2.setFireworkMeta(fwm);
        }
    }

    int h = 0;
    int timeUntilTeleport = 0;
    public void startCelebration(Plugin plugin, Player winner){
        timeUntilTeleport = 5;
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        h = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                if(timeUntilTeleport > 0){
                    timeUntilTeleport --;
                    spawnFireworks(winner.getLocation(), 2);
                }else{
                    winner.teleport(getEndSpawn());
                    winner.getScoreboard().getObjective(DisplaySlot.SIDEBAR).unregister();
                    players.remove(winner);
                    restoreInventory(winner);
                    for (Player p : getDeadPlayers()) {
                        p.getScoreboard().getObjective(DisplaySlot.SIDEBAR).unregister();
                        playersOut.remove(p);
                        p.teleport(getEndSpawn());
                        restoreInventory(p);
                    }
                    Bukkit.getScheduler().cancelTask(h);
                }
            }
        }, 0L, 1 * 20L);
    }

    public void addPlayer(Player player, Plugin plugin) {
        if (state.canJoin() == true) {
            if (getPlayers().size() < getMaxPlayers()) {
                this.players.add(player);
                storeAndClearInventory(player);
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
        }
    }

    public void updateScoreboard(){
      for(Player p : getPlayers()){
          GameScoreBoard.SetPlayerBoard(p, new GameScoreBoard(this));
      }
    }

    public void removePlayer(Player player) {
        this.players.remove(player);
        restoreInventory(player);
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
        countdown = 7;
        Title title = new Title();
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        f = scheduler.scheduleSyncRepeatingTask(plugin, () -> {
            for (Player player : getPlayers()) {
                //player.sendMessage(format("The floor will start breaking in: "+ countdown));

                if(countdown==7) {
                    title.send(player, color("Welcome to..."), color("&cElectric Floor"), 0, 1, 0);
                }else if(countdown==6) {
                    title.send(player, color("Welcome to..."), color("&cElectric Floor"), 0, 1, 0);
                }else if(countdown==5) {
                    title.send(player, color("&a5"), color("&7until start!"), 0, 1, 0);
                }else if(countdown==4) {
                    title.send(player, color("&a4"), color("&7until start!"), 0, 1, 0);
                }else if(countdown==3) {
                    title.send(player, color("&63"), color("&7until start!"), 0, 1, 0);
                }else if(countdown==2) {
                    title.send(player, color("&62"), color("&7until start!"), 0, 1, 0);
                }else if(countdown==1) {
                    title.send(player, color("&c1"), color("&7until start!"), 0, 1, 0);
                }else if(countdown==0) {
                    title.send(player, color("&aGO!"), color("Keep running, avoid the red blocks!"), 0, 1, 0);
                }

                player.playSound(player.getLocation(),Sound.BLOCK_NOTE_BLOCK_HARP,1, 3);
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
        Location check = getGameSpawn();
        //check.setY(check.getBlockY()-1);
        for(int x = 0; x<100; x++){
            for(int z = 0; z<100; z++){
                double newX = check.getBlockX() - 50+x;
                double newZ = check.getBlockZ() - 50+z;

                Location c = new Location(getGameSpawn().getWorld(), newX, getGlassHeight() ,newZ);

                Block block = c.getBlock();
                if(block.getType() == Material.LIME_STAINED_GLASS || block.getType() == Material.ORANGE_STAINED_GLASS || block.getType() == Material.RED_STAINED_GLASS){
                    block.setType(Material.WHITE_STAINED_GLASS);
                }
            }
        }

        state = GameState.WAITING;

        if(getPlayers().size() < getMinPlayers()){
            if(state == GameState.WAITING){
                countdown = -1;
                Bukkit.getScheduler().cancelTask(c);
            }
        }
        updateScoreboard();
    }


    int e = 0;
    public void startFloorDecay(Plugin plugin){
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        e = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {

            @Override
            public void run() {
                if(state != null) {
                    if (state == GameState.INGAME) {
                        for (Player player : players) {
                            if (player != null) {
                                if (players.contains(player)) {
                                    if (player.getLocation() != null) {
                                        int x = player.getLocation().getBlockX();
                                        int y = player.getLocation().getBlockY();
                                        int z = player.getLocation().getBlockZ();
                                        World w = player.getLocation().getWorld();

                                        Location blockUnder = new Location(w, x, y - 1, z);
                                        if (blockUnder.getBlock() != null) {
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
                            }
                        }
                    }
                }
            }
        }, 0L, 1 * 5L);
    }


}