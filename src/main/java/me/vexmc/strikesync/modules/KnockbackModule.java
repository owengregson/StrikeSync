package me.vexmc.strikesync.modules;

import me.vexmc.strikesync.StrikeSyncPlugin;
import me.vexmc.strikesync.utils.Debug;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KnockbackModule implements Listener {

    private final StrikeSyncPlugin plugin;
    private boolean knockbackEnabled;
    private boolean knockbackAsync;
    private double knockbackHorizontal;
    private double knockbackVertical;
    private double knockbackVerticalLimit;
    private double knockbackExtraHorizontal;
    private double knockbackExtraVertical;
    private double knockbackLimitHorizontal;
    private boolean armorResistanceEnabled;
    private double frictionX;
    private double frictionY;
    private double frictionZ;
    private double sprintFactor;
    private final Map<UUID, Vector> playerKnockbackMap = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    public KnockbackModule(StrikeSyncPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void enableKnockback() {
        knockbackEnabled = true;
    }

    public void disableKnockback() {
        knockbackEnabled = false;
    }

    public boolean isKnockbackEnabled() {
        return knockbackEnabled;
    }

    // Reload the configuration settings
    public void reload() {
        knockbackEnabled = plugin.getConfig().getBoolean("knockback.enabled", true);
        knockbackAsync = plugin.getConfig().getBoolean("knockback.async", false);
        knockbackHorizontal = plugin.getConfig().getDouble("knockback.base.horizontal", 0.4);
        knockbackVertical = plugin.getConfig().getDouble("knockback.base.vertical", 0.4);
        knockbackExtraHorizontal = plugin.getConfig().getDouble("knockback.extra.horizontal", 0.5);
        knockbackExtraVertical = plugin.getConfig().getDouble("knockback.extra.vertical", 0.1);
        knockbackVerticalLimit = plugin.getConfig().getDouble("knockback.limits.vertical", 0.4);
        knockbackLimitHorizontal = plugin.getConfig().getDouble("knockback.limits.horizontal", -1);
        frictionX = plugin.getConfig().getDouble("knockback.friction.x", 0.5);
        frictionY = plugin.getConfig().getDouble("knockback.friction.y", 0.5);
        frictionZ = plugin.getConfig().getDouble("knockback.friction.z", 0.5);
        sprintFactor = plugin.getConfig().getDouble("knockback.modifiers.sprint", 1.0);
        armorResistanceEnabled = plugin.getConfig().getBoolean("knockback.modifiers.armor-resistance", false);

        Debug.log("Knockback settings reloaded: enabled=" + knockbackEnabled + ", async=" + knockbackAsync);
    }

    // Remove the player from the knockback map when they quit
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        playerKnockbackMap.remove(e.getPlayer().getUniqueId());
    }

    // Handle player velocity events
    // Priority = lowest because we are ignoring the existing velocity, which could break other plugins
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerVelocityEvent(PlayerVelocityEvent event) {
        if (knockbackEnabled) {
            UUID playerUUID = event.getPlayer().getUniqueId();
            if (!playerKnockbackMap.containsKey(playerUUID)) return;
            Debug.log("Applying velocity to " + event.getPlayer().getName(), event.getPlayer());
            event.setVelocity(playerKnockbackMap.get(playerUUID));
            playerKnockbackMap.remove(playerUUID);
        }
    }

    // Handle entity damage events to disable netherite knockback resistance if applicable
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player) || !armorResistanceEnabled) return;
        Player player = (Player) event.getEntity();
        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        assert attribute != null;
        attribute.getModifiers().forEach(attribute::removeModifier);
    }

    // Only need MONITOR priority since actual knockback happens on the velocity change event
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageEntity(EntityDamageByEntityEvent event) {
        if (!knockbackEnabled) return;
        if (!(event.getDamager() instanceof LivingEntity)) return;
        LivingEntity attacker = (LivingEntity) event.getDamager();
        if (!(event.getEntity() instanceof Player)) return;
        LivingEntity victim = (LivingEntity) event.getEntity();

        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;

        Debug.log("EntityDamageEntity triggered A: " + attacker.getName() + " V: " + victim.getName(), attacker);

        if (knockbackAsync) {
            // TODO: Fix player getting like 5x vertical knockback when async enabled
            // TODO: Fix player getting like 5x vertical knockback when async enabled
            // TODO: Fix player getting like 5x vertical knockback when async enabled
            // TODO: Fix player getting like 5x vertical knockback when async enabled
            // TODO: Fix player getting like 5x vertical knockback when async enabled
            executorService.execute(() -> calculateAndApplyKnockback(attacker, victim));
        } else {
            // Perform knockback calculation synchronously
            calculateAndApplyKnockback(attacker, victim);
        }

        // Sometimes PlayerVelocityEvent doesn't fire, so we need to
        // remove data to not mess up later events when this happens
        Bukkit.getScheduler().runTaskLater(plugin, () -> playerKnockbackMap.remove(victim.getUniqueId()), 1L);
    }

    public void forceAttack(LivingEntity attacker, Entity target) {
        if (!(attacker instanceof Player) || !(target instanceof LivingEntity)) return;

        Player playerAttacker = (Player) attacker;
        LivingEntity livingTarget = (LivingEntity) target;

        // TODO: Rewrite this entire thing (it doesn't work.)
        // TODO: Rewrite this entire thing (it doesn't work.)
        // TODO: Rewrite this entire thing (it doesn't work.)
        // TODO: Rewrite this entire thing (it doesn't work.)
        // TODO: Rewrite this entire thing (it doesn't work.)
        new BukkitRunnable() {
            @Override
            public void run() {
                final Constructor<EntityDamageByEntityEvent> constructor = getEntityDamageByEntityEventConstructor();
                EntityDamageByEntityEvent event;
                try {
                    event = constructor.newInstance(attacker, target, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 0.0);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
                Bukkit.getPluginManager().callEvent(event);
                if(event.isCancelled()) {
                    Debug.log("EntityDamageByEntityEvent was cancelled", playerAttacker);
                    return;
                }
                double finalDamage = event.getFinalDamage();
                livingTarget.damage(finalDamage, playerAttacker);
                Debug.log("Applied " + Debug.round(finalDamage,2) + " dmg to " + livingTarget.getName(), playerAttacker);
            }

            @NotNull
            private Constructor<EntityDamageByEntityEvent> getEntityDamageByEntityEventConstructor() {
                Constructor<EntityDamageByEntityEvent> constructor;
                try {
                    constructor = EntityDamageByEntityEvent.class.getDeclaredConstructor(
                            Entity.class, Entity.class, EntityDamageEvent.DamageCause.class, double.class);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
                constructor.setAccessible(true);
                return constructor;
            }
        }.runTask(plugin);
    }

    private void calculateAndApplyKnockback(LivingEntity attacker, LivingEntity victim) {
        if(!knockbackEnabled) return;
        Location attackerLocation = attacker.getLocation();
        Location victimLocation = victim.getLocation();
        double deltaX = attackerLocation.getX() - victimLocation.getX();
        double deltaZ;

        // Ensure the knockback direction is valid and not too small
        for (deltaZ = attackerLocation.getZ() - victimLocation.getZ();
             deltaX * deltaX + deltaZ * deltaZ < 1.0E-4D;
             deltaZ = (Math.random() - Math.random()) * 0.01D) {
            deltaX = (Math.random() - Math.random()) * 0.01D;
        }

        double magnitude = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        Vector playerVelocity = victim.getVelocity();
        double xVel = (playerVelocity.getX() * frictionX) - (deltaX / magnitude * knockbackHorizontal);
        double yVel = (playerVelocity.getY() * frictionY) + knockbackVertical;
        double zVel = (playerVelocity.getZ() * frictionZ) - (deltaZ / magnitude * knockbackHorizontal);

        playerVelocity.setX(xVel);
        playerVelocity.setY(yVel);
        playerVelocity.setZ(zVel);

        Debug.log("Base velocity calculated: X: " + Debug.round(xVel,2) + " Y: " + Debug.round(yVel,2) + " Z: " + Debug.round(zVel,2), attacker);

        // Apply horizontal knockback limit if applicable
        if (knockbackLimitHorizontal > 0 && playerVelocity.length() > knockbackLimitHorizontal) {
            xVel = playerVelocity.getX() / playerVelocity.length() * knockbackLimitHorizontal;
            zVel = playerVelocity.getZ() / playerVelocity.length() * knockbackLimitHorizontal;
            playerVelocity.setX(xVel);
            playerVelocity.setZ(zVel);
            Debug.log("Applied horizontal knockback limit, new X: " + Debug.round(xVel, 2) + " Z: " + Debug.round(zVel, 2), attacker);
        }

        // Calculate bonus for sprinting or knockback enchantment levels
        EntityEquipment equipment = attacker.getEquipment();
        if (equipment != null) {
            ItemStack heldItem = equipment.getItemInMainHand().getType() == Material.AIR ?
                    equipment.getItemInOffHand() : equipment.getItemInMainHand();

            double bonusKnockback = heldItem.getEnchantmentLevel(Enchantment.KNOCKBACK);
            if (attacker instanceof Player && ((Player) attacker).isSprinting()) {
                bonusKnockback += sprintFactor;
                Debug.log("Added bonus of " + sprintFactor, attacker);
            }

            if (playerVelocity.getY() > knockbackVerticalLimit && knockbackVerticalLimit > 0) {
                yVel = knockbackVerticalLimit;
                playerVelocity.setY(yVel);
                Debug.log("Applied vertical knockback limit, new Y: " + Debug.round(yVel,2), attacker);
            }

            if (bonusKnockback > 0) {
                xVel = (-Math.sin(attacker.getLocation().getYaw() * Math.PI / 180.0F) * bonusKnockback * knockbackExtraHorizontal);
                yVel = knockbackExtraVertical;
                zVel = (Math.cos(attacker.getLocation().getYaw() * Math.PI / 180.0F) * bonusKnockback * knockbackExtraHorizontal);
                Debug.log("Added bonus vector X: " + Debug.round(xVel, 2) + " Y: " + Debug.round(yVel, 2) + " Z: " + Debug.round(zVel, 2), attacker);
                playerVelocity.add(new Vector(xVel,yVel,zVel));
            }
        }

        // Apply armor knockback resistance if applicable
        if (armorResistanceEnabled) {
            double resistance = 1 - Objects.requireNonNull(victim.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE)).getValue();
            playerVelocity.multiply(new Vector(resistance, 1, resistance));
            Debug.log("Applied armor resistance", attacker);
        }

        UUID victimId = victim.getUniqueId();
        Debug.log("Final velocity calculated!", attacker);
        Debug.log("fvX: " + playerVelocity.getX(), attacker);
        Debug.log("fvY: " + playerVelocity.getY(), attacker);
        Debug.log("fvZ: " + playerVelocity.getZ(), attacker);
        playerKnockbackMap.put(victimId, playerVelocity);
    }
}
