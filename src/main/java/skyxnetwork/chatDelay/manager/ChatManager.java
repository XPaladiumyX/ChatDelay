package skyxnetwork.chatDelay.manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ChatManager {

    private final JavaPlugin plugin;
    private final Map<UUID, Long> cooldowns;
    private final Map<UUID, Double> overrideDelays;
    private final PermissionHandler permissionHandler;
    private final File overrideFile;

    public ChatManager(JavaPlugin plugin, PermissionHandler permissionHandler) {
        this.plugin = plugin;
        this.cooldowns = new HashMap<>();
        this.overrideDelays = new HashMap<>();
        this.permissionHandler = permissionHandler;
        this.overrideFile = new File(plugin.getDataFolder(), "overrides.yml");
        loadOverrides();
    }

    public boolean isOnCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!cooldowns.containsKey(playerId)) {
            return false;
        }

        long lastChatTime = cooldowns.get(playerId);
        double delay = getEffectiveDelay(player);
        long cooldownTime = (long) (delay * 1000);

        return System.currentTimeMillis() - lastChatTime < cooldownTime;
    }

    public double getRemainingCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!cooldowns.containsKey(playerId)) {
            return 0;
        }

        long lastChatTime = cooldowns.get(playerId);
        double delay = getEffectiveDelay(player);
        long cooldownTime = (long) (delay * 1000);
        long elapsed = System.currentTimeMillis() - lastChatTime;

        if (elapsed >= cooldownTime) {
            return 0;
        }

        return (cooldownTime - elapsed) / 1000.0;
    }

    public void updateCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void clearCooldown(Player player) {
        cooldowns.remove(player.getUniqueId());
    }

    public double getEffectiveDelay(Player player) {
        UUID playerId = player.getUniqueId();
        if (overrideDelays.containsKey(playerId)) {
            return overrideDelays.get(playerId);
        }
        return permissionHandler.getDelayForPlayer(player);
    }

    public void setOverrideDelay(Player player, double delay) {
        UUID playerId = player.getUniqueId();
        if (delay <= 0) {
            overrideDelays.remove(playerId);
        } else {
            overrideDelays.put(playerId, delay);
        }
        saveOverrides();
    }

    public boolean hasOverrideDelay(Player player) {
        return overrideDelays.containsKey(player.getUniqueId());
    }

    public void clearAllCooldowns() {
        cooldowns.clear();
    }

    public void clearOverrideDelay(Player player) {
        overrideDelays.remove(player.getUniqueId());
        saveOverrides();
    }

    public Map<UUID, Double> getActiveOverrides() {
        return new HashMap<>(overrideDelays);
    }

    private void loadOverrides() {
        if (!overrideFile.exists()) {
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(overrideFile);
        if (config.contains("overrides")) {
            for (String key : config.getConfigurationSection("overrides").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    double delay = config.getDouble("overrides." + key);
                    overrideDelays.put(uuid, delay);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    private void saveOverrides() {
        if (!overrideFile.exists()) {
            overrideFile.getParentFile().mkdirs();
        }

        FileConfiguration config = new YamlConfiguration();
        for (Map.Entry<UUID, Double> entry : overrideDelays.entrySet()) {
            config.set("overrides." + entry.getKey().toString(), entry.getValue());
        }

        try {
            config.save(overrideFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save overrides: " + e.getMessage());
        }
    }

    public void reloadOverrides() {
        overrideDelays.clear();
        loadOverrides();
    }
}