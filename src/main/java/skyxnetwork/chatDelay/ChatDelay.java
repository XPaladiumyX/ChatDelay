package skyxnetwork.chatDelay;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public final class ChatDelay extends JavaPlugin implements Listener {

    private final HashMap<UUID, Long> chatCooldowns = new HashMap<>();
    private String chatPrefix;

    @Override
    public void onEnable() {
        // Chargement ou création de la configuration
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        chatPrefix = ChatColor.translateAlternateColorCodes('&', config.getString("Prefix", "&dSky X &9Network &aCHAT-DELAY &8●⏺&7"));

        // Enregistrement de l'événement
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("ChatDelay enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ChatDelay disabled.");
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        // Vérification des permissions
        if (player.hasPermission("skyxnetwork.chat-delay")) {
            chatCooldowns.remove(playerId); // Pas de délai pour les administrateurs
            return;
        }

        // Calcul du délai
        double delay = 2.99; // Par défaut
        for (double i = 0.1; i <= 2.99; i += 0.1) {
            if (player.hasPermission("skyxnetwork.chat-delay." + i)) {
                delay = i;
                break;
            }
        }

        long cooldownTime = (long) (delay * 1000); // Conversion en millisecondes
        long lastChatTime = chatCooldowns.getOrDefault(playerId, 0L);

        if (currentTime - lastChatTime < cooldownTime) {
            event.setCancelled(true);
            player.sendMessage(net.kyori.adventure.text.Component.text(chatPrefix + " You need to wait " + delay + " seconds before sending a another message !")
                    .color(net.kyori.adventure.text.format.NamedTextColor.RED));

            return;
        }

        chatCooldowns.put(playerId, currentTime);
    }
}