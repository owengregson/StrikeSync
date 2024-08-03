package me.vexmc.strikesync.commands;

import org.bukkit.command.CommandSender;

public interface Command {
    boolean execute(CommandSender sender, String[] args);
}