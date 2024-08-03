package me.vexmc.strikesync.commands;

import me.vexmc.strikesync.StrikeSyncPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

public class ToggleCommand implements Command {
    private final StrikeSyncPlugin plugin;

    public ToggleCommand(StrikeSyncPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (plugin.isListening()) {
            plugin.getConfig().set("async-hitreg.enabled", false);
            plugin.saveConfig();
            plugin.reload();
            sender.sendMessage(
                    Component.text("Strike")
                            .color(NamedTextColor.GOLD)
                            .decorate(TextDecoration.BOLD)
                            .append(Component.text("Sync")
                                    .color(NamedTextColor.YELLOW)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" Hit-reg disabled!")
                                    .color(NamedTextColor.RED)));
        } else {
            plugin.getConfig().set("async-hitreg.enabled", true);
            plugin.saveConfig();
            plugin.reload();
            sender.sendMessage(
                    Component.text("Strike")
                            .color(NamedTextColor.GOLD)
                            .decorate(TextDecoration.BOLD)
                            .append(Component.text("Sync")
                                    .color(NamedTextColor.YELLOW)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" Hit-reg enabled!")
                                    .color(NamedTextColor.GREEN)));
        }
        return true;
    }
}