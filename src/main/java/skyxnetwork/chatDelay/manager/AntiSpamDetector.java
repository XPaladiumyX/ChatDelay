package skyxnetwork.chatDelay.manager;

import org.bukkit.entity.Player;

import java.util.*;

public final class AntiSpamDetector {

    private final Map<UUID, Queue<String>> playerMessages;
    private final int maxRepeatedCount;
    private final int maxMessageHistory;

    public AntiSpamDetector() {
        this.playerMessages = new HashMap<>();
        this.maxRepeatedCount = 2;
        this.maxMessageHistory = 5;
    }

    public boolean detectSpam(Player player, String message) {
        String normalizedMessage = normalizeMessage(message);
        UUID playerId = player.getUniqueId();

        if (normalizedMessage.isEmpty()) {
            return false;
        }

        Queue<String> messages = playerMessages.computeIfAbsent(playerId, k -> new LinkedList<>());
        messages.add(normalizedMessage);

        while (messages.size() > maxMessageHistory) {
            messages.poll();
        }

        return checkForRepeatedPhrases(messages, normalizedMessage);
    }

    private boolean checkForRepeatedPhrases(Queue<String> messages, String currentMessage) {
        int repeatCount = 1;

        for (String msg : messages) {
            if (msg.equals(currentMessage)) {
                repeatCount++;
                if (repeatCount > maxRepeatedCount) {
                    return true;
                }
            }
        }

        return false;
    }

    private String normalizeMessage(String message) {
        return message.toLowerCase().trim();
    }

    public void clearPlayerHistory(Player player) {
        playerMessages.remove(player.getUniqueId());
    }

    public void clearAllHistory() {
        playerMessages.clear();
    }

    public int getMaxRepeatedCount() {
        return maxRepeatedCount;
    }
}