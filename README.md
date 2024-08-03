# StrikeSync
Asynchronous hit registration and knockback for modern Minecraft.

Note that this this plugin is experimental and any builds here serve as a beta release. Please report bugs if you encounter them!

## The Problem with Normal Hit Registration
Minecraft registers and processes hits in the main thread, ticking at a rate of 20 ticks-per-second (TPS), or 50ms per tick.
This means that hits may be unnecessarily delayed by up to 50 milliseconds! This is especially noticeable during combat where timing is critical to the outcome of the fight.
Additionally, Minecraft includes a (relatively low) max clicks-per-second (CPS) limit built-in to the server.
This plugin removes the limit so that hits may be processed on-demand at any time. That doesn't mean that players will experience unusual hit behavior, it will merely feel more responsive in PvP.

## How does StrikeSync solve this?
StrikeSync asynchronously listens for hit requests and processes them independently of the tick stack!
Actual damage, knockback, etc. MUST still be synchronized to the Minecraft server and applied on the next tick in the tick stack, though, as damaging players isn't thread-safe.
However, both the animation and hit registration are carried out by StrikeSync.

## How does the tick stack work, and how does it relate to this plugin?
The Minecraft server has a "stack" of operations to carry out, called the "tick stack."
When new operations need to be performed, such as spawning mobs or damaging a player, they are added to the tick stack.
The tick stack is processed 20 times every second, or 20 TPS. This means if you were to hit someone at the beginning of a tick, the server might not actually register anything until 50ms later.
This is especially noticeable during combat where timing is critical to the outcome of the fight. A few big PvP Practice servers implemented a similar version of this system hardcoded into their server jar.

## Asynchronous Events
Since async listener occurs outside of the main thread, we provide a separate event for cancelling StrikeSync's hit-registration.
To do this, you can use and hook into the `AsyncHitRegisterEvent`. Remember, though, that your listener is invoked asynchronously,
therefore you may NOT safely write to the Bukkit API or perform other actions which must occur on the main thread. See Bukkit documentation
for more information on this.

Here is an example code snippet to cancel hits in a protected WorldGuard region:

```java
class WorldGuardListener implements Listener {
    WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");

    @EventHandler
    public void AsyncHitRegisterEvent(AsyncHitRegisterEvent e) {
        Player damager = e.getDamager();
        Damageable entity = e.getEntity();
        World world = damager.getWorld();
        RegionManager rgMgr = wg.getRegionManager(world);

        boolean damagerCanDamage = rgMgr.getApplicableRegions(damager.getLocation()).testState( wg.wrapPlayer(damager), DefaultFlag.PVP)
        boolean entityIsDamageable = rgMgr.getApplicableRegions(entity.getLocation()).testState( wg.wrapPlayer(damager), DefaultFlag.PVP)
        if(!damagerCanDamage || !entityIsDamageable) {
            e.setCancelled(true);
        }
    }

}
```

## Notes
Back in 2016, another GitHub user by the name of frash23 attempted to implement a similar asynchronous hit processing technique in his plugin SmashHit.
Unfortunately, his plugin did not work exactly as described and only supports ancient, unsupported versions of Minecraft from pre-1.10.
Hence, I have recoded the system for modern versions and worked to ensure it works as intended. I still want to thank frash23 for inspiring me to make this project!
