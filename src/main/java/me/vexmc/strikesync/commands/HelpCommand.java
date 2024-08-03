package me.vexmc.strikesync.commands;

import me.vexmc.strikesync.StrikeSyncPlugin;
import org.bukkit.command.CommandSender;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
public class HelpCommand implements Command {

    private final StrikeSyncPlugin plugin;

    public HelpCommand(StrikeSyncPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage(Component.text()
                .append(Component.text("Strike").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
                .append(Component.text("Sync").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD))
                .append(Component.text(" 1.0").color(NamedTextColor.WHITE))
                .append(Component.newline())
                .append(Component.text("/ss help").color(NamedTextColor.YELLOW))
                .append(Component.text(" - Show this help message").color(NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text("/ss authors").color(NamedTextColor.YELLOW))
                .append(Component.text(" - Show the plugin developers").color(NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text("/ss reload").color(NamedTextColor.YELLOW))
                .append(Component.text(" - Reload configuration and listeners").color(NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text("/ss toggle").color(NamedTextColor.YELLOW))
                .append(Component.text(" - Disable/enable async hit registration").color(NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text("/ss knockback <enable | disable | status>").color(NamedTextColor.YELLOW))
                .append(Component.text(" - Manage knockback settings").color(NamedTextColor.GRAY)));
        return true;
    }
}
