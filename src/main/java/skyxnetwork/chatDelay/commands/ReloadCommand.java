package skyxnetwork.chatDelay.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import skyxnetwork.chatDelay.ChatDelay;
import skyxnetwork.chatDelay.util.MessageFactory;

public final class ReloadCommand implements CommandExecutor {

    private final ChatDelay plugin;

    public ReloadCommand(ChatDelay plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("skyxnetwork.chatdelay.admin.reload")) {
            sender.sendMessage(MessageFactory.noPermissionMessage());
            return true;
        }

        plugin.reloadPlugin();
        sender.sendMessage(MessageFactory.success("Configuration reloaded successfully!"));
        return true;
    }
}