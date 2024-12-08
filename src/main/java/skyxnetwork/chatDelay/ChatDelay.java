package skyxnetwork.chatDelay;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public final class ChatDelay extends JavaPlugin implements Listener {

    private final HashMap<UUID, Long> chatCooldowns = new HashMap<>();
    private String chatPrefix;
    private BukkitAudiences adventure;

    @Override
    public void onEnable() {
        // Adventure audiences for sending messages
        adventure = BukkitAudiences.create(this);

        // Load or create the configuration
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        chatPrefix = config.getString("Prefix", "&dSky X &9Network &aCHAT-DELAY &8●⏺&7");

        // Translate color codes (& to actual color)
        Component translatedPrefix = Component.text(chatPrefix)
                .color(TextColor.color(0xDDDDDD)); // Default color, set a specific color if needed

        // Register the event
        getServer().getPluginManager().registerEvents(this, this);

        // Log prefix with colors
        adventure.console().sendMessage(translatedPrefix);

        getLogger().info("ChatDelay enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ChatDelay disabled.");
        if (adventure != null) {
            adventure.close();
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
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