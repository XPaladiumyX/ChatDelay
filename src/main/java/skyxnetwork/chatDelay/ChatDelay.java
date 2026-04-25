package skyxnetwork.chatDelay;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import skyxnetwork.chatDelay.commands.CooldownCommand;
import skyxnetwork.chatDelay.commands.ReloadCommand;
import skyxnetwork.chatDelay.manager.AntiSpamDetector;
import skyxnetwork.chatDelay.manager.ChatManager;
import skyxnetwork.chatDelay.manager.PermissionHandler;
import skyxnetwork.chatDelay.util.MessageFactory;

public final class ChatDelay extends JavaPlugin implements Listener {

    private static final String ANSI_MAGENTA = "\u001B[35m";
    private static final String ANSI_LIGHT_GRAY = "\u001B[37m";
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_LIGHT_GREEN = "\u001B[92m";
    private static final String ANSI_RED = "\u001B[31m";

    private BukkitAudiences adventure;
    private PermissionHandler permissionHandler;
    private ChatManager chatManager;
    private AntiSpamDetector antiSpamDetector;

    @Override
    public void onEnable() {
        printBanner(true);

        saveDefaultConfig();

        MessageFactory.initialize(this);

        adventure = BukkitAudiences.create(this);

        permissionHandler = new PermissionHandler(this);
        chatManager = new ChatManager(this, permissionHandler);
        antiSpamDetector = new AntiSpamDetector();

        registerCommands();
        registerEvents();

        Bukkit.getLogger().info(ANSI_LIGHT_GREEN + "ChatDelay plugin enabled successfully!");
        Bukkit.getLogger().info(ANSI_LIGHT_GRAY + "Running on Paper API for Minecraft 1.21.4+");
    }

    @Override
    public void onDisable() {
        printBanner(false);

        if (adventure != null) {
            adventure.close();
        }
    }

    private void registerCommands() {
        CooldownCommand cooldownCommand = new CooldownCommand(chatManager);
        getCommand("cooldown").setExecutor(cooldownCommand);
        getCommand("cooldown").setTabCompleter(cooldownCommand);

        ReloadCommand reloadCommand = new ReloadCommand(this);
        getCommand("chatdelayreload").setExecutor(reloadCommand);
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        if (permissionHandler.hasBypassPermission(player)) {
            chatManager.clearCooldown(player);
            return;
        }

        String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        if (chatManager.isOnCooldown(player)) {
            event.setCancelled(true);
            double timeLeft = chatManager.getRemainingCooldown(player);
            adventure.player(player).sendMessage(MessageFactory.cooldownMessage(timeLeft));
            return;
        }

        if (antiSpamDetector.detectSpam(player, message)) {
            event.setCancelled(true);
            adventure.player(player).sendMessage(MessageFactory.spamDetectedMessage());
            return;
        }

        chatManager.updateCooldown(player);
    }

    public void reloadPlugin() {
        reloadConfig();
        MessageFactory.initialize(this);
        permissionHandler.reload();
        chatManager.reloadOverrides();
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public PermissionHandler getPermissionHandler() {
        return permissionHandler;
    }

    public AntiSpamDetector getAntiSpamDetector() {
        return antiSpamDetector;
    }

    private void printBanner(boolean enabled) {
        String color = enabled ? ANSI_LIGHT_GREEN : ANSI_RED;
        String status = enabled ? "enabled" : "disabled";

        Bukkit.getLogger().info(ANSI_LIGHT_GRAY + "╔════════════════════════════════════════════════════════════════════════╗");
        Bukkit.getLogger().info(ANSI_MAGENTA + "║  _______  ___   _  __   __    __   __    __    _  _______  _______  ║");
        Bukkit.getLogger().info(ANSI_MAGENTA + "║ |       ||   | | ||  | |  |  |  |_|  |  |  |  | ||       ||       | ║");
        Bukkit.getLogger().info(ANSI_MAGENTA + "║ |  _____||   |_| ||  |_|  |  |       |  |   |_| ||    ___||_     _| ║");
        Bukkit.getLogger().info(ANSI_MAGENTA + "║ | |_____ |      _||       |  |       |  |       ||   |___   |   |   ║");
        Bukkit.getLogger().info(ANSI_MAGENTA + "║ |_____  ||     |_ |_     _|   |     |   |  _    ||    ___|  |   |   ║");
        Bukkit.getLogger().info(ANSI_MAGENTA + "║  _____| ||    _  |  |   |    |   _   |  | | |   ||   |___   |   |   ║");
        Bukkit.getLogger().info(ANSI_MAGENTA + "║ |_______||___| |_|  |___|    |__| |__|  |_|  |__||_______|  |___|   ║");
        Bukkit.getLogger().info(ANSI_LIGHT_GRAY + "╚════════════════════════════════════════════════════════════════════════╝");
        Bukkit.getLogger().info("  ");
        Bukkit.getLogger().info(color + "  ChatDelay plugin " + status + "!");
        Bukkit.getLogger().info(ANSI_LIGHT_GRAY + "╚════════════════════════════════════════════════════════════════════════╝");
    }
}