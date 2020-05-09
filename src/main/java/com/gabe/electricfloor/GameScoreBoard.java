package com.gabe.electricfloor;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class GameScoreBoard {

    org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
    private Scoreboard board = manager.getNewScoreboard();

    public GameScoreBoard(Arena arena){
        if(arena.state == GameState.WAITING){
            Objective p = board.registerNewObjective("players","");
            p.setDisplaySlot(DisplaySlot.SIDEBAR);
            p.setDisplayName(ChatColor.translateAlternateColorCodes('&',"&3Electric&bFloor"));
            Score s = p.getScore("Players: " + ChatColor.YELLOW + arena.getPlayers().size() + "/" + arena.getMaxPlayers());
            s.setScore(10);

            Score m = p.getScore("Min Players: " + ChatColor.YELLOW + arena.getMinPlayers());
            m.setScore(9);
            if(arena.getCountdown() == -1) {
                Score c = p.getScore("Waiting for players...");
                c.setScore(8);
            }else{
                Score c = p.getScore("Starting in: " + ChatColor.YELLOW + arena.getCountdown());
                c.setScore(8);
            }

        }else{
            Objective p = board.registerNewObjective("players","");
            p.setDisplaySlot(DisplaySlot.SIDEBAR);
            p.setDisplayName(ChatColor.translateAlternateColorCodes('&',"&3Electric&bFloor"));
            Score s = p.getScore("Players Left: " + ChatColor.YELLOW + arena.getPlayers().size());
            s.setScore(10);

        }
    }

    public Scoreboard getBoard() {
        return board;
    }

    public static void SetPlayerBoard(Player player, GameScoreBoard s){
        player.setScoreboard(s.getBoard());
    }
}
