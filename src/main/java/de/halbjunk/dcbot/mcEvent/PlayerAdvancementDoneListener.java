package de.halbjunk.dcbot.mcEvent;

import de.halbjunk.dcbot.Main;
import net.dv8tion.jda.api.entities.Activity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public class PlayerAdvancementDoneListener implements Listener {
    @EventHandler
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent event) {
        String playerName = event.getPlayer().getName();
        String advancement = event.getAdvancement().getKey().getKey();
        Main.bot.getPresence().setActivity(Activity.streaming(playerName + " hat " + advancement , "https://twitch.tv/junk1d"));


    }
}
