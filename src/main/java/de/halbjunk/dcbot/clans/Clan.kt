package de.halbjunk.dcbot.clans

import de.halbjunk.dcbot.Main
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import org.bukkit.entity.Player
import java.util.*

class Clan {
    var name: String? = null
    var dcCategoryId: String? = null
//    fun getMemberList(): List<String> {
//        return memberList
//    }

    fun addMemberList(memberId: String) {
        memberList.add(memberId)
    }

//    fun setMemberList(memberList: MutableList<String>) {
//        this.memberList = memberList
//    }

    var roleId: String? = null
    public var memberList: MutableList<String> = ArrayList()
    @JvmField
    var adminId: String? = null
    private val adminName: String? = null
    var moderators: List<String> = ArrayList()
    @JvmField
    var prefix: String? = null

    constructor()
    constructor(prefix: String, admin: Player) {
        name = prefix
        adminId = admin.uniqueId.toString()
        this.prefix = prefix
        memberList.add(admin.uniqueId.toString())
        val team = Main.board!!.registerNewTeam(prefix)
        team.prefix = "[$prefix]"
        team.addEntry(admin.name)
        //admin.addAttachment(Main.plugin, prefix + ".clan.admin" , true);
        Main.clans[prefix] = this
        val guild = Main.bot!!.getGuildById("1092435763502796912") // Ersetze DEINE_GUILD_ID durch die ID deines Servers
        val category = guild!!.createCategory(name!!.uppercase(Locale.getDefault()) + " CLAN").complete() // Erstelle die Kategorie
        dcCategoryId = category.id
        val role = guild.createRole()
                .setName(prefix)
                .complete() // Erstelle die Rolle
        roleId = role.id
        val textChannel = guild.createTextChannel("$prefix Text")
                .setParent(category) // Setze die Kategorie als Elternkanal
                .addPermissionOverride(role,  // Erlaube der Rolle Zugriff
                        EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND),  // Erlaube das Anzeigen und Schreiben von Nachrichten
                        null) // Keine Berechtigungen für andere Benutzer festgelegt
                .addPermissionOverride(guild.publicRole,  // Verweigere Berechtigungen für die @everyone-Rolle
                        null,  // Keine Berechtigungen für die @everyone-Rolle festgelegt
                        EnumSet.of(Permission.VIEW_CHANNEL)) // Verweigere das Anzeigen des Kanals für alle anderen Benutzer
                .complete()
        val voiceChannel = guild.createVoiceChannel("$prefix Talk")
                .setParent(category) // Setze die Kategorie als Elternkanal
                .addPermissionOverride(role,  // Erlaube der Rolle Zugriff
                        EnumSet.of(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT),  // Erlaube das Anzeigen und Verbinden zum Sprachkanal
                        null) // Keine Berechtigungen für andere Benutzer festgelegt
                .addPermissionOverride(guild.publicRole,  // Verweigere Berechtigungen für die @everyone-Rolle
                        null,  // Keine Berechtigungen für die @everyone-Rolle festgelegt
                        EnumSet.of(Permission.VIEW_CHANNEL)) // Verweigere das Anzeigen des Kanals für alle anderen Benutzer
                .complete()
        val userId = getKeyByValue(Main.dcToMcID, admin.uniqueId.toString())!!
        guild.retrieveMemberById(userId).queue { member: Member? -> guild.addRoleToMember(member!!, role).queue() } //,
        //error -> System.err.println("Benutzer nicht gefunden mit ID: " + userId)

        admin.sendMessage(this.prefix + " wurde erstellt")
    }

    fun leave(player: Player) {
        val guild = Main.bot!!.getGuildById("1092435763502796912")
        val userId = getKeyByValue(Main.dcToMcID, player.uniqueId.toString())!!
        guild!!.retrieveMemberById(userId).queue(
                { member: Member? ->
                    guild.removeRoleFromMember(member!!, guild.getRoleById(roleId!!)!!).queue(
                            { success: Void? -> println("Rolle erfolgreich vom Benutzer mit ID: $userId weggenommen") }
                    ) { error: Throwable -> System.err.println("Fehler beim wegnehmen der Rolle: " + error.message) }
                }
        ) { error: Throwable? -> System.err.println("Benutzer nicht gefunden mit ID: $userId") }
        Main.board!!.getTeam(prefix!!)!!.removeEntry(player.name)
        memberList.remove(player.uniqueId.toString())
    }

    fun join(player: Player) {
        memberList.add(player.uniqueId.toString())
        Main.board!!.getTeam(prefix!!)!!.addEntry(player.name)
        val guild = Main.bot!!.getGuildById("1092435763502796912")
        val userId = getKeyByValue(Main.dcToMcID, player.uniqueId.toString())!!
        guild!!.retrieveMemberById(userId).queue(
                { member: Member? ->
                    guild.addRoleToMember(member!!, guild.getRoleById(roleId!!)!!).queue(
                            { success: Void? -> println("Rolle erfolgreich zugewiesen an Benutzer mit ID: $userId") }
                    ) { error: Throwable -> System.err.println("Fehler beim Zuweisen der Rolle: " + error.message) }
                }
        ) { error: Throwable? -> System.err.println("Benutzer nicht gefunden mit ID: $userId") }
    }

    fun delete() {
        val guild = Main.bot!!.getGuildById("1092435763502796912")
        guild!!.getRoleById(roleId!!)!!.delete().queue()
        val category = guild.getCategoryById(dcCategoryId!!)
        if (category != null) {
            val textChannels = category.textChannels
            for (textChannel in textChannels) {
                textChannel.delete().queue()
            }
            val voiceChannels = category.voiceChannels
            for (voiceChannel in voiceChannels) {
                voiceChannel.delete().queue()
            }
            category.delete().queue()
        }
        if (Main.board!!.getTeam(prefix!!) != null) {
            Main.board!!.getTeam(prefix!!)!!.unregister()
        }
        Main.clans.remove(prefix)
    }

    companion object {
        @JvmStatic
        fun isAdmin(p: Player): Boolean {
            for ((k, v) in Main.clans) {
                if (v.adminId == p.uniqueId.toString()) {
                    return true
                }
            }
            return false
        }

        fun <K, V> getKeyByValue(map: Map<K, V>, value: V): K? {
            for ((key, value1) in map) {
                if (value == value1) {
                    return key
                }
            }
            return null
        }
    }
}
