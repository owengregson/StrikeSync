package me.vexmc.strikesync.handlers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import me.vexmc.strikesync.StrikeSyncPlugin;
import me.vexmc.strikesync.events.AsyncHitRegisterEvent;
import me.vexmc.strikesync.modules.KnockbackModule;
import me.vexmc.strikesync.utils.Debug;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.bukkit.Bukkit.getPluginManager;

public class StrikeSyncHandler extends PacketAdapter {
    private final Map<Player, Integer> cps = new HashMap<>();
    private final Queue<Runnable> hitQueue = new ConcurrentLinkedQueue<>();
    private final KnockbackModule knockbackModule;
    private final Map<UUID, Long> lastPacketTime = new HashMap<>();
    private static byte MAX_CPS;

    public StrikeSyncHandler(StrikeSyncPlugin pl, int maxCps) {
        super(pl, ListenerPriority.LOWEST, Collections.singletonList(PacketType.Play.Client.USE_ENTITY));
        MAX_CPS = (byte) maxCps;
        knockbackModule = pl.getKnockbackModule();
    }

    private final BukkitTask hitQueueProcessor = new BukkitRunnable() {
        @Override
        public void run() {
            while (!hitQueue.isEmpty()) {
                Runnable damageTask = hitQueue.remove();
                damageTask.run();
            }
        }
    }.runTaskTimer(StrikeSyncPlugin.getInstance(), 1, 1);

    private final BukkitTask cpsResetter = new BukkitRunnable() {
        @Override
        public void run() {
            cps.clear();
        }
    }.runTaskTimer(StrikeSyncPlugin.getInstance(), 20, 20);

    @Override
    public void onPacketReceiving(PacketEvent e) {
        Debug.log("Received a packet: " + e.getPacketType().name(), e.getPlayer());
        PacketContainer packet = e.getPacket();
        Player attacker = e.getPlayer();
        // TODO: Rewrite this entire method (it doesn't work)
        // TODO: Rewrite this entire method (it doesn't work)
        // TODO: Rewrite this entire method (it doesn't work)
        // TODO: Rewrite this entire method (it doesn't work)
        // TODO: Rewrite this entire method (it doesn't work)

        // Issues with current implementation:
        // - Unable to tell the difference between right and left clicks on entity,
        // so I made a shitty workaround that detects if you send two packets quickly
        // because that seemed to happen during right-clicks but not left-clicks.
        // Obviously, this A. doesn't work and B. is a terrible solution.

        // - The attacking event sometimes doesn't get cancelled, so my plugin can't
        // even handle the attack event properly.

        // - Everything here is shit

        // Potential solutions to apply damage (not in any particular order):

        // 1. NMS - Use NMS to attack other player (already attempted, but I am really
        // inexperienced with NMS, so I was unable to do it.) You can use the NMSUtils
        // class I already created during my attempt if you want to build on this idea
        // and go forward with this solution.

        // 2. ProtocolLib - Use ProtocolLib to correctly trigger an EntityDamageByEntityEvent
        // This is the solution I tried to implement below, but it's buggy and doesn't work.

        // 3. Another approach - I'm not sure what other approach to take, but I'm sure
        // there are other ways to do this. I just don't know what they are.

        if (e.getPacketType() != PacketType.Play.Client.USE_ENTITY) {
            Debug.log("Packet type is not USE_ENTITY", attacker);
            return;
        }

        try {
            // Ensure that the packet has integers available
            if (packet.getIntegers().size() == 0) {
                Debug.log("No integers present in the packet", attacker);
                return;
            }

            // Retrieve the entity ID from the packet
            int entityId = packet.getIntegers().readSafely(0);
            Debug.log("Entity ID: " + entityId, attacker);

            // Check if action type and differentiate between attack and right-click
            boolean isAttack = false;
            if (packet.getEntityUseActions().size() > 0) {
                EnumWrappers.EntityUseAction action = packet.getEntityUseActions().readSafely(0);
                Debug.log("Action: " + action + ", Entity ID: " + entityId, attacker);

                if (action == EntityUseAction.ATTACK) {
                    isAttack = true;
                }
            }

            // Additional check using packet count within a short timeframe
            long currentTime = System.currentTimeMillis();
            long lastTime = lastPacketTime.getOrDefault(attacker.getUniqueId(), 0L);
            boolean isRightClick = (currentTime - lastTime) < 50; // Threshold for double packet detection

            lastPacketTime.put(attacker.getUniqueId(), currentTime);

            if (isRightClick) {
                Debug.log("Detected a right-click action", attacker);
                lastPacketTime.remove(attacker.getUniqueId());
                return;
            }

            /*if (!isAttack) {
                Debug.log("Action is not an attack", attacker);
                return;
            }*/

            // Retrieve the entity using the entity ID
            Entity entity = attacker.getWorld().getEntities().stream()
                    .filter(fe -> fe.getEntityId() == entityId)
                    .findFirst()
                    .orElse(null);

            if (entity == null) {
                Debug.log("Entity with ID " + entityId + " could not be found", attacker);
                return;
            }

            Debug.log("Entity retrieved: " + entity, attacker);

            // Check if target is valid
            if (!(entity instanceof Damageable)) {
                Debug.log("Target is not a Damageable entity", attacker);
                return;
            }
            Damageable target = (Damageable) entity;
            if (target.isDead()) {
                Debug.log("Target is dead", attacker);
                return;
            }

            Debug.log("Target is valid and not dead", attacker);

            // Check world consistency and PvP status
            World world = attacker.getWorld();
            if (world != target.getWorld()) {
                Debug.log("Attacker and target are not in the same world", attacker);
                return;
            }
            if (!world.getPVP()) {
                Debug.log("PvP is not enabled in this world", attacker);
                return;
            }

            Debug.log("World is consistent and PvP is enabled", attacker);

            // Check if target is a player and their game mode
            if (target instanceof Player) {
                Player targetPlayer = (Player) target;
                if (targetPlayer.getGameMode() == GameMode.CREATIVE) {
                    Debug.log("Target player is in creative mode", attacker);
                    return;
                }
            }

            Debug.log("Target is not in creative mode or not a player", attacker);
            Debug.log("Valid attack packet received", attacker);
            e.setCancelled(true);

            int attackerCps = cps.getOrDefault(attacker, 0);
            cps.put(attacker, attackerCps + 1);

            if (attackerCps <= MAX_CPS) {
                hitQueue.add(() -> {
                    Debug.log("Processing hit in queue", attacker);
                    if (!target.isDead() && attacker.isOnline()) {
                        // Fire the custom event
                        AsyncHitRegisterEvent damageEvent = new AsyncHitRegisterEvent(attacker, target, 0);
                        getPluginManager().callEvent(damageEvent);

                        if (!damageEvent.isCancelled()) {
                            // Force server to register the hit
                            knockbackModule.forceAttack(attacker, target);
                            Debug.log("Async hit registered from " + attacker.getName() + " on " + target.getName(), attacker);
                        }
                    }
                });
            }
        } catch (Exception ex) {
            Debug.log("An error occurred while processing the packet", attacker);
            ex.printStackTrace();
        }
    }

    public void stop() {
        cpsResetter.cancel();
        hitQueueProcessor.cancel();
    }
}