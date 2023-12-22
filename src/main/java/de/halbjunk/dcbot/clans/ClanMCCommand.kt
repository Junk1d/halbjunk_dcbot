package de.halbjunk.dcbot.clans;

import de.halbjunk.dcbot.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ClanMCCommand implements CommandExecutor, TabCompleter {

    private Map<String, Clan> clanInvitations = new HashMap<>();


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)) return false;
        Player p = (Player) sender;
        if (args.length == 0){
            p.sendMessage("  ");
            p.sendMessage("/clan create");
            p.sendMessage("/clan delete");
            p.sendMessage("/clan invite <spielername>");
            p.sendMessage("/clan kick <spielername>");
            p.sendMessage("/clan leave");
            p.sendMessage("  ");



        } else if (args[0].equalsIgnoreCase("delete")){
            if(args.length > 2){
                sender.sendMessage("/clan delete");
                return true;
            }
            if(args.length == 2){
                if(!sender.isOp()){
                    sender.sendMessage("/clan delete");
                    return true;
                }
                if(!Main.clans.containsKey(args[1])){
                    sender.sendMessage("Dieser Clan existiert noch nicht");
                    return true;
                }

                //delete
                Main.clans.get(args[1]).delete();
                return true;
            }
            if(!isClan(p)){
                sender.sendMessage("Du bist in keinem Clan");
                return true;
            }

            if(!Clan.isAdmin(p)){
                sender.sendMessage("Nur der Admin des Clans kann diesen auch Löschen");
                return true;
            }
            //delete
            getClanByPlayer(p).delete();
            return true;





        } else if (args[0].equalsIgnoreCase("leave")){
            if(!isClan(p)){
                sender.sendMessage("Du bist in keinem Clan");
                return true;
            }
            if(isAdmin(p)){
                sender.sendMessage("Um den Clan zu verlassen musst du ihn löschen");
                return true;
            }

            getClanByPlayer(p).leave(p);
            return true;






        } else if (args[0].equalsIgnoreCase("kick")){
            if (args.length < 2) {
                p.sendMessage("Bitte gib den Namen des Spielers ein, den du kicken möchtest.");
                return true;
            }

            if(!getClanByPlayer(p).getAdminId().equals(p.getUniqueId().toString())){
                sender.sendMessage("Du bist kein Admin von einem Clan");
                return true;
            }

            Player player = Bukkit.getPlayer(args[1]);
            if(player == null){
                for (OfflinePlayer offlinePlayer : Bukkit.getServer().getWhitelistedPlayers()) {
                    if (offlinePlayer.getName().equalsIgnoreCase(args[1])) {
                        player = (Player) offlinePlayer;
                        break;
                    }
                }
            }
            if(player == null){
                sender.sendMessage("Dieser Spieler existiert auf diesem Server nicht");
                return true;
            }
            if(p.getUniqueId() == player.getUniqueId()){
                sender.sendMessage("Du kannst dich nicht selber Kicken");
                return true;
            }
            if(!(getClanByPlayer(player).equals(getClanByPlayer(p)))){
                sender.sendMessage(args[1] + " ist nicht in deinem Clan");
                return true;
            }

            getClanByPlayer(p).leave(player);
            return true;





        } else if (args[0].equalsIgnoreCase("invite")){
            if (!isAdmin(p)) {
                p.sendMessage("Aktuelle kann nur der Admin des Clans Leute einladen");
                return true;
            }

            if (args.length < 2) {
                p.sendMessage("Bitte gib den Namen des Spielers ein, den du in deinen Clan einladen möchtest.");
                return true;
            }

            Player invitedPlayer = Bukkit.getPlayer(args[1]);
            if (invitedPlayer == null) {
                p.sendMessage("Der eingeladene Spieler ist nicht online.");
                return true;
            }

            // Überprüfen, ob der eingeladene Spieler bereits eine Einladung erhalten hat
            if (clanInvitations.containsKey(invitedPlayer.getUniqueId().toString())) {
                p.sendMessage("Der eingeladene Spieler hat bereits eine ausstehende Einladung.");
                return true;
            }
            Clan clan = getClanByPlayer(p);
            // Speichern der Einladung
            clanInvitations.put(invitedPlayer.getUniqueId().toString(), clan);

            p.sendMessage("Du hast " + invitedPlayer.getName() + " erfolgreich in deinen Clan eingeladen.");
            invitedPlayer.sendMessage("Du hast eine Einladung erhalten, dem Clan " + clan.getPrefix() + " beizutreten.");
            invitedPlayer.sendMessage("Um die Einladung anzunehmen, verwende den Befehl: /clan accept");
            invitedPlayer.sendMessage("Um die Einladung abzulehnen, verwende den Befehl: /clan decline");
            return true;





        } else if (args[0].equalsIgnoreCase("accept")){
            if(!clanInvitations.containsKey(p.getUniqueId().toString())){
                p.sendMessage("Du hast keine Einladung");
                return true;
            }
            if(isClan(p)){
                p.sendMessage("Du bist schon in einem Clan");
                return true;
            }
            Clan clan = clanInvitations.remove(p.getUniqueId().toString());
            clan.join(p);



        } else if (args[0].equalsIgnoreCase("decline")){
            if(!clanInvitations.containsKey(p.getUniqueId().toString())){
                p.sendMessage("Du hast keine Einladung");
                return true;
            }
            clanInvitations.remove(p.getUniqueId().toString());
            p.sendMessage("Du hast die Einladung abgelehnt");
            return true;




        } else if (args[0].equalsIgnoreCase("create")){

            if(isClan(p)){
                sender.sendMessage("Du musst erst dein aktuellen Clan verlassen");
                return true;
            }
            if(args.length == 1){
                sender.sendMessage("/clan create <clan kürzel>");
                return true;
            }
            if(args[1].length() > 6 || args[1].length() < 2){
                sender.sendMessage("Der Kürzel darf nur 2 bis 6 Zeichen betragen");
                return true;
            }

            boolean containsKeyIgnoreCase = Main.clans.keySet().stream()
                    .anyMatch(key -> key.equalsIgnoreCase(args[1]));

            if(containsKeyIgnoreCase){
                sender.sendMessage("Es existiert schon ein Clan namens " + args[1]);
                return true;
            }

            new Clan(args[1], p);
            return true;





        } else if(args[0].equalsIgnoreCase("help")){

        } else if(args[0].equalsIgnoreCase("info")){





        } else if(args[0].equalsIgnoreCase("list")){
            if(Main.clans.isEmpty()){
                p.sendMessage("Es gibt noch keine Clans");
                return true;
            }
            Set<String> keys = Main.clans.keySet();
            StringJoiner joiner = new StringJoiner(", ");
            for (String key : keys) {
                String keyInBrackets = "[" + key + "]";
                joiner.add(keyInBrackets);
            }


            p.sendMessage("Es gibt "+ Main.clans.size() + " Clans: " + joiner);
            return true;


        } else if(args[0].equalsIgnoreCase("modify")){
            if(p.isOp()){
                if(args[1].equalsIgnoreCase("Admin")) {
                    if(!(args.length == 4)){
                        p.sendMessage("/clan modify admin <clan> <player>");
                        return true;
                    }
                    if(!Main.clans.containsKey(args[2])){
                        p.sendMessage("Clan existiert nicht");
                        return true;
                    }
                    Player admin = Bukkit.getPlayer(args[3]);
                    if(admin == null){
                        p.sendMessage("Spieler ist nicht online");
                        return true;
                    }
                    Clan clan = Main.clans.get(args[2]);
                    clan.setAdminId(admin.getUniqueId().toString());
                    p.sendMessage(admin.getName() +  " ist nun der neue Admin von " + clan.getPrefix());
                    return true;

                }



            }



            if(args[1].equalsIgnoreCase("color")) {
                if (!isAdmin(p)) {
                    sender.sendMessage(ChatColor.RED + "Nur der Admin eines Clans kann die Farbe ändern!");
                    return true;
                }
                ChatColor color;
                try {
                    color = ChatColor.valueOf(args[2].toUpperCase());
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ChatColor.RED + "Ungültige Farbe!");
                    return true;
                }
                if (color.name().equalsIgnoreCase("BOLD") || color.name().equalsIgnoreCase("ITALIC")) {
                    sender.sendMessage(ChatColor.RED + "Ungültige Farbe!");
                    return true;
                }
                if (color.name().equalsIgnoreCase("MAGIC") || color.name().equalsIgnoreCase("STRIKETHROUGH")) {
                    sender.sendMessage(ChatColor.RED + "Ungültige Farbe!");
                    return true;
                }
                if (color.name().equalsIgnoreCase("UNDERLINE")) {
                    sender.sendMessage(ChatColor.RED + "Ungültige Farbe!");
                    return true;
                }


                if (color.equals(ChatColor.DARK_RED) || color.equals(ChatColor.RED)) {
                    sender.sendMessage(ChatColor.RED + "Rot und Dunkel Rot dürfen nicht verwendet werden.");
                    return true;
                }

                Main.board.getTeam(getClanByPlayer(p).getPrefix()).setColor(color);
                sender.sendMessage(ChatColor.GREEN + "Die Farbe des Teams wurde erfolgreich geändert.");
                return true;
            }






            //op

        }

        return true;
    }





    public static Clan getClanByPlayer(Player p){
        for (Map.Entry<String, Clan> entry : Main.clans.entrySet()) {
            String k = entry.getKey();
            Clan v = entry.getValue();
            if (v.getMemberList().contains(p.getUniqueId().toString())) {
                return v;
            }
        }
        return null;
    }

    public static boolean isAdmin(Player p){
        for (Map.Entry<String, Clan> entry : Main.clans.entrySet()) {
            String k = entry.getKey();
            Clan v = entry.getValue();
            if (Objects.equals(v.getAdminId(), p.getUniqueId().toString())) {
                return true;
            }
        }
        return false;
    }
    public static boolean isClan(Player p){
        for (Map.Entry<String, Clan> entry : Main.clans.entrySet()) {
            String k = entry.getKey();
            Clan v = entry.getValue();
            if (v.getMemberList().contains(p.getUniqueId().toString())) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player p = (Player) sender;

        ArrayList<String> list = new ArrayList<>();
        if(args.length == 0) return list;
        if(args.length == 1){
            list.add("create");
            list.add("delete");
            list.add("invite");
            list.add("kick");
            list.add("leave");
            list.add("decline");
            list.add("help");
            list.add("info");
            list.add("list");
            list.add("modify");
            list.add("accept");
        } else if (args[0].equalsIgnoreCase("invite")){
            if(args.length > 2) return list;
            for (Player player : Bukkit.getServer().getOnlinePlayers()){
                list.add(player.getName());
            }
        }else if (args[0].equalsIgnoreCase("kick")){
            if(args.length > 2) return list;
            for (Player player : Bukkit.getServer().getOnlinePlayers()){
                list.add(player.getName());
            }

        } else if (args[0].equalsIgnoreCase("modify")){
            if(args.length == 2){
                list.add("color");


                if(p.isOp()){
                    list.add("admin");
                }
            } else if (args[1].equalsIgnoreCase("color")){
                for (ChatColor color : ChatColor.values()) {
                    if(color.name().equalsIgnoreCase("red") || color.name().equalsIgnoreCase("dark_red")) continue;
                    if(color.name().equalsIgnoreCase("BOLD") || color.name().equalsIgnoreCase("ITALIC")) continue;
                    if(color.name().equalsIgnoreCase("MAGIC") || color.name().equalsIgnoreCase("STRIKETHROUGH")) continue;
                    if(color.name().equalsIgnoreCase("UNDERLINE")) continue;
                    list.add(color.name().toLowerCase());
                }

            }


        }




        ArrayList<String> completeList = new ArrayList<>();
        String arg = args[args.length -1];
        for (String s : list){
            if(s.startsWith(arg.toLowerCase())){
                completeList.add(s);
            }
        }

        return completeList;
    }
}


