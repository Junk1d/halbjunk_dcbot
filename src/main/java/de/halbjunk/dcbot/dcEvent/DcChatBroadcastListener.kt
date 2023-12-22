package de.halbjunk.dcbot.dcEvent;

import de.halbjunk.dcbot.Main;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.ChatColor;

public class DcChatBroadcastListener extends ListenerAdapter {
    public void onMessageReceived (MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!event.isFromGuild()) return;
        if (!event.getChannel().getId().equals("1107772229544067263")) return;
        String prefix = ChatColor.DARK_AQUA + "[Discord]" + ChatColor.RESET;
        Main.getPlugin().getServer().broadcastMessage(prefix + " " + event.getAuthor().getName() +  ": " + event.getMessage().getContentStripped());

    }
}
