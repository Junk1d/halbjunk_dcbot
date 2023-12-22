package de.halbjunk.dcbot.clans;


import de.halbjunk.dcbot.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class Clan {
    private String name;

    public String getDcCategoryId() {
        return dcCategoryId;
    }

    public void setDcCategoryId(String dcCategoryId) {
        this.dcCategoryId = dcCategoryId;
    }

    private String dcCategoryId;
    public List<String> getMemberList() {
        return memberList;
    }

    public void addMemberList(String memberId) {
        this.memberList.add(memberId);
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public List<String> getModerators() {
        return moderators;
    }

    public void setModerators(List<String> moderators) {
        this.moderators = moderators;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public void setMemberList(List<String> memberList) {
        this.memberList = memberList;
    }

    private String roleId;

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    private List<String> memberList = new ArrayList<>();
    private String adminId;
    private String adminName;
    private List<String> moderators  = new ArrayList<>();
    private String prefix;
    public Clan(){

    }
    public Clan(String prefix, Player admin) {
        this.name = prefix;
        this.adminId = admin.getUniqueId().toString();
        this.prefix = prefix;
        this.memberList.add(admin.getUniqueId().toString());
        Team team = Main.board.registerNewTeam(prefix);
        team.setPrefix("[" +prefix + "]");
        team.addEntry(admin.getName());
        //admin.addAttachment(Main.getPlugin(), prefix + ".clan.admin" , true);
        Main.clans.put(prefix, this);


        Guild guild = Main.bot.getGuildById("1092435763502796912"); // Ersetze DEINE_GUILD_ID durch die ID deines Servers

        Category category = guild.createCategory(name.toUpperCase() + " CLAN").complete(); // Erstelle die Kategorie
        this.dcCategoryId = category.getId();

        Role role = guild.createRole()
                .setName(prefix)
                .complete(); // Erstelle die Rolle

        this.roleId = role.getId();

        TextChannel textChannel = guild.createTextChannel(prefix + " Text")
                .setParent(category) // Setze die Kategorie als Elternkanal
                .addPermissionOverride(role, // Erlaube der Rolle Zugriff
                        EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), // Erlaube das Anzeigen und Schreiben von Nachrichten
                        null) // Keine Berechtigungen für andere Benutzer festgelegt
                .addPermissionOverride(guild.getPublicRole(), // Verweigere Berechtigungen für die @everyone-Rolle
                        null, // Keine Berechtigungen für die @everyone-Rolle festgelegt
                        EnumSet.of(Permission.VIEW_CHANNEL)) // Verweigere das Anzeigen des Kanals für alle anderen Benutzer
                .complete();

        VoiceChannel voiceChannel = guild.createVoiceChannel(prefix + " Talk")
                .setParent(category) // Setze die Kategorie als Elternkanal
                .addPermissionOverride(role, // Erlaube der Rolle Zugriff
                        EnumSet.of(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT), // Erlaube das Anzeigen und Verbinden zum Sprachkanal
                        null) // Keine Berechtigungen für andere Benutzer festgelegt
                .addPermissionOverride(guild.getPublicRole(), // Verweigere Berechtigungen für die @everyone-Rolle
                        null, // Keine Berechtigungen für die @everyone-Rolle festgelegt
                        EnumSet.of(Permission.VIEW_CHANNEL)) // Verweigere das Anzeigen des Kanals für alle anderen Benutzer
                .complete();

        String userId = getKeyByValue(Main.dcToMcID, admin.getUniqueId().toString());

        guild.retrieveMemberById(userId).queue(
                member -> {
                    guild.addRoleToMember(member, role).queue(
                            //success -> System.out.println("Rolle erfolgreich zugewiesen an Benutzer mit ID: " + userId),
                            //error -> System.err.println("Fehler beim Zuweisen der Rolle: " + error.getMessage())
                    );
                }//,
                //error -> System.err.println("Benutzer nicht gefunden mit ID: " + userId)
        );



        admin.sendMessage(this.getPrefix() + " wurde erstellt");

    }


    public void leave(Player player){
        Guild guild = Main.bot.getGuildById("1092435763502796912");
        String userId = getKeyByValue(Main.dcToMcID, player.getUniqueId().toString());
        guild.retrieveMemberById(userId).queue(
                member -> {
                    guild.removeRoleFromMember(member, guild.getRoleById(this.getRoleId())).queue(
                            success -> System.out.println("Rolle erfolgreich vom Benutzer mit ID: " + userId+ " weggenommen"),
                            error -> System.err.println("Fehler beim wegnehmen der Rolle: " + error.getMessage())
                    );
                },
                error -> System.err.println("Benutzer nicht gefunden mit ID: " + userId)
        );

        Main.board.getTeam(this.getPrefix()).removeEntry(player.getName());
        this.memberList.remove(player.getUniqueId().toString());
    }



    public void join(Player player){
        this.memberList.add(player.getUniqueId().toString());
        Main.board.getTeam(this.getPrefix()).addEntry(player.getName());
        Guild guild = Main.bot.getGuildById("1092435763502796912");

        String userId = getKeyByValue(Main.dcToMcID, player.getUniqueId().toString());

        guild.retrieveMemberById(userId).queue(
                member -> {
                    guild.addRoleToMember(member, guild.getRoleById(this.getRoleId())).queue(
                            success -> System.out.println("Rolle erfolgreich zugewiesen an Benutzer mit ID: " + userId),
                            error -> System.err.println("Fehler beim Zuweisen der Rolle: " + error.getMessage())
                    );
                },
                error -> System.err.println("Benutzer nicht gefunden mit ID: " + userId)
        );

    }


    public  void delete(){
        Guild guild = Main.bot.getGuildById("1092435763502796912");
        guild.getRoleById(this.getRoleId()).delete().queue();
        Category category = guild.getCategoryById(this.getDcCategoryId());
        if (category != null) {
            List<TextChannel> textChannels = category.getTextChannels();
            for (TextChannel textChannel : textChannels) {
                textChannel.delete().queue();
            }

            List<VoiceChannel> voiceChannels = category.getVoiceChannels();
            for (VoiceChannel voiceChannel : voiceChannels) {
                voiceChannel.delete().queue();
            }
            category.delete().queue();
        }
        if(Main.board.getTeam(this.getPrefix()) != null){
            Main.board.getTeam(this.getPrefix()).unregister();
        }

        Main.clans.remove(this.prefix);
    }


    public static boolean isAdmin(Player p){
        for (Map.Entry<String, Clan> entry : Main.clans.entrySet()) {
            String k = entry.getKey();
            Clan v = entry.getValue();
            if (v.getAdminId().equals(p.getUniqueId().toString())) {
                return true;
            }
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public static <K, V> K getKeyByValue(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

}
