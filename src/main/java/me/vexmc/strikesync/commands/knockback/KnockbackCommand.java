package me.vexmc.strikesync.commands.knockback;

import me.vexmc.strikesync.StrikeSyncPlugin;
import me.vexmc.strikesync.commands.Command;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class KnockbackCommand implements Command {
    private final Map<String, Command> subCommands = new HashMap<>();

    public KnockbackCommand(StrikeSyncPlugin plugin) {
        subCommands.put("enable", new KnockbackEnableCommand(plugin));
        subCommands.put("disable", new KnockbackDisableCommand(plugin));
        subCommands.put("status", new KnockbackStatusCommand(plugin));
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            return false;
        }

        Command subCommand = subCommands.get(args[1].toLowerCase());
        if (subCommand != null) {
            return subCommand.execute(sender, args);
        }

        return false;
    }
}
