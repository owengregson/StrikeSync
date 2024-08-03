package me.vexmc.strikesync.events;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AsyncHitRegisterEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled;
    private final Player damager;
    private final Damageable target;
    private double damage;

    public AsyncHitRegisterEvent(@NotNull Player damager, @NotNull Damageable target, double damage) {
        super(true);
        this.damager = damager;
        this.target = target;
        this.damage = damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public double getDamage() {
        return damage;
    }

    public Player getDamager() {
        return damager;
    }

    public Damageable getTarget() {
        return target;
    }

    public EntityType getEntityType() {
        return target.getType();
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}