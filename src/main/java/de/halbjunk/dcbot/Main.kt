package de.halbjunk.dcbot

import de.halbjunk.dcbot.clans.Clan
import de.halbjunk.dcbot.clans.ClanMCCommand
import de.halbjunk.dcbot.dcEvent.DcChatBroadcastListener
import de.halbjunk.dcbot.dcEvent.GuildMemberRemove
import de.halbjunk.dcbot.dcEvent.WhitelistListener
import de.halbjunk.dcbot.mcEvent.*
import de.halbjunk.dcbot.util.Lag
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.Scoreboard
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

class Main : JavaPlugin() {
//    var config: FileConfiguration? = null
    override fun onEnable() {
//        String url = "jdbc:mysql://localhost:3306/falconbyte";
//        String user = "root";
//        String pass = "";
//

//        try {
//            Connection con = DriverManager.getConnection(url, user, pass);
//            System.out.println("Verbindung erfolgreich hergestellt");
//
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//        }
        Bukkit.getServer().scheduler.scheduleSyncRepeatingTask(this, Lag(), 100L, 1L)
        plugin = this
        board = Bukkit.getScoreboardManager()!!.mainScoreboard
        Companion.file = File(dataFolder, "config.yml")
        if (!Companion.file!!.exists()) {
            dataFolder.mkdir()
            try {
                Companion.file!!.createNewFile()
            } catch (e: IOException) {
                println("config.yml konnte nicht erstellt werden!!!")
            }
        }
        FileSaveLoad.dcIdsLoader()
        val configs = YamlConfiguration.loadConfiguration(Companion.file!!)
        val statusChannelId = configs.getString("statusChannel")
        if (statusChannelId != null) {
            Status.statusChannelId = statusChannelId
        }
        val statusMessageId = configs.getString("statusMessage")
        if (statusMessageId != null) {
            Status.statusMessageId = statusMessageId
        }
        val domain = configs.getString("domain")
        if (domain != null) {
            Status.domain = domain
        }


        //Minecraft load
        server.pluginManager.registerEvents(McJoinLeaveListener(), this)
        server.pluginManager.registerEvents(PlayerDeathListener(), this)
        server.pluginManager.registerEvents(PlayerAdvancementDoneListener(), this)
        server.pluginManager.registerEvents(McChatBroadcastListener(), this)
        server.pluginManager.registerEvents(CommandListener(), this)
        getCommand("clan")!!.setExecutor(ClanMCCommand())
        val token: String
        val path = Paths.get(Main.plugin?.getDataFolder().toString() + "/secret.txt")
        token = try {
            String(Files.readAllBytes(path), StandardCharsets.UTF_8)
        } catch (e: IOException) {
            println("Error reading secret value from file: " + e.message)
            return
        }


        //Discord load
        val builder = JDABuilder.createDefault(token)
        builder.enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
        builder.setStatus(OnlineStatus.ONLINE)
        //builder.setActivity(Activity.playing("HalbJunk.de mit " + 0 + " Leuten"));
        builder.setActivity(Activity.streaming("Server Online", "https://twitch.tv/junk1d"))
        builder.addEventListeners(WhitelistListener())
        builder.addEventListeners(GuildMemberRemove())
        builder.addEventListeners(Status())
        builder.addEventListeners(DcChatBroadcastListener())
        bot = builder.build()
        println("Online")


        //bot.getPresence().setActivity(Activity.playing("HalbJunk.de"));
    }

    override fun onDisable() {
        // Plugin shutdown logic
        Status.stopStatusUpdateTask()
        try {
            FileSaveLoad.dcIdsSaver()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            FileSaveLoad.clanSaver()
        } catch (e: IOException) {
            try {
                FileSaveLoad.clanSaver()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
            e.printStackTrace()
        }
        if (bot != null) {
            bot!!.shutdown()
        }
    }

    companion object {
        @JvmField
        var file: File? = null
        @JvmField
        var clanFile: File? = null
        @JvmField
        var dcToMcID = HashMap<String, String>()
        @JvmField
        var clans = HashMap<String, Clan>()
        @JvmField
        var plugin: JavaPlugin? = null
        @JvmField
        var bot: JDA? = null
        @JvmField
        var board: Scoreboard? = null
    }
}
