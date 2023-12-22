package de.halbjunk.dcbot.mcEvent

import de.halbjunk.dcbot.Main
import de.halbjunk.dcbot.Status.Companion.updateStatus
import net.dv8tion.jda.api.entities.Activity
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class McJoinLeaveListener : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        Main.bot!!.presence.activity = Activity.streaming(event.player.displayName + " betreten", "https://twitch.tv/junk1d")
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                updateStatus()
            }
        }, 0)
    }

    @EventHandler
    fun onPlayerLogin(event: PlayerLoginEvent) {
        if (!event.player.isWhitelisted) {
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, ChatColor.RED.toString() + "Du bist nicht auf der Whitelist!")
            return
        }
        val now = LocalDateTime.now()
        val targetDateTime = LocalDateTime.of(2023, 6, 10, 12, 0, 0)
        if (!event.player.isOp) {
            if (now.isBefore(targetDateTime)) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.DARK_RED.toString() + "Der Server startet erst am " + targetDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " um " + targetDateTime.format(DateTimeFormatter.ofPattern("HH:mm")) + " Uhr")
            }
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        Main.bot!!.presence.activity = Activity.streaming(event.player.displayName + " verlassen", "https://twitch.tv/junk1d")
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                updateStatus()
            }
        }, 15)
    }
}
