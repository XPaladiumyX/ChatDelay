package skyxnetwork.chatDelay.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class MessageFactory {

    private static Component prefix;
    private static JavaPlugin plugin;

    private MessageFactory() {
    }

    public static void initialize(JavaPlugin plugin) {
        MessageFactory.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        String prefixStr = config.getString("Prefix", "&dSky X &9Network &aCHAT-DELAY &8>>> &7");
        prefix = LegacyComponentSerializer.legacyAmpersand().deserialize(prefixStr);
    }

    public static Component getPrefix() {
        return prefix;
    }

    public static Component colorize(String message) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }

    public static Component error(String message) {
        return prefix.append(Component.text(" " + message).color(NamedTextColor.RED));
    }

    public static Component success(String message) {
        return prefix.append(Component.text(" " + message).color(NamedTextColor.GREEN));
    }

    public static Component info(String message) {
        return prefix.append(Component.text(" " + message).color(NamedTextColor.YELLOW));
    }

    public static Component cooldownMessage(double timeLeft) {
        return error("You need to wait " + String.format("%.1f", timeLeft) + " seconds before sending another message!");
    }

    public static Component spamDetectedMessage() {
        return error("Spam detected! Repeating the same message is not allowed.");
    }

    public static Component cooldownSetMessage(String playerName, double delay) {
        return success("Chat delay for " + playerName + " has been set to " + delay + " seconds.");
    }

    public static Component cooldownResetMessage(String playerName) {
        return success("Chat delay for " + playerName + " has been reset to default.");
    }

    public static Component noPermissionMessage() {
        return error("You don't have permission to use this command.");
    }

    public static Component playerNotFoundMessage(String playerName) {
        return error("Player '" + playerName + "' not found.");
    }

    public static Component invalidDelayMessage() {
        return error("Invalid delay value. Please use a positive number.");
    }
}