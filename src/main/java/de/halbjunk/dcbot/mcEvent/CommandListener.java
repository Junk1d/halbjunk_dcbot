package de.halbjunk.dcbot.mcEvent;

import de.halbjunk.dcbot.Main;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandListener implements Listener {

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().split(" ")[0].substring(1); // Extrahiere den Befehl ohne das "/"-Präfix
        String playerName = event.getPlayer().getName();

        if(command.equalsIgnoreCase("msg")){
            if(!(event.getMessage().split(" ").length == 3)){
                return;
            }
            String message = event.getMessage().split(" ")[2];
            String resiver = event.getMessage().split(" ")[1];
            Main.bot.getTextChannelById("1113557918403526688").sendMessage("msg von " + playerName + " zu " + resiver  +  ": "+ message).queue();
        }

    }
}
