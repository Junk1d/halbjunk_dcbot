package de.halbjunk.dcbot;

import de.halbjunk.dcbot.util.Lag;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.w3c.dom.events.EventTarget;

import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.sql.Time;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

public class Status extends ListenerAdapter {
    private static TextChannel statusChannel;
    private static BukkitRunnable statusUpdateTask;
    private static Message statusMessage;

    @Override
    public void onReady(ReadyEvent e){

        try {
            FileSaveLoad.clanLoader();
        } catch (IOException ex) {
            ex.printStackTrace();
        }


        statusChannel = e.getJDA().getTextChannelById("1113199643115921538");
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

            statusChannel.retrieveMessageById("1114253049343516792").queue((message) -> {
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
        embedBuilder.setColor(Color.GREEN);
        embedBuilder.addField("STATUS", "```online```", true);
        embedBuilder.addField("SPIELER", "```"+playerCount + "/" + maxPlayers + "```", true);
        double ping = calculateAveragePing();
        if(ping > 0){
            embedBuilder.addField("PING", "```"+ ping +" ms```", true);
        } else embedBuilder.addBlankField(true);
        embedBuilder.addField("IP ADRESSE", "```HalbJunk.de```", true);
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        decimalFormat.setMaximumFractionDigits(2);
        String formattedValue = decimalFormat.format(Lag.getTPS());
        double result = Double.parseDouble(formattedValue);
        embedBuilder.addField("TPS", "```"+ result +"```", true);
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

        statusChannel.retrieveMessageById("1114253049343516792").queue((message) -> {
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
