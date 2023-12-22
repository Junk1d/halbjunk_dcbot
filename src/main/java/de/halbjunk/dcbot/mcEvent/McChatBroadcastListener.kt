package de.halbjunk.dcbot.mcEvent

import de.halbjunk.dcbot.Main
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class McChatBroadcastListener : Listener {
    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        println("Chat Nachricht")
        val message = event.message
        val playerName = event.player.name
        Main.bot!!.getTextChannelById("1113557918403526688")!!.sendMessage("$playerName: $message").queue()
    }
}
