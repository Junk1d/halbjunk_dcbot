package de.halbjunk.dcbot;

import de.halbjunk.dcbot.clans.Clan;
import de.halbjunk.dcbot.clans.ClanMCCommand;
import de.halbjunk.dcbot.dcEvent.DcChatBroadcastListener;
import de.halbjunk.dcbot.dcEvent.GuildMemberRemove;
import de.halbjunk.dcbot.dcEvent.WhitelistListener;
import de.halbjunk.dcbot.mcEvent.*;
import de.halbjunk.dcbot.util.Lag;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public final class Main extends JavaPlugin {
    FileConfiguration config;
    public static File file;
    public static File clanFile;
    public static HashMap<String,String> dcToMcID = new HashMap<>();
    public static HashMap<String, Clan> clans = new HashMap<>();
    private static JavaPlugin thisPlugin;
    public static JDA bot;
    public static Scoreboard board;


    @Override
    public void onEnable() {
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


        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Lag(), 100L, 1L);



        thisPlugin = this;
        board = Bukkit.getScoreboardManager().getMainScoreboard();


        file = new File (getDataFolder(), "config.yml");
        if (!file.exists()){
            getDataFolder().mkdir();
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("config.yml konnte nicht erstellt werden!!!");
            }
        }



        FileSaveLoad.dcIdsLoader();









        //Minecraft load
        getServer().getPluginManager().registerEvents(new McJoinLeaveListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
        //getServer().getPluginManager().registerEvents(new PlayerAdvancementDoneListener(), this);
        getServer().getPluginManager().registerEvents(new McChatBroadcastListener(), this);
        getServer().getPluginManager().registerEvents(new CommandListener(), this);

        getCommand("clan").setExecutor(new ClanMCCommand());
        String token;
        Path path = Paths.get(Main.getPlugin().getDataFolder() + "/secret.txt");
        try {
            token = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            System.out.println("Secret value read from file: " + token);
        } catch (IOException e) {
            System.out.println("Error reading secret value from file: " + e.getMessage());
            return;
        }


        //Discord load
        JDABuilder builder = JDABuilder.createDefault(token);

        builder.enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT);

        builder.setStatus(OnlineStatus.ONLINE);
        //builder.setActivity(Activity.playing("HalbJunk.de mit " + 0 + " Leuten"));
        builder.setActivity(Activity.streaming("Server Online", "https://twitch.tv/junk1d"));
        builder.addEventListeners( new WhitelistListener());
        builder.addEventListeners(new GuildMemberRemove());
        builder.addEventListeners(new Status());
        builder.addEventListeners(new DcChatBroadcastListener());


        bot = builder.build();
        System.out.println("Online");


        //bot.getPresence().setActivity(Activity.playing("HalbJunk.de"));
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Status.stopStatusUpdateTask();




        try {
            FileSaveLoad.dcIdsSaver();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileSaveLoad.clanSaver();
        } catch (IOException e) {
            try {
                FileSaveLoad.clanSaver();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
        if(bot != null){
            bot.shutdown();
        }

    }


    public static JavaPlugin getPlugin() {
        return thisPlugin;
    }
}



//TODO Message Sever Status
