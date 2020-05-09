package com.gabe.electricfloor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class Completion implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(command.getLabel().equalsIgnoreCase("ef")){
            if(args.length == 1){
                List<String> c = new ArrayList<>();
                c.add("create");
                c.add("remove");
                c.add("join");
                c.add("leave");
                c.add("edit");
                c.add("list");
                return c;
            }
            if(args.length == 2) {
                if(args[0].equalsIgnoreCase("edit")){
                    List<String> c = new ArrayList<>();
                    for(Arena a : ElectricFloor.getArenaManager().getArenaList()){
                        c.add(a.getName());
                    }
                    return c;
                }else if(args[0].equalsIgnoreCase("remove")){
                    List<String> c = new ArrayList<>();
                    for(Arena a : ElectricFloor.getArenaManager().getArenaList()){
                        c.add(a.getName());
                    }
                    return c;
                }else if(args[0].equalsIgnoreCase("join")){
                    List<String> c = new ArrayList<>();
                    for(Arena a : ElectricFloor.getArenaManager().getArenaList()){
                        c.add(a.getName());
                    }
                    return c;
                }
            }
        }
        return null;
    }
}
