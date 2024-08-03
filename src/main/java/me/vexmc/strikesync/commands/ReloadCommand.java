package me.vexmc.strikesync.commands;

import me.vexmc.strikesync.StrikeSyncPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements Command {
    private final StrikeSyncPlugin plugin;

    public ReloadCommand(StrikeSyncPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        plugin.reload();
        sender.sendMessage(
                Component.text("Strike")
                        .color(NamedTextColor.GOLD)
                        .decorate(TextDecoration.BOLD)
                        .append(Component.text("Sync")
                                .color(NamedTextColor.YELLOW)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" reloaded!")
                                .color(NamedTextColor.BLUE)));
        return true;
    }
}