package de.halbjunk.dcbot.mcEvent

import de.halbjunk.dcbot.Main
import net.dv8tion.jda.api.entities.Activity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAdvancementDoneEvent

class PlayerAdvancementDoneListener : Listener {
    @EventHandler
    fun onPlayerAdvancementDone(event: PlayerAdvancementDoneEvent) {
        val playerName = event.player.name
        val advancement = event.advancement.key.key
        Main.bot!!.presence.activity = Activity.streaming("$playerName hat $advancement", "https://twitch.tv/junk1d")
    }
}
