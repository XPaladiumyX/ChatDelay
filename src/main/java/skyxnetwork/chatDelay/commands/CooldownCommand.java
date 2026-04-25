package skyxnetwork.chatDelay.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import skyxnetwork.chatDelay.manager.ChatManager;
import skyxnetwork.chatDelay.util.MessageFactory;

import java.util.ArrayList;
import java.util.List;

public final class CooldownCommand implements CommandExecutor, TabCompleter {

    private final ChatManager chatManager;

    public CooldownCommand(ChatManager chatManager) {
        this.chatManager = chatManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("skyxnetwork.chatdelay.admin.setcooldown")) {
            sender.sendMessage(MessageFactory.noPermissionMessage());
            return true;
        }

        if (args.length < 1) {
            sendUsage(sender, label);
            return true;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "set" -> handleSet(sender, args);
            case "reset" -> handleReset(sender, args);
            case "check" -> handleCheck(sender, args);
            case "list" -> handleList(sender);
            default -> sendUsage(sender, label);
        }

        return true;
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(MessageFactory.info("Usage: /cooldown set <player> <delay>"));
            return;
        }

        String playerName = args[1];
        Player target = Bukkit.getPlayer(playerName);

        if (target == null) {
            sender.sendMessage(MessageFactory.playerNotFoundMessage(playerName));
            return;
        }

        double delay;
        try {
            delay = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(MessageFactory.invalidDelayMessage());
            return;
        }

        if (delay < 0) {
            sender.sendMessage(MessageFactory.invalidDelayMessage());
            return;
        }

        chatManager.setOverrideDelay(target, delay);
        sender.sendMessage(MessageFactory.cooldownSetMessage(target.getName(), delay));

        if (target != sender) {
            target.sendMessage(MessageFactory.info("An administrator has set your chat delay to " + delay + " seconds."));
        }
    }

    private void handleReset(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(MessageFactory.info("Usage: /cooldown reset <player>"));
            return;
        }

        String playerName = args[1];
        Player target = Bukkit.getPlayer(playerName);

        if (target == null) {
            sender.sendMessage(MessageFactory.playerNotFoundMessage(playerName));
            return;
        }

        chatManager.clearOverrideDelay(target);
        chatManager.clearCooldown(target);
        sender.sendMessage(MessageFactory.cooldownResetMessage(target.getName()));

        if (target != sender) {
            target.sendMessage(MessageFactory.info("Your chat delay has been reset to the default by an administrator."));
        }
    }

    private void handleCheck(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(MessageFactory.info("Usage: /cooldown check <player>"));
            return;
        }

        String playerName = args[1];
        Player target = Bukkit.getPlayer(playerName);

        if (target == null) {
            sender.sendMessage(MessageFactory.playerNotFoundMessage(playerName));
            return;
        }

        double effectiveDelay = chatManager.getEffectiveDelay(target);
        double remaining = chatManager.getRemainingCooldown(target);
        boolean hasOverride = chatManager.hasOverrideDelay(target);

        Component message = MessageFactory.getPrefix().append(
            Component.text(" Information for " + target.getName() + ": ").color(net.kyori.adventure.text.format.NamedTextColor.GRAY)
        );

        sender.sendMessage(message);
        sender.sendMessage(Component.text("  Effective delay: " + effectiveDelay + "s").color(net.kyori.adventure.text.format.NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("  Override active: " + hasOverride).color(net.kyori.adventure.text.format.NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("  Time remaining: " + String.format("%.1f", remaining) + "s").color(net.kyori.adventure.text.format.NamedTextColor.YELLOW));
    }

    private void handleList(CommandSender sender) {
        var overrides = chatManager.getActiveOverrides();

        sender.sendMessage(MessageFactory.info("Active delay overrides:"));

        if (overrides.isEmpty()) {
            sender.sendMessage(Component.text("  No active overrides.").color(net.kyori.adventure.text.format.NamedTextColor.GRAY));
            return;
        }

        for (var entry : overrides.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            String name = player != null ? player.getName() : entry.getKey().toString();
            sender.sendMessage(Component.text("  " + name + ": " + entry.getValue() + "s").color(net.kyori.adventure.text.format.NamedTextColor.YELLOW));
        }
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(MessageFactory.info("Cooldown Commands:"));
        sender.sendMessage(Component.text("  /" + label + " set <player> <delay> - Set a player's chat delay").color(net.kyori.adventure.text.format.NamedTextColor.GRAY));
        sender.sendMessage(Component.text("  /" + label + " reset <player> - Reset a player's chat delay").color(net.kyori.adventure.text.format.NamedTextColor.GRAY));
        sender.sendMessage(Component.text("  /" + label + " check <player> - Check a player's delay status").color(net.kyori.adventure.text.format.NamedTextColor.GRAY));
        sender.sendMessage(Component.text("  /" + label + " list - List active overrides").color(net.kyori.adventure.text.format.NamedTextColor.GRAY));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("set");
            completions.add("reset");
            completions.add("check");
            completions.add("list");
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("check"))) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            completions.add("0");
            completions.add("1");
            completions.add("2");
            completions.add("3");
            completions.add("5");
            completions.add("10");
        }

        String current = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(current));

        return completions;
    }
}