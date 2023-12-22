package de.halbjunk.dcbot.mcEvent

import de.halbjunk.dcbot.Main
import net.dv8tion.jda.api.entities.Activity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class PlayerDeathListener : Listener {
    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        //Main.bot.getPresence().setActivity(Activity.playing(event.getEntity().getName() + " ist gestorben"));
        if (event.entity.killer != null) {
            Main.bot!!.presence.activity = Activity.streaming(event.entity.killer!!.name + " killed " + event.entity.name, "https://twitch.tv/junk1d")
            return
        }
        Main.bot!!.presence.activity = Activity.streaming(event.entity.name + " ist gestorben", "https://twitch.tv/junk1d")
        //TODO Wenn Zeit: Bestimmte Death nachrichten ausgeben
    }
}
