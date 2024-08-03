package me.vexmc.strikesync.commands;

import me.vexmc.strikesync.StrikeSyncPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

public class AuthorsCommand implements Command {
    private final StrikeSyncPlugin plugin;

    public AuthorsCommand(StrikeSyncPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage(
                Component.text("Strike")
                        .color(NamedTextColor.GOLD)
                        .decorate(TextDecoration.BOLD)
                        .append(Component.text("Sync")
                                .color(NamedTextColor.YELLOW)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" by ")
                                .color(NamedTextColor.WHITE))
                        .append(Component.text("@owengregson")
                                .color(NamedTextColor.YELLOW))
                        .decorate(TextDecoration.UNDERLINED)
                        .hoverEvent(Component.text("Click to view GitHub profile")
                                .color(NamedTextColor.GRAY))
                        .clickEvent(net.kyori.adventure.text.event.ClickEvent.openUrl("https://github.com/owengregson"))
        );
        return true;
    }
}