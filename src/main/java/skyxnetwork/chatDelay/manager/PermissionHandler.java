package skyxnetwork.chatDelay.manager;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.TreeMap;

public final class PermissionHandler {

    private final JavaPlugin plugin;
    private final Map<Integer, String> rankDelays;
    private double defaultDelay;

    private static final String BYPASS_PERMISSION = "skyxnetwork.chat-delay.bypass";
    private static final String BASE_PERMISSION = "skyxnetwork.chat-delay.";
    private static final String ADMIN_OVERRIDE_PERMISSION = "skyxnetwork.chat-delay.admin.override";

    public PermissionHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        this.rankDelays = new TreeMap<>(Map.of(
            1, "vip",
            2, "member",
            3, "default"
        ));
        loadConfiguration();
    }

    private void loadConfiguration() {
        defaultDelay = plugin.getConfig().getDouble("default-delay", 3.0);
    }

    public void reload() {
        plugin.reloadConfig();
        loadConfiguration();
    }

    public boolean hasBypassPermission(Player player) {
        return player.hasPermission(BYPASS_PERMISSION);
    }

    public boolean hasAdminOverridePermission(Player player) {
        return player.hasPermission(ADMIN_OVERRIDE_PERMISSION);
    }

    public double getDelayForPlayer(Player player) {
        if (hasBypassPermission(player)) {
            return 0;
        }

        double configuredDelay = getDelayFromConfig(player);
        if (configuredDelay >= 0) {
            return configuredDelay;
        }

        return getDelayFromPermissions(player);
    }

    private double getDelayFromConfig(Player player) {
        for (Map.Entry<Integer, String> entry : rankDelays.entrySet()) {
            String group = entry.getValue();
            String path = "ranks." + group + ".delay";
            if (plugin.getConfig().contains(path)) {
                double delay = plugin.getConfig().getDouble(path);
                if (player.hasPermission("group." + group) || player.hasPermission("chatdelay.group." + group)) {
                    return delay;
                }
            }
        }
        return -1;
    }

    private double getDelayFromPermissions(Player player) {
        for (double i = 0.1; i <= 10.0; i += 0.1) {
            String permission = BASE_PERMISSION + String.format("%.1f", i);
            if (player.hasPermission(permission)) {
                return i;
            }
        }

        if (player.hasPermission(BASE_PERMISSION + "custom")) {
            double customDelay = plugin.getConfig().getDouble("custom-permission-delay", -1);
            if (customDelay > 0) {
                return customDelay;
            }
        }

        return defaultDelay;
    }

    public double getDefaultDelay() {
        return defaultDelay;
    }
}