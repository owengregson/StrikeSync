package me.vexmc.strikesync.commands;

import me.vexmc.strikesync.StrikeSyncPlugin;
import me.vexmc.strikesync.commands.knockback.KnockbackCommand;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class StrikeSyncCommand implements CommandExecutor, TabCompleter {
    private final StrikeSyncPlugin plugin;
    private final Map<String, Command> subCommands = new HashMap<>();

    public StrikeSyncCommand(StrikeSyncPlugin plugin) {
        this.plugin = plugin;
        registerSubCommands();
    }

    private void registerSubCommands() {
        subCommands.put("help", new HelpCommand(plugin));
        subCommands.put("authors", new AuthorsCommand(plugin));
        subCommands.put("reload", new ReloadCommand(plugin));
        subCommands.put("toggle", new ToggleCommand(plugin));
        subCommands.put("knockback", new KnockbackCommand(plugin));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String label, String[] args) {
        if (args.length < 1) {
            return subCommands.get("help").execute(sender, args);
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("strikesync.command.use")) {
                player.sendMessage("You do not have permission to use this command.");
                return true;
            }
        }

        Command subCommand = subCommands.get(args[0].toLowerCase());
        if (subCommand != null) {
            return subCommand.execute(sender, args);
        }

        // Send usage message
        sender.sendMessage("Usage: /ss help");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return new ArrayList<>(subCommands.keySet());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("knockback")) {
            return Arrays.asList("enable", "disable", "status");
        }
        return Collections.emptyList();
    }
}
