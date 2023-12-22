package de.halbjunk.dcbot.dcEvent;

import de.halbjunk.dcbot.Main;
//import jdk.nashorn.internal.parser.JSONParser;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;


public class WhitelistListener extends ListenerAdapter{
    public void onMessageReceived (MessageReceivedEvent event){
        if (event.getAuthor().isBot()) return;
        if (!event.isFromGuild()) return;
        if (!event.getChannel().getId().equals("1092436858442616883")) return;

        String massage = event.getMessage().getContentRaw();

        if (Main.dcToMcID.containsKey(event.getAuthor().getId())){
            event.getMessage().addReaction(Emoji.fromFormatted("❌")).queue();
            event.getChannel().sendMessage("Man kann pro Discord Account nur einen Minecraft Account Whitelisten").queue();
            return;
        }
        String id = mcId(massage);
        if(id == null){
            event.getMessage().addReaction(Emoji.fromFormatted("❌")).queue();
            event.getChannel().sendMessage(massage+ " ist kein Minecraft Account").queue();
            return;
        }
        String uuid = fromTrimmed(id);
        if (isWhitelisted(massage)){
            event.getMessage().addReaction(Emoji.fromFormatted("❌")).queue();
            event.getChannel().sendMessage(massage+ " ist schon auf der Whitelist").queue();
            return;
        }

        try {
            event.getGuild().addRoleToMember(event.getAuthor(), Objects.requireNonNull(event.getGuild().getRoleById("1092436393722134549"))).queue();
            Main.dcToMcID.put(event.getAuthor().getId(), uuid);
            Bukkit.getScheduler().callSyncMethod( Main.getPlugin(), () -> Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(),"whitelist add "+massage)).get();
            Bukkit.reloadWhitelist();
            event.getMessage().addReaction(Emoji.fromFormatted("✅")).queue();
        } catch (Exception e) {
            event.getChannel().sendMessage("Rolle konnte nicht vergeben werden").queue();
            event.getMessage().addReaction(Emoji.fromFormatted("❌")).queue();
        }





    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();
        if( ! command.equals("setwhitelistchannel"))return;

//        if(event.getMember().isOwner()){
//            event.reply("owner").setEphemeral(true).queue();
//            return;
//        }
        event.reply("test passed").setEphemeral(true).queue();
        event.getChannel().sendMessage("awdawd").queue(new Consumer<Message>()
        {
            @Override
            public void accept(Message t)
            {
                System.out.printf("Sent Message %s\n", t.getIdLong());
            }
        });

    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        event.getGuild().upsertCommand(Commands.slash("setwhitelistchannel", "setwhitelistchannel").setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))).queue();
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        event.getGuild().upsertCommand(Commands.slash("setwhitelistchannel", "setwhitelistchannel").setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))).queue();
    }

    public boolean isWhitelisted(String name){
        for (OfflinePlayer player : Main.getPlugin().getServer().getWhitelistedPlayers()) {
            if( player.getName().equalsIgnoreCase(name)){
                return true;
            }
        }
        return false;
    }


    public String fromTrimmed(String trimmedUUID) throws IllegalArgumentException{
        if(trimmedUUID == null) throw new IllegalArgumentException();
        StringBuilder builder = new StringBuilder(trimmedUUID.trim());
        /* Backwards adding to avoid index adjustments */
        try {
            builder.insert(20, "-");
            builder.insert(16, "-");
            builder.insert(12, "-");
            builder.insert(8, "-");
        } catch (StringIndexOutOfBoundsException e){
            throw new IllegalArgumentException();
        }

        return builder.toString();
    }

    public String mcId (String message){
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + message);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            System.out.println(connection.getResponseCode());
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(response.toString());
            String id = (String) obj.get("id");
            return id;
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            System.out.println("2");
            return null;
        }

    }
}

