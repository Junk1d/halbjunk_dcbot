package de.halbjunk.dcbot

import de.halbjunk.dcbot.util.Lag
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.ErrorResponse
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.awt.Color
import java.io.IOException
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class Status : ListenerAdapter() {
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val command = event.name
        if (event.user.isBot) return
        if (command == "setstatuschannel") {
            event.reply("Status Channel has been set").setEphemeral(true).queue()
            val embedBuilder = EmbedBuilder()
            embedBuilder.setTitle("Server Status")
            embedBuilder.setColor(Color.GREEN)
            //
//        event
            event.channel.sendMessageEmbeds(embedBuilder.build()).queue { t ->
                val messageId = t.idLong.toString()
                statusMessageId = messageId
                println(messageId)
                val ChannelId = t.channel.id
                statusChannelId = ChannelId
                println(ChannelId)
                val config = YamlConfiguration.loadConfiguration(Main.file!!)
                config["statusChannel"] = statusChannelId
                config["statusMessage"] = statusMessageId
                try {
                    config.save(Main.file!!)
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
                statusChannel = event.jda.getTextChannelById(statusChannelId!!)
                if (statusChannel!!.name != "\uD83D\uDFE2-server-status") {
                    statusChannel!!.manager.setName("\uD83D\uDFE2-server-status").queue()
                }
                statusUpdateTask = object : BukkitRunnable() {
                    override fun run() {
                        updateStatus()
                    }
                }

                // Starte den Task und führe ihn alle 5 Minuten aus
                (statusUpdateTask as BukkitRunnable).runTaskTimer(Main.plugin!!, 0, TimeUnit.MINUTES.toSeconds(1) * 20)
            }
        }
        if (command == "setdomain") {
            domain = event.getOption("domain")!!.asString
            println(domain)
            event.reply("domain has been set").setEphemeral(true).queue()
            val config = YamlConfiguration.loadConfiguration(Main.file!!)
            config["domain"] = domain
            try {
                config.save(Main.file!!)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
            updateStatus()
        }
    }

    override fun onGuildReady(event: GuildReadyEvent) {
        event.guild.upsertCommand(Commands.slash("setstatuschannel", "setstatuschannel").setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))).queue()
        event.guild.upsertCommand(Commands.slash("setdomain", "setstatuschannel").setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)).addOption(OptionType.STRING, "domain", "domain shown in server status", true)).queue()
    }

    override fun onGuildJoin(event: GuildJoinEvent) {
        event.guild.upsertCommand(Commands.slash("setdomain", "setstatuschannel").setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)).addOption(OptionType.STRING, "domain", "domain shown in server status", true)).queue()
        event.guild.upsertCommand(Commands.slash("setstatuschannel", "setstatuschannel").setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))).queue()
    }

    override fun onReady(e: ReadyEvent) {
        try {
            FileSaveLoad.clanLoader()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        if (statusChannelId == null) return
        if (statusMessageId == null) return
        statusChannel = e.jda.getTextChannelById(statusChannelId!!)
        if (statusChannel!!.name != "\uD83D\uDFE2-server-status") {
            statusChannel!!.manager.setName("\uD83D\uDFE2-server-status").queue()
        }
        statusUpdateTask = object : BukkitRunnable() {
            override fun run() {
                updateStatus()
            }
        }

        // Starte den Task und führe ihn alle 5 Minuten aus
        (statusUpdateTask as BukkitRunnable).runTaskTimer(Main.plugin!!, 0, TimeUnit.MINUTES.toSeconds(1) * 20)
    }

    companion object {
        private var statusChannel: TextChannel? = null
        private var statusUpdateTask: BukkitRunnable? = null
        private val statusMessage: Message? = null
        var statusMessageId: String? = null
        var statusChannelId: String? = null
        var domain = "localhost"
        fun stopStatusUpdateTask() {
            // Stoppe den Aktualisierungs-Task, wenn das Plugin deaktiviert wird
            if (statusUpdateTask != null) {
                val embedBuilder = EmbedBuilder()
                embedBuilder.setTitle("Server Status")
                embedBuilder.setColor(Color.RED)
                embedBuilder.addField("STATUS", "```offline```", true)
                embedBuilder.setTimestamp(LocalDateTime.now())
                statusChannel!!.retrieveMessageById(statusMessageId!!).queue({ message: Message -> message.editMessageEmbeds(embedBuilder.build()).queue() }, ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE) { e: ErrorResponseException? ->
                    // this means the message doesn't exist
                    println("Status Nachricht konnte nicht bebaeitet werden")
                })
                statusChannel!!.manager.setName("\uD83D\uDD34-server-status").queue()
                statusUpdateTask!!.cancel()
                statusUpdateTask!!.cancel()
            }
        }

        @JvmStatic
        fun updateStatus() {
            // Hol dir den aktuellen Status des Bukkit-Servers
            val playerCount = Bukkit.getOnlinePlayers().size
            val maxPlayers = Bukkit.getMaxPlayers()
            val serverStatus = Bukkit.getServer().motd
            if (statusChannel!!.name != "\uD83D\uDFE2-server-status") {
                statusChannel!!.manager.setName("\uD83D\uDFE2-server-status").queue()
            }

            // Erstelle ein Embed mit den aktuellen Informationen
            val embedBuilder = EmbedBuilder()
            embedBuilder.setTitle("Server Status")
            //        embedBuilder.setThumbnail("https://cdn.discordapp.com/attachments/1093822046514982962/1106642582706065408/unnamed-removebg-preview.png?ex=658f09b8&is=657c94b8&hm=74b25c8eaf861db2ad5bfa61078ded9c9bd0bab9c94f207f4017e86fb245e326&");
            embedBuilder.setColor(Color.GREEN)
            embedBuilder.addField("STATUS", "```online```", true)
            embedBuilder.addField("SPIELER", "```$playerCount/$maxPlayers```", true)
            val ping = calculateAveragePing()
            if (ping > 0.0) {
                embedBuilder.addField("PING", "```" + String.format("%.2f", ping) + " ms```", true)
            } else {
                embedBuilder.addField("PING", "```" + "-" + " ms```", true)
            }
            embedBuilder.addField("DOMAIN", "```" + domain + "```", true)
            embedBuilder.addField("TPS", "```" + String.format("%.2f", Lag.tPS) + "```", true)
            embedBuilder.setImage("https://i.stack.imgur.com/Fzh0w.png")
            if (playerCount > 0) {
                val playerListBuilder = StringBuilder()
                val onlinePlayers = Bukkit.getOnlinePlayers().toTypedArray<Player>()
                for (i in onlinePlayers.indices) {
                    playerListBuilder.append(onlinePlayers[i].name)
                    if (i < onlinePlayers.size - 1) {
                        playerListBuilder.append(", ")
                    }
                }
                val playerList = playerListBuilder.toString()
                embedBuilder.addField("SPIELER LISTE", "```$playerList```", false)
            }
            embedBuilder.setTimestamp(LocalDateTime.now())
            statusChannel!!.retrieveMessageById(statusMessageId!!).queue({ message: Message -> message.editMessageEmbeds(embedBuilder.build()).queue() }, ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE) { e: ErrorResponseException? ->
                // this means the message doesn't exist
                println("Status Nachricht konnte nicht bebaeitet werden")
            })


            /*
        // Aktualisiere den Status im Textkanal
        if (statusMessage == null) {
            // Wenn die Nachricht noch nicht gesendet wurde, sende sie
            statusChannel.sendMessageEmbeds(embedBuilder.build()).queue(message -> statusMessage = message);
        } else {
            // Wenn die Nachricht bereits gesendet wurde, bearbeite sie
            statusMessage.editMessageEmbeds(embedBuilder.build()).queue();
        }*/
        }

        fun calculateAveragePing(): Double {
            var totalPing = 0
            var playerCount = 0
            for (player in Bukkit.getServer().onlinePlayers) {
                val ping = player.ping
                totalPing += ping
                playerCount++
            }
            return if (playerCount > 0) {
                totalPing.toDouble() / playerCount
            } else {
                return 0.0
            }
        }
    }
}
