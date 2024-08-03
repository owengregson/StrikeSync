package me.vexmc.strikesync.commands.knockback;

import me.vexmc.strikesync.StrikeSyncPlugin;
import me.vexmc.strikesync.commands.Command;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

public class KnockbackDisableCommand implements Command {
    private final StrikeSyncPlugin plugin;

    public KnockbackDisableCommand(StrikeSyncPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        plugin.getConfig().set("knockback.enabled", false);
        plugin.saveConfig();
        plugin.reload();
        sender.sendMessage(
                Component.text("Strike")
                        .color(NamedTextColor.GOLD)
                        .decorate(TextDecoration.BOLD)
                        .append(Component.text("Sync")
                                .color(NamedTextColor.YELLOW)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" Knockback disabled!")
                                .color(NamedTextColor.RED)));
        return true;
    }
}