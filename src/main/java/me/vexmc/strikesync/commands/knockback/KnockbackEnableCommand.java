package me.vexmc.strikesync.commands.knockback;

import me.vexmc.strikesync.StrikeSyncPlugin;
import me.vexmc.strikesync.commands.Command;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

public class KnockbackEnableCommand implements Command {
    private final StrikeSyncPlugin plugin;

    public KnockbackEnableCommand(StrikeSyncPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        plugin.getConfig().set("knockback.enabled", true);
        plugin.saveConfig();
        plugin.reload();
        sender.sendMessage(
                Component.text("Strike")
                        .color(NamedTextColor.GOLD)
                        .decorate(TextDecoration.BOLD)
                        .append(Component.text("Sync")
                                .color(NamedTextColor.YELLOW)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" Knockback enabled!")
                                .color(NamedTextColor.GREEN)));
        return true;
    }
}
