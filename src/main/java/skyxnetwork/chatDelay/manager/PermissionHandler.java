package skyxnetwork.chatDelay.manager;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.TreeMap;

public final class PermissionHandler {

    private final JavaPlugin plugin;
    private final Map<Integer, String> rankOrder;
    private double defaultDelay;

    private static final String BYPASS_PERMISSION = "skyxnetwork.chat-delay.bypass";
    private static final String BASE_PERMISSION = "skyxnetwork.chat-delay.";

    private static final Map<Integer, String> DEFAULT_RANK_ORDER = Map.ofEntries(
        Map.entry(1, "default"),
        Map.entry(2, "member"),
        Map.entry(3, "advanced"),
        Map.entry(4, "legend"),
        Map.entry(5, "ultra"),
        Map.entry(6, "ultime"),
        Map.entry(7, "ultimeii"),
        Map.entry(8, "ultimeiii"),
        Map.entry(9, "god"),
        Map.entry(10, "builder"),
        Map.entry(11, "helper"),
        Map.entry(12, "mod"),
        Map.entry(13, "owner"),
        Map.entry(14, "media"),
        Map.entry(15, "linked"),
        Map.entry(16, "vip"),
        Map.entry(17, "vip+"),
        Map.entry(18, "mvp"),
        Map.entry(19, "mvp+")
    );

    public PermissionHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        this.rankOrder = new TreeMap<>(DEFAULT_RANK_ORDER);
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

    public double getDelayForPlayer(Player player) {
        if (hasBypassPermission(player)) {
            return 0;
        }

        double luckPermsDelay = getDelayFromLuckPerms(player);
        if (luckPermsDelay >= 0) {
            return luckPermsDelay;
        }

        double configuredDelay = getDelayFromConfig(player);
        if (configuredDelay >= 0) {
            return configuredDelay;
        }

        return getDelayFromPermissions(player);
    }

    private double getDelayFromLuckPerms(Player player) {
        LuckPerms luckPerms = plugin.getServer().getServicesManager().load(LuckPerms.class);
        if (luckPerms == null) {
            return -1;
        }

        try {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user == null) {
                return -1;
            }

            String primaryGroup = user.getPrimaryGroup();
            if (primaryGroup == null) {
                return -1;
            }

            String path = "ranks." + primaryGroup.toLowerCase() + ".delay";
            if (plugin.getConfig().contains(path)) {
                return plugin.getConfig().getDouble(path);
            }

            for (Map.Entry<Integer, String> entry : rankOrder.entrySet()) {
                if (entry.getValue().equalsIgnoreCase(primaryGroup)) {
                    String configPath = "ranks." + entry.getValue() + ".delay";
                    if (plugin.getConfig().contains(configPath)) {
                        return plugin.getConfig().getDouble(configPath);
                    }
                }
            }

        } catch (Exception e) {
            plugin.getLogger().warning("Error checking LuckPerms: " + e.getMessage());
        }

        return -1;
    }

    private double getDelayFromConfig(Player player) {
        for (Map.Entry<Integer, String> entry : rankOrder.entrySet()) {
            String group = entry.getValue();
            if (player.hasPermission("group." + group) || player.hasPermission("chatdelay.group." + group)) {
                String path = "ranks." + group + ".delay";
                if (plugin.getConfig().contains(path)) {
                    return plugin.getConfig().getDouble(path);
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

    public Map<Integer, String> getRankOrder() {
        return new TreeMap<>(rankOrder);
    }
}