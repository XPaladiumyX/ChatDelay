package skyxnetwork.chatDelay;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public final class ChatDelay extends JavaPlugin implements Listener {

    private final HashMap<UUID, Long> chatCooldowns = new HashMap<>();
    private Component chatPrefix;
    private BukkitAudiences adventure;

    @Override
    public void onEnable() {
        // Initialiser Adventure
        adventure = BukkitAudiences.create(this);

        // Charger ou créer la configuration
        saveDefaultConfig();
        FileConfiguration config = getConfig();

        // Charger et convertir le préfixe avec les codes couleurs
        String prefix = config.getString("Prefix", "&dSky X &9Network &aCHAT-DELAY &8●⏺&7");
        chatPrefix = LegacyComponentSerializer.legacyAmpersand().deserialize(prefix);

        // Enregistrer l'événement
        getServer().getPluginManager().registerEvents(this, this);

        // Confirmer l'activation
        adventure.console().sendMessage(Component.text("ChatDelay enabled!").color(NamedTextColor.GREEN));
        getLogger().info("ChatDelay plugin is enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ChatDelay plugin is disabled.");
        if (adventure != null) {
            adventure.close();
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        // Vérifier si le joueur a la permission de bypass
        if (player.hasPermission("skyxnetwork.chat-delay")) {
            chatCooldowns.remove(playerId); // Pas de cooldown pour les administrateurs
            return;
        }

        // Déterminer le délai du joueur en fonction des permissions
        double delay = getChatDelay(player);
        long cooldownTime = (long) (delay * 1000); // Convertir en millisecondes

        // Vérifier le cooldown
        long lastChatTime = chatCooldowns.getOrDefault(playerId, 0L);
        if (currentTime - lastChatTime < cooldownTime) {
            // Temps restant avant le prochain message
            double timeLeft = (cooldownTime - (currentTime - lastChatTime)) / 1000.0;

            // Annuler l'événement et informer le joueur
            event.setCancelled(true);
            adventure.player(player).sendMessage(chatPrefix.append(
                    Component.text(" You need to wait " + String.format("%.1f", timeLeft) + " seconds before sending another message!")
                            .color(NamedTextColor.RED)
            ));
            return;
        }

        // Mettre à jour le temps du dernier message
        chatCooldowns.put(playerId, currentTime);
    }

    /**
     * Récupère le délai en fonction des permissions du joueur.
     *
     * @param player Le joueur.
     * @return Le délai (en secondes).
     */
    private double getChatDelay(Player player) {
        // Délai par défaut
        double defaultDelay = 3.0; // Augmenté à 3 secondes

        // Vérifier les permissions dynamiques
        for (double i = 0.1; i <= defaultDelay; i += 0.1) {
            String permission = "skyxnetwork.chat-delay." + String.format("%.1f", i);
            if (player.hasPermission(permission)) {
                return i;
            }
        }
        return defaultDelay; // Retourne la valeur par défaut si aucune permission n'est trouvée
    }
}