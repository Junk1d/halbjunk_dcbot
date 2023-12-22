package de.halbjunk.dcbot.mcEvent;

import de.halbjunk.dcbot.Main;
import net.dv8tion.jda.api.entities.Activity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Objects;

public class PlayerDeathListener implements Listener {
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        //Main.bot.getPresence().setActivity(Activity.playing(event.getEntity().getName() + " ist gestorben"));
        if(event.getEntity().getKiller() != null){
            Main.bot.getPresence().setActivity(Activity.streaming(event.getEntity().getKiller().getName() + " killed " + event.getEntity().getName(), "https://twitch.tv/junk1d"));
            return;
        }

        Main.bot.getPresence().setActivity(Activity.streaming(event.getEntity().getName() + " ist gestorben", "https://twitch.tv/junk1d"));
        //TODO Wenn Zeit: Bestimmte Death nachrichten ausgeben

    }
}
