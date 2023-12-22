package de.halbjunk.dcbot.dcEvent

import de.halbjunk.dcbot.Main
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.bukkit.Bukkit
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

//import jdk.nashorn.internal.parser.JSONParser;
class WhitelistListener : ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot) return
        if (!event.isFromGuild) return
        if (event.channel.id != "1092436858442616883") return
        val massage = event.message.contentRaw
        if (Main.dcToMcID.containsKey(event.author.id)) {
            event.message.addReaction(Emoji.fromFormatted("❌")).queue()
            event.channel.sendMessage("Man kann pro Discord Account nur einen Minecraft Account Whitelisten").queue()
            return
        }
        val id = mcId(massage)
        if (id == null) {
            event.message.addReaction(Emoji.fromFormatted("❌")).queue()
            event.channel.sendMessage("$massage ist kein Minecraft Account").queue()
            return
        }
        val uuid = fromTrimmed(id)
        if (isWhitelisted(massage)) {
            event.message.addReaction(Emoji.fromFormatted("❌")).queue()
            event.channel.sendMessage("$massage ist schon auf der Whitelist").queue()
            return
        }
        try {
            Objects.requireNonNull(event.guild.getRoleById("1092436393722134549"))?.let { event.guild.addRoleToMember(event.author, it).queue() }
            Main.dcToMcID[event.author.id] = uuid
            Bukkit.getScheduler().callSyncMethod(Main.plugin!!) { Bukkit.dispatchCommand(Bukkit.getServer().consoleSender, "whitelist add $massage") }.get()
            Bukkit.reloadWhitelist()
            event.message.addReaction(Emoji.fromFormatted("✅")).queue()
        } catch (e: Exception) {
            event.channel.sendMessage("Rolle konnte nicht vergeben werden").queue()
            event.message.addReaction(Emoji.fromFormatted("❌")).queue()
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val command = event.name
        if (command == "setwhitelistchannel"){
            event.reply("test passed").setEphemeral(true).queue()
            event.channel.sendMessage("awdawd").queue { t -> System.out.printf("Sent Message %s\n", t.idLong) }

        }
    }

    override fun onGuildReady(event: GuildReadyEvent) {
        event.guild.upsertCommand(Commands.slash("setwhitelistchannel", "setwhitelistchannel").setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))).queue()
    }

    override fun onGuildJoin(event: GuildJoinEvent) {
        event.guild.upsertCommand(Commands.slash("setwhitelistchannel", "setwhitelistchannel").setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))).queue()
    }

    fun isWhitelisted(name: String?): Boolean {
        for (player in Main.plugin!!.server.whitelistedPlayers) {
            if (player.name.equals(name, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    @Throws(IllegalArgumentException::class)
    fun fromTrimmed(trimmedUUID: String?): String {
        requireNotNull(trimmedUUID)
        val builder = StringBuilder(trimmedUUID.trim { it <= ' ' })
        /* Backwards adding to avoid index adjustments */try {
            builder.insert(20, "-")
            builder.insert(16, "-")
            builder.insert(12, "-")
            builder.insert(8, "-")
        } catch (e: StringIndexOutOfBoundsException) {
            throw IllegalArgumentException()
        }
        return builder.toString()
    }

    fun mcId(message: String): String? {
        return try {
            val url = URL("https://api.mojang.com/users/profiles/minecraft/$message")
            val connection = url.openConnection() as HttpURLConnection
            println(connection.getResponseCode())
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()
            val parser = JSONParser()
            val obj = parser.parse(response.toString()) as JSONObject
            obj["id"] as String
        } catch (e: IOException) {
            e.printStackTrace()
            println("2")
            null
        } catch (e: ParseException) {
            e.printStackTrace()
            println("2")
            null
        }
    }
}
