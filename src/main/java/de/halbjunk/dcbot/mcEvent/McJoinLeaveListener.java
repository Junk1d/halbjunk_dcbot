package de.halbjunk.dcbot.mcEvent;

import de.halbjunk.dcbot.Main;
import de.halbjunk.dcbot.Status;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Team;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class McJoinLeaveListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        Main.bot.getPresence().setActivity(Activity.streaming(event.getPlayer().getDisplayName() + " betreten", "https://twitch.tv/junk1d"));

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Status.updateStatus();
            }
        }, 0);

    }


    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (!event.getPlayer().isWhitelisted()) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, ChatColor.RED + "Du bist nicht auf der Whitelist!");
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetDateTime = LocalDateTime.of(2023, 6, 10, 12, 0, 0);
        if(!event.getPlayer().isOp()){
            if (now.isBefore(targetDateTime)){
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.DARK_RED + "Der Server startet erst am " + targetDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " um " + targetDateTime.format(DateTimeFormatter.ofPattern("HH:mm")) + " Uhr");
            }
        }
    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){

        Main.bot.getPresence().setActivity(Activity.streaming(event.getPlayer().getDisplayName() + " verlassen", "https://twitch.tv/junk1d"));
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Status.updateStatus();
            }
        }, 15);
    }




}
