package de.halbjunk.dcbot.dcEvent

import de.halbjunk.dcbot.Main
import de.halbjunk.dcbot.clans.ClanMCCommand.Companion.getClanByPlayer
import de.halbjunk.dcbot.clans.ClanMCCommand.Companion.isAdmin
import de.halbjunk.dcbot.clans.ClanMCCommand.Companion.isClan
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

class GuildMemberRemove : ListenerAdapter() {
    override fun onGuildMemberRemove(event: GuildMemberRemoveEvent) {
        if (Main.dcToMcID.containsKey(event.user.id)) {
            val server = Bukkit.getServer()
            println(server.whitelistedPlayers)
            var player: OfflinePlayer? = null
            for (offlinePlayer in server.whitelistedPlayers) {
                if (offlinePlayer.uniqueId.toString() == Main.dcToMcID[event.user.id]) {
                    player = offlinePlayer
                    break
                }
            }
            if (player != null) {
                player.isWhitelisted = false
                Bukkit.reloadWhitelist()
            }
            Main.dcToMcID.remove(event.user.id)
            if (isClan((player as Player?)!!)) {
                if (isAdmin(player!!)) {
                    getClanByPlayer(player!!)!!.delete()
                } else {
                    getClanByPlayer(player!!)!!.leave(player!!)
                }
            }
        }
    }
}
