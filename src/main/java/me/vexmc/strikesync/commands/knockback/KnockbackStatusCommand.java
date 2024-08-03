package me.vexmc.strikesync.commands.knockback;

import me.vexmc.strikesync.StrikeSyncPlugin;
import me.vexmc.strikesync.commands.Command;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

public class KnockbackStatusCommand implements Command {
    private final StrikeSyncPlugin plugin;

    public KnockbackStatusCommand(StrikeSyncPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        boolean enabled = plugin.getKnockbackModule().isKnockbackEnabled();
        sender.sendMessage(
                Component.text("Strike")
                        .color(NamedTextColor.GOLD)
                        .decorate(TextDecoration.BOLD)
                        .append(Component.text("Sync")
                                .color(NamedTextColor.YELLOW)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" Knockback is currently ")
                                .color(NamedTextColor.WHITE))
                        .append(Component.text((enabled ? "enabled" : "disabled"))
                                .color(enabled ? NamedTextColor.GREEN : NamedTextColor.RED)));
        return true;
    }
}