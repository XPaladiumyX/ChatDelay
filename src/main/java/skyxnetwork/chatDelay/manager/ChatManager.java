package skyxnetwork.chatDelay.manager;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ChatManager {

    private final Map<UUID, Long> cooldowns;
    private final Map<UUID, Double> overrideDelays;
    private final PermissionHandler permissionHandler;

    public ChatManager(PermissionHandler permissionHandler) {
        this.cooldowns = new HashMap<>();
        this.overrideDelays = new HashMap<>();
        this.permissionHandler = permissionHandler;
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
        if (delay <= 0) {
            overrideDelays.remove(player.getUniqueId());
        } else {
            overrideDelays.put(player.getUniqueId(), delay);
        }
    }

    public boolean hasOverrideDelay(Player player) {
        return overrideDelays.containsKey(player.getUniqueId());
    }

    public void clearAllCooldowns() {
        cooldowns.clear();
    }

    public void clearOverrideDelay(Player player) {
        overrideDelays.remove(player.getUniqueId());
    }

    public Map<UUID, Double> getActiveOverrides() {
        return new HashMap<>(overrideDelays);
    }
}