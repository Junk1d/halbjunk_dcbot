package de.halbjunk.dcbot.clans

import de.halbjunk.dcbot.Main
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.*

class ClanMCCommand : CommandExecutor, TabCompleter {
    private val clanInvitations: MutableMap<String, Clan?> = HashMap()
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) return false
        val p = sender
        if (args.size == 0) {
            p.sendMessage("  ")
            p.sendMessage("/clan create")
            p.sendMessage("/clan delete")
            p.sendMessage("/clan invite <spielername>")
            p.sendMessage("/clan kick <spielername>")
            p.sendMessage("/clan leave")
            p.sendMessage("  ")
        } else if (args[0].equals("delete", ignoreCase = true)) {
            if (args.size > 2) {
                sender.sendMessage("/clan delete")
                return true
            }
            if (args.size == 2) {
                if (!sender.isOp()) {
                    sender.sendMessage("/clan delete")
                    return true
                }
                if (!Main.clans.containsKey(args[1])) {
                    sender.sendMessage("Dieser Clan existiert noch nicht")
                    return true
                }

                //delete
                Main.clans[args[1]]!!.delete()
                return true
            }
            if (!isClan(p)) {
                sender.sendMessage("Du bist in keinem Clan")
                return true
            }
            if (!Clan.isAdmin(p)) {
                sender.sendMessage("Nur der Admin des Clans kann diesen auch Löschen")
                return true
            }
            //delete
            getClanByPlayer(p)!!.delete()
            return true
        } else if (args[0].equals("leave", ignoreCase = true)) {
            if (!isClan(p)) {
                sender.sendMessage("Du bist in keinem Clan")
                return true
            }
            if (isAdmin(p)) {
                sender.sendMessage("Um den Clan zu verlassen musst du ihn löschen")
                return true
            }
            getClanByPlayer(p)!!.leave(p)
            return true
        } else if (args[0].equals("kick", ignoreCase = true)) {
            if (args.size < 2) {
                p.sendMessage("Bitte gib den Namen des Spielers ein, den du kicken möchtest.")
                return true
            }
            if (getClanByPlayer(p)!!.adminId != p.uniqueId.toString()) {
                sender.sendMessage("Du bist kein Admin von einem Clan")
                return true
            }
            var player = Bukkit.getPlayer(args[1])
            if (player == null) {
                for (offlinePlayer in Bukkit.getServer().whitelistedPlayers) {
                    if (offlinePlayer.name.equals(args[1], ignoreCase = true)) {
                        player = offlinePlayer as Player
                        break
                    }
                }
            }
            if (player == null) {
                sender.sendMessage("Dieser Spieler existiert auf diesem Server nicht")
                return true
            }
            if (p.uniqueId === player.uniqueId) {
                sender.sendMessage("Du kannst dich nicht selber Kicken")
                return true
            }
            if (getClanByPlayer(player) != getClanByPlayer(p)) {
                sender.sendMessage(args[1] + " ist nicht in deinem Clan")
                return true
            }
            getClanByPlayer(p)!!.leave(player)
            return true
        } else if (args[0].equals("invite", ignoreCase = true)) {
            if (!isAdmin(p)) {
                p.sendMessage("Aktuelle kann nur der Admin des Clans Leute einladen")
                return true
            }
            if (args.size < 2) {
                p.sendMessage("Bitte gib den Namen des Spielers ein, den du in deinen Clan einladen möchtest.")
                return true
            }
            val invitedPlayer = Bukkit.getPlayer(args[1])
            if (invitedPlayer == null) {
                p.sendMessage("Der eingeladene Spieler ist nicht online.")
                return true
            }

            // Überprüfen, ob der eingeladene Spieler bereits eine Einladung erhalten hat
            if (clanInvitations.containsKey(invitedPlayer.uniqueId.toString())) {
                p.sendMessage("Der eingeladene Spieler hat bereits eine ausstehende Einladung.")
                return true
            }
            val clan = getClanByPlayer(p)
            // Speichern der Einladung
            clanInvitations[invitedPlayer.uniqueId.toString()] = clan
            p.sendMessage("Du hast " + invitedPlayer.name + " erfolgreich in deinen Clan eingeladen.")
            invitedPlayer.sendMessage("Du hast eine Einladung erhalten, dem Clan " + clan!!.prefix + " beizutreten.")
            invitedPlayer.sendMessage("Um die Einladung anzunehmen, verwende den Befehl: /clan accept")
            invitedPlayer.sendMessage("Um die Einladung abzulehnen, verwende den Befehl: /clan decline")
            return true
        } else if (args[0].equals("accept", ignoreCase = true)) {
            if (!clanInvitations.containsKey(p.uniqueId.toString())) {
                p.sendMessage("Du hast keine Einladung")
                return true
            }
            if (isClan(p)) {
                p.sendMessage("Du bist schon in einem Clan")
                return true
            }
            val clan = clanInvitations.remove(p.uniqueId.toString())
            clan!!.join(p)
        } else if (args[0].equals("decline", ignoreCase = true)) {
            if (!clanInvitations.containsKey(p.uniqueId.toString())) {
                p.sendMessage("Du hast keine Einladung")
                return true
            }
            clanInvitations.remove(p.uniqueId.toString())
            p.sendMessage("Du hast die Einladung abgelehnt")
            return true
        } else if (args[0].equals("create", ignoreCase = true)) {
            if (isClan(p)) {
                sender.sendMessage("Du musst erst dein aktuellen Clan verlassen")
                return true
            }
            if (args.size == 1) {
                sender.sendMessage("/clan create <clan kürzel>")
                return true
            }
            if (args[1].length > 6 || args[1].length < 2) {
                sender.sendMessage("Der Kürzel darf nur 2 bis 6 Zeichen betragen")
                return true
            }
            val containsKeyIgnoreCase = Main.clans.keys.stream()
                    .anyMatch { key: String -> key.equals(args[1], ignoreCase = true) }
            if (containsKeyIgnoreCase) {
                sender.sendMessage("Es existiert schon ein Clan namens " + args[1])
                return true
            }
            Clan(args[1], p)
            return true
        } else if (args[0].equals("help", ignoreCase = true)) {
        } else if (args[0].equals("info", ignoreCase = true)) {
        } else if (args[0].equals("list", ignoreCase = true)) {
            if (Main.clans.isEmpty()) {
                p.sendMessage("Es gibt noch keine Clans")
                return true
            }
            val keys: Set<String> = Main.clans.keys
            val joiner = StringJoiner(", ")
            for (key in keys) {
                val keyInBrackets = "[$key]"
                joiner.add(keyInBrackets)
            }
            p.sendMessage("Es gibt " + Main.clans.size + " Clans: " + joiner)
            return true
        } else if (args[0].equals("modify", ignoreCase = true)) {
            if (p.isOp) {
                if (args[1].equals("Admin", ignoreCase = true)) {
                    if (args.size != 4) {
                        p.sendMessage("/clan modify admin <clan> <player>")
                        return true
                    }
                    if (!Main.clans.containsKey(args[2])) {
                        p.sendMessage("Clan existiert nicht")
                        return true
                    }
                    val admin = Bukkit.getPlayer(args[3])
                    if (admin == null) {
                        p.sendMessage("Spieler ist nicht online")
                        return true
                    }
                    val clan = Main.clans[args[2]]
                    clan!!.adminId = admin.uniqueId.toString()
                    p.sendMessage(admin.name + " ist nun der neue Admin von " + clan.prefix)
                    return true
                }
            }
            if (args[1].equals("color", ignoreCase = true)) {
                if (!isAdmin(p)) {
                    sender.sendMessage(ChatColor.RED.toString() + "Nur der Admin eines Clans kann die Farbe ändern!")
                    return true
                }
                val color: ChatColor
                color = try {
                    ChatColor.valueOf(args[2].uppercase(Locale.getDefault()))
                } catch (e: IllegalArgumentException) {
                    sender.sendMessage(ChatColor.RED.toString() + "Ungültige Farbe!")
                    return true
                }
                if (color.name.equals("BOLD", ignoreCase = true) || color.name.equals("ITALIC", ignoreCase = true)) {
                    sender.sendMessage(ChatColor.RED.toString() + "Ungültige Farbe!")
                    return true
                }
                if (color.name.equals("MAGIC", ignoreCase = true) || color.name.equals("STRIKETHROUGH", ignoreCase = true)) {
                    sender.sendMessage(ChatColor.RED.toString() + "Ungültige Farbe!")
                    return true
                }
                if (color.name.equals("UNDERLINE", ignoreCase = true)) {
                    sender.sendMessage(ChatColor.RED.toString() + "Ungültige Farbe!")
                    return true
                }
                if (color == ChatColor.DARK_RED || color == ChatColor.RED) {
                    sender.sendMessage(ChatColor.RED.toString() + "Rot und Dunkel Rot dürfen nicht verwendet werden.")
                    return true
                }
                Main.board!!.getTeam(getClanByPlayer(p)!!.prefix!!)!!.color = color
                sender.sendMessage(ChatColor.GREEN.toString() + "Die Farbe des Teams wurde erfolgreich geändert.")
                return true
            }


            //op
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<String>): List<String>? {
        val p = sender as Player
        val list = ArrayList<String>()
        if (args.size == 0) return list
        if (args.size == 1) {
            list.add("create")
            list.add("delete")
            list.add("invite")
            list.add("kick")
            list.add("leave")
            list.add("decline")
            list.add("help")
            list.add("info")
            list.add("list")
            list.add("modify")
            list.add("accept")
        } else if (args[0].equals("invite", ignoreCase = true)) {
            if (args.size > 2) return list
            for (player in Bukkit.getServer().onlinePlayers) {
                list.add(player.name)
            }
        } else if (args[0].equals("kick", ignoreCase = true)) {
            if (args.size > 2) return list
            for (player in Bukkit.getServer().onlinePlayers) {
                list.add(player.name)
            }
        } else if (args[0].equals("modify", ignoreCase = true)) {
            if (args.size == 2) {
                list.add("color")
                if (p.isOp) {
                    list.add("admin")
                }
            } else if (args[1].equals("color", ignoreCase = true)) {
                for (color in ChatColor.entries) {
                    if (color.name.equals("red", ignoreCase = true) || color.name.equals("dark_red", ignoreCase = true)) continue
                    if (color.name.equals("BOLD", ignoreCase = true) || color.name.equals("ITALIC", ignoreCase = true)) continue
                    if (color.name.equals("MAGIC", ignoreCase = true) || color.name.equals("STRIKETHROUGH", ignoreCase = true)) continue
                    if (color.name.equals("UNDERLINE", ignoreCase = true)) continue
                    list.add(color.name.lowercase(Locale.getDefault()))
                }
            }
        }
        val completeList = ArrayList<String>()
        val arg = args[args.size - 1]
        for (s in list) {
            if (s.startsWith(arg.lowercase(Locale.getDefault()))) {
                completeList.add(s)
            }
        }
        return completeList
    }

    companion object {
        @JvmStatic
        fun getClanByPlayer(p: Player): Clan? {
            for ((k, v) in Main.clans) {
                if (v.memberList.contains(p.uniqueId.toString())) {
                    return v
                }
            }
            return null
        }

        @JvmStatic
        fun isAdmin(p: Player): Boolean {
            for ((k, v) in Main.clans) {
                if (v.adminId == p.uniqueId.toString()) {
                    return true
                }
            }
            return false
        }

        @JvmStatic
        fun isClan(p: Player): Boolean {
            for ((k, v) in Main.clans) {
                if (v.memberList.contains(p.uniqueId.toString())) {
                    return true
                }
            }
            return false
        }
    }
}
