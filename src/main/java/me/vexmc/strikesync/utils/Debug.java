package me.vexmc.strikesync.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Debug {

    private static Plugin plugin;

    private Debug() {}

    public static void initialize(Plugin plugin) {
        Debug.plugin = plugin;
    }

    private static void checkInitialized() {
        if (plugin == null) {
            throw new IllegalStateException("Debug not initialized. Call Debug.initialize(plugin) first.");
        }
    }

    public static void log(String message) {
        checkInitialized();
        if(!plugin.getConfig().getBoolean("debug.enabled")) return;
        plugin.getLogger().info("[DEBUG] " + message);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static void log(String message, LivingEntity entity) {
        checkInitialized();
        if(!plugin.getConfig().getBoolean("debug.enabled")) return;
        entity.sendMessage(Component.text("Strike")
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD)
                .append(Component.text("Sync")
                        .color(NamedTextColor.YELLOW)
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" " + message)
                        .color(NamedTextColor.WHITE)));
        plugin.getLogger().info("[DEBUG] " + message);
    }
}