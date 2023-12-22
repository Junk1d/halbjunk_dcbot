package de.halbjunk.dcbot.dcEvent

import de.halbjunk.dcbot.Main
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.bukkit.ChatColor

class DcChatBroadcastListener : ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot) return
        if (!event.isFromGuild) return
        if (event.channel.id != "1107772229544067263") return
        val prefix = ChatColor.DARK_AQUA.toString() + "[Discord]" + ChatColor.RESET
        Main.plugin!!.server.broadcastMessage(prefix + " " + event.author.name + ": " + event.message.contentStripped)
    }
}
