package de.halbjunk.dcbot.dcEvent

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.bukkit.Bukkit

class MSG : ListenerAdapter() {
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name == "message") {
            event.deferReply().queue() // Bestätigung der Slash-Antwort
            val playerName = event.getOption("spielername")!!.asString
            val message = event.getOption("nachricht")!!.asString
            val player = Bukkit.getPlayerExact(playerName)
            if (player == null || !player.isOnline) {
                event.hook.sendMessage("Spieler nicht gefunden oder offline.").queue()
                return
            }
            player.sendMessage("[Discord] Geheime Nachricht: $message")
            event.hook.sendMessage("Nachricht erfolgreich an $playerName gesendet.").queue()
        }
    }
}
