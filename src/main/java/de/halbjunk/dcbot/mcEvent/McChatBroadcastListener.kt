package de.halbjunk.dcbot.mcEvent;

import de.halbjunk.dcbot.Main;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class McChatBroadcastListener implements Listener {
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        System.out.println("Chat Nachricht");
        String message = event.getMessage();
        String playerName = event.getPlayer().getName();

        Main.bot.getTextChannelById("1113557918403526688").sendMessage(playerName +": "+ message).queue();

    }
}
