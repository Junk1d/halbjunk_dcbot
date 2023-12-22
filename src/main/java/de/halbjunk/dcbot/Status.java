package de.halbjunk.dcbot;

import de.halbjunk.dcbot.util.Lag;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Status extends ListenerAdapter {
    private static TextChannel statusChannel;
    private static BukkitRunnable statusUpdateTask;
    private static Message statusMessage;

    public static String statusMessageId;
    public static String statusChannelId;
    public static String domain = "localhost";

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();
        if(event.getUser().isBot()) return;
        if(command.equals("setstatuschannel")){
            event.reply("Status Channel has been set").setEphemeral(true).queue();

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Server Status");
            embedBuilder.setColor(Color.GREEN);
//
//        event
            event.getChannel().sendMessageEmbeds(embedBuilder.build()).queue(new Consumer<Message>(){
                @Override
                public void accept(Message t) {

                    String messageId = String.valueOf(t.getIdLong());
                    statusMessageId=messageId;
                    System.out.println(messageId);
                    String ChannelId = t.getChannel().getId();
                    statusChannelId=ChannelId;
                    System.out.println(ChannelId);
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(Main.file);
                    config.set( "statusChannel" , statusChannelId);
                    config.set( "statusMessage" , statusMessageId);
                    try {
                        config.save(Main.file);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    statusChannel = event.getJDA().getTextChannelById(statusChannelId);
                    if(!statusChannel.getName().equals("\uD83D\uDFE2-server-status")){
                        statusChannel.getManager().setName("\uD83D\uDFE2-server-status").queue();
                    }

                    statusUpdateTask = new BukkitRunnable() {
                        @Override
                        public void run() {
                            updateStatus();
                        }
                    };

                    // Starte den Task und führe ihn alle 5 Minuten aus
                    statusUpdateTask.runTaskTimer(Main.getPlugin(), 0, TimeUnit.MINUTES.toSeconds(1) * 20);
                }
            });

        }
        if(command.equals("setdomain")){
           domain = event.getOption("domain").getAsString();
           System.out.println(domain);
            event.reply("domain has been set").setEphemeral(true).queue();
            YamlConfiguration config = YamlConfiguration.loadConfiguration(Main.file);
            config.set( "domain" , domain);
            try {
                config.save(Main.file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            updateStatus();
        }


    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        event.getGuild().upsertCommand(Commands.slash("setstatuschannel", "setstatuschannel").setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))).queue();
        event.getGuild().upsertCommand(Commands.slash("setdomain", "setstatuschannel").setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)).addOption(OptionType.STRING,"domain", "domain shown in server status", true )).queue();
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        event.getGuild().upsertCommand(Commands.slash("setdomain", "setstatuschannel").setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)).addOption(OptionType.STRING,"domain", "domain shown in server status", true )).queue();
        event.getGuild().upsertCommand(Commands.slash("setstatuschannel", "setstatuschannel").setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))).queue();
    }

    @Override
    public void onReady(ReadyEvent e){

        try {
            FileSaveLoad.clanLoader();
        } catch (IOException ex) {
            ex.printStackTrace();
        }


        if(statusChannelId == null)return;
        if(statusMessageId == null)return;
        statusChannel = e.getJDA().getTextChannelById(statusChannelId);
        if(!statusChannel.getName().equals("\uD83D\uDFE2-server-status")){
            statusChannel.getManager().setName("\uD83D\uDFE2-server-status").queue();
        }

        statusUpdateTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateStatus();
            }
        };

        // Starte den Task und führe ihn alle 5 Minuten aus
        statusUpdateTask.runTaskTimer(Main.getPlugin(), 0, TimeUnit.MINUTES.toSeconds(1) * 20);


    }

    public static void stopStatusUpdateTask() {
        // Stoppe den Aktualisierungs-Task, wenn das Plugin deaktiviert wird



        if (statusUpdateTask != null) {


            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Server Status");
            embedBuilder.setColor(Color.RED);
            embedBuilder.addField("STATUS", "```offline```", true);
            embedBuilder.setTimestamp(LocalDateTime.now());

            statusChannel.retrieveMessageById(statusMessageId).queue((message) -> {
                message.editMessageEmbeds(embedBuilder.build()).queue();
            }, new ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE, (e) -> {
                // this means the message doesn't exist
                System.out.println("Status Nachricht konnte nicht bebaeitet werden");
            }));
            statusChannel.getManager().setName("\uD83D\uDD34-server-status").queue();
            statusUpdateTask.cancel();
            statusUpdateTask.cancel();

        }
    }

    public static void updateStatus() {
        // Hol dir den aktuellen Status des Bukkit-Servers
        int playerCount = Bukkit.getOnlinePlayers().size();
        int maxPlayers = Bukkit.getMaxPlayers();
        String serverStatus = Bukkit.getServer().getMotd();

        if(!statusChannel.getName().equals("\uD83D\uDFE2-server-status")){
            statusChannel.getManager().setName("\uD83D\uDFE2-server-status").queue();
        }

        // Erstelle ein Embed mit den aktuellen Informationen
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Server Status");
//        embedBuilder.setThumbnail("https://cdn.discordapp.com/attachments/1093822046514982962/1106642582706065408/unnamed-removebg-preview.png?ex=658f09b8&is=657c94b8&hm=74b25c8eaf861db2ad5bfa61078ded9c9bd0bab9c94f207f4017e86fb245e326&");
        embedBuilder.setColor(Color.GREEN);
        embedBuilder.addField("STATUS", "```online```", true);
        embedBuilder.addField("SPIELER", "```"+playerCount + "/" + maxPlayers + "```", true);
        double ping = calculateAveragePing();
        if(ping > 0){

            embedBuilder.addField("PING", "```"+ String.format("%.2f", ping) +" ms```", true);
        } else {
            embedBuilder.addField("PING", "```"+ "-" +" ms```", true);

        }
        embedBuilder.addField("DOMAIN", "```"+domain+"```", true);
        embedBuilder.addField("TPS", "```"+ String.format("%.2f", Lag.getTPS()) +"```", true);
        embedBuilder.setImage("https://i.stack.imgur.com/Fzh0w.png");

        if(playerCount > 0){
            StringBuilder playerListBuilder = new StringBuilder();
            Player[] onlinePlayers = Bukkit.getOnlinePlayers().toArray(new Player[0]);

            for (int i = 0; i < onlinePlayers.length; i++) {
                playerListBuilder.append(onlinePlayers[i].getName());

                if (i < onlinePlayers.length - 1) {
                    playerListBuilder.append(", ");
                }
            }
            String playerList = playerListBuilder.toString();

            embedBuilder.addField("SPIELER LISTE","```" + playerList  + "```" , false);
        }



        embedBuilder.setTimestamp(LocalDateTime.now());

        statusChannel.retrieveMessageById(statusMessageId).queue((message) -> {
            message.editMessageEmbeds(embedBuilder.build()).queue();
        }, new ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE, (e) -> {
            // this means the message doesn't exist
            System.out.println("Status Nachricht konnte nicht bebaeitet werden");
        }));




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



    public static double calculateAveragePing() {
        int totalPing = 0;
        int playerCount = 0;

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            int ping = player.getPing();
            totalPing += ping;
            playerCount++;
        }

        if (playerCount > 0) {
            return (double) totalPing / playerCount;
        } else {
            return 0;
        }
    }



}
