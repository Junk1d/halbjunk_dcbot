package de.halbjunk.dcbot.mcEvent

import de.halbjunk.dcbot.Main
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent

class CommandListener : Listener {
    @EventHandler
    fun onCommandPreprocess(event: PlayerCommandPreprocessEvent) {
        val command = event.message.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].substring(1) // Extrahiere den Befehl ohne das "/"-Präfix
        val playerName = event.player.name
        if (command.equals("msg", ignoreCase = true)) {
            if (event.message.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size != 3) {
                return
            }
            val message = event.message.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[2]
            val resiver = event.message.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            Main.bot!!.getTextChannelById("1113557918403526688")!!.sendMessage("msg von $playerName zu $resiver: $message").queue()
        }
    }
}
