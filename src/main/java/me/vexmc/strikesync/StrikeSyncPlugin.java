package me.vexmc.strikesync;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.async.AsyncListenerHandler;
import me.vexmc.strikesync.commands.StrikeSyncCommand;
import me.vexmc.strikesync.handlers.StrikeSyncHandler;
import me.vexmc.strikesync.modules.KnockbackModule;
import me.vexmc.strikesync.utils.Debug;
import me.vexmc.strikesync.utils.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public final class StrikeSyncPlugin extends JavaPlugin {
    private boolean listening = false;
    private final static int pluginID = 22740;
    private static StrikeSyncPlugin instance;
    private static Metrics metrics;
    private StrikeSyncHandler hitListener;
    private AsyncListenerHandler hitListenerHandler;
    private ProtocolManager protocolManager;
    private KnockbackModule knockbackModule;

    @Override
    public void onEnable() {
        instance = this;
        protocolManager = ProtocolLibrary.getProtocolManager();
        Debug.initialize(this);
        Objects.requireNonNull(getCommand("strikesync")).setExecutor(new StrikeSyncCommand(this));
        Objects.requireNonNull(getCommand("strikesync")).setTabCompleter(new StrikeSyncCommand(this));
        registerListeners();
        knockbackModule = new KnockbackModule(this);
        getServer().getPluginManager().registerEvents(knockbackModule, this);
        reload();
        metrics = new Metrics(this, pluginID);
        getLogger().info("======================SS======================\nStrikeSync 1.0 has been enabled successfully!\n======================SS======================");
    }

    @Override
    public void onDisable() {
        if (hitListener != null) {
            unregisterHitListener();
        }
        protocolManager = null;
        instance = null;
        metrics.shutdown();
        getLogger().info("======================SS======================\nStrikeSync 1.0 has been disabled successfully!\n======================SS======================");
    }

    public void registerListeners() {
        knockbackModule = new KnockbackModule(this);
        getServer().getPluginManager().registerEvents(knockbackModule, this);
    }

    public void registerHitListener() {
        if (!getConfig().getBoolean("async-hitreg.enabled", true)) {
            return;
        }

        if (hitListener == null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    int maxCps = getConfig().getInt("async-hitreg.max-cps", 20);
                    hitListener = new StrikeSyncHandler(instance, maxCps);
                    hitListenerHandler = protocolManager.getAsynchronousManager().registerAsyncHandler(hitListener);
                    hitListenerHandler.start();
                }
            }.runTaskAsynchronously(this);
            listening = true;
        }
    }

    public void unregisterHitListener() {
        if (hitListener != null) {
            protocolManager.getAsynchronousManager().unregisterAsyncHandler(hitListenerHandler);
            listening = false;
            hitListener = null;
        }
    }

    public KnockbackModule getKnockbackModule() {
        return knockbackModule;
    }

    public void reload() {
        saveDefaultConfig();
        reloadConfig();

        if (hitListener != null) {
            hitListener.stop();
        }
        unregisterHitListener();
        registerHitListener();

        if (knockbackModule != null) {
            knockbackModule.reload();
        }
    }

    public static StrikeSyncPlugin getInstance() {
        return instance;
    }

    public boolean isListening() {
        return listening;
    }
}
