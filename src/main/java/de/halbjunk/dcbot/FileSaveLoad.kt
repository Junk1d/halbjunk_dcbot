package de.halbjunk.dcbot

import com.google.gson.GsonBuilder
import com.google.gson.internal.LinkedTreeMap
import de.halbjunk.dcbot.clans.Clan
import org.bukkit.configuration.file.YamlConfiguration
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

object FileSaveLoad {
    @Throws(IOException::class)
    fun mapSaver(map: Map<*, *>?, nameMap: String?) {
        val config = YamlConfiguration.loadConfiguration(Main.file!!)
        config[nameMap!!] = map
        config.save(Main.file!!)
    }

    fun maploader(nameMap: String?): HashMap<*, *> {
        val config = YamlConfiguration.loadConfiguration(Main.file!!)
        val section = config.getConfigurationSection(nameMap!!) ?: return HashMap<Any, Any>()
        val loadedMap = section.getValues(false)
        val map: MutableMap<String, String> = HashMap()
        for ((key, value1) in loadedMap) {
            val value = value1 as String
            map[key] = value
        }
        return map as HashMap<*, *>
    }

    @Throws(IOException::class)
    fun dcIdsSaver() {
        val config = YamlConfiguration.loadConfiguration(Main.file!!)
        config["dcToMcID"] = Main.dcToMcID
        config.save(Main.file!!)
    }

    fun dcIdsLoader() {
        val config = YamlConfiguration.loadConfiguration(Main.file!!)
        val section = config.getConfigurationSection("dcToMcID")
        if (section == null) {
            Main.dcToMcID = HashMap()
            return
        }
        val loadedMap = section.getValues(false)
        val map = HashMap<String, String>()
        for ((key, value1) in loadedMap) {
            val value = value1 as String
            for (player in Main.plugin!!.server.whitelistedPlayers) {
                if (player.uniqueId.toString().equals(value, ignoreCase = true)) {
                    map[key] = value
                }
            }
        }
        for (player in Main.plugin!!.server.whitelistedPlayers) {
            if (!map.containsValue(player.uniqueId.toString())) {
                player.isWhitelisted = false
            }
        }
        Main.dcToMcID = map
    }

    @Throws(IOException::class)
    fun clanSaver() {
        val gson = GsonBuilder().setPrettyPrinting().create()

// Konvertiere die Clan-Liste in einen JSON-String
        val jsonString = gson.toJson(Main.clans)

// Speichere den JSON-String in einer Datei
        try {
            FileWriter(Main.plugin!!.dataFolder.toString() + "/clans.json").use { fileWriter -> fileWriter.write(jsonString) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    fun clanLoader() {
        val gson = GsonBuilder().create()

        // Lese den JSON-String aus der Datei
        var jsonString = ""
        try {
            FileReader(Main.plugin!!.dataFolder.toString() + "/clans.json").use { fileReader ->
                var character: Int
                while (fileReader.read().also { character = it } != -1) {
                    jsonString += character.toChar()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val map: Map<String, LinkedTreeMap<*, *>>? = gson.fromJson(jsonString, HashMap<String, LinkedTreeMap<*, *>>().javaClass)
        if (map == null) {
            Main.clans = HashMap()
            return
        }
        for ((key, value) in map) {
            val clan = Clan()
            clan.name = value["name"] as String?
            clan.dcCategoryId = value["dcCategoryId"] as String?
            clan.roleId = value["roleId"] as String?
            clan.memberList = value["memberList"] as MutableList<String>
            clan.adminId = value["adminId"] as String?
            clan.moderators = value["moderators"] as List<String>
            clan.prefix = value["prefix"] as String?
            Main.clans[key] = clan
            if (clan.prefix?.let { Main.board!!.getTeam(it) } == null) {
                clan.delete()
                System.err.println(value["prefix"].toString() + " wurde gelöscht, da kein Team mit diesem Namen existiert")
            }
        }
        for (team in Main.board!!.teams) {
            if (!Main.clans.containsKey(team.name)) {
                team.unregister()
                System.err.println(team.name + " wurde gelöscht, da kein Clan mit diesem Namen existiert")
                System.err.println("Möglicherweise existiert noch der DC Rang und Channel")
            }
        }
    }

    @Throws(IOException::class)
    fun listsaver(list: List<*>?, nameList: String?) {
        val config = YamlConfiguration.loadConfiguration(Main.file!!)
        config[nameList!!] = list
        config.save(Main.file!!)
    }

    fun listLoader(nameList: String?): List<*> {
        val config = YamlConfiguration.loadConfiguration(Main.file!!)
        return config.getStringList(nameList!!)
    }
}
