package de.halbjunk.dcbot.dcEvent;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MSG extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("message")) {
            event.deferReply().queue(); // Bestätigung der Slash-Antwort

            String playerName = event.getOption("spielername").getAsString();
            String message = event.getOption("nachricht").getAsString();

            Player player = Bukkit.getPlayerExact(playerName);
            if (player == null || !player.isOnline()) {
                event.getHook().sendMessage("Spieler nicht gefunden oder offline.").queue();
                return;
            }

            player.sendMessage("[Discord] Geheime Nachricht: " + message);
            event.getHook().sendMessage("Nachricht erfolgreich an " + playerName + " gesendet.").queue();
        }
    }
}
