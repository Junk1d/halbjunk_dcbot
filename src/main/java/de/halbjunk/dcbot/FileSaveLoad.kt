package de.halbjunk.dcbot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import de.halbjunk.dcbot.clans.Clan;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scoreboard.Team;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileSaveLoad {
    public static void mapSaver(Map map, String nameMap) throws IOException {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(Main.file);
        config.set( nameMap , map);
        config.save(Main.file);
    }
    public static HashMap maploader(String nameMap){
        YamlConfiguration config = YamlConfiguration.loadConfiguration(Main.file);
        ConfigurationSection section = config.getConfigurationSection(nameMap);
        if (section == null) return new HashMap<>();
        Map<String, Object> loadedMap = section.getValues(false);
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<String, Object> entry : loadedMap.entrySet()) {
            String key = entry.getKey();
            String value = (String) entry.getValue();
            map.put(key, value);
        }
        return (HashMap) map;
    }

    public static void dcIdsSaver() throws IOException {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(Main.file);
        config.set( "dcToMcID" , Main.dcToMcID);
        config.save(Main.file);
    }


    public static void dcIdsLoader(){
        YamlConfiguration config = YamlConfiguration.loadConfiguration(Main.file);
        ConfigurationSection section = config.getConfigurationSection("dcToMcID");
        if (section == null){
            Main.dcToMcID = new HashMap<>();
            return;
        }
        Map<String, Object> loadedMap = section.getValues(false);
        HashMap<String, String> map = new HashMap<>();
        for (Map.Entry<String, Object> entry : loadedMap.entrySet()) {
            String key = entry.getKey();
            String value = (String) entry.getValue();

            for (OfflinePlayer player : Main.getPlugin().getServer().getWhitelistedPlayers()) {
                if( player.getUniqueId().toString().equalsIgnoreCase(value)){
                    map.put(key, value);
                }
            }
        }
        for (OfflinePlayer player : Main.getPlugin().getServer().getWhitelistedPlayers()) {
            if(!map.containsValue(player.getUniqueId().toString())){
                player.setWhitelisted(false);
            }
        }

        Main.dcToMcID = map;
    }




    public static void clanSaver() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

// Konvertiere die Clan-Liste in einen JSON-String
        String jsonString = gson.toJson(Main.clans);

// Speichere den JSON-String in einer Datei
        try (FileWriter fileWriter = new FileWriter(Main.getPlugin().getDataFolder() + "/clans.json")) {
            fileWriter.write(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void clanLoader() throws IOException {
        Gson gson = new GsonBuilder().create();

        // Lese den JSON-String aus der Datei
        String jsonString = "";
        try (FileReader fileReader = new FileReader(Main.getPlugin().getDataFolder() + "/clans.json")) {
            int character;
            while ((character = fileReader.read()) != -1) {
                jsonString += (char) character;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, LinkedTreeMap> map = gson.fromJson(jsonString, new HashMap<String, LinkedTreeMap>().getClass());

        if(map == null){
            Main.clans = new HashMap<>();
            return;
        }

        for (Map.Entry<String, LinkedTreeMap> entry : map.entrySet()) {
            String key = entry.getKey();
            LinkedTreeMap value = entry.getValue();



            Clan clan = new Clan();
            clan.setName((String) value.get("name"));
            clan.setDcCategoryId((String) value.get("dcCategoryId"));

            clan.setRoleId((String) value.get("roleId"));
            clan.setMemberList((List<String>) value.get("memberList"));
            clan.setAdminId((String) value.get("adminId"));
            clan.setModerators((List<String>) value.get("moderators"));
            clan.setPrefix((String) value.get("prefix"));




            Main.clans.put(key, clan);

            if(Main.board.getTeam(clan.getPrefix()) == null){
                clan.delete();
                System.err.println(value.get("prefix") + " wurde gelöscht, da kein Team mit diesem Namen existiert");
            }

        }

        for (Team team: Main.board.getTeams()){
            if(!Main.clans.containsKey(team.getName())){
                team.unregister();
                System.err.println(team.getName() + " wurde gelöscht, da kein Clan mit diesem Namen existiert");
                System.err.println("Möglicherweise existiert noch der DC Rang und Channel");
            }
        }
    }


    public static void listsaver(List list, String nameList) throws IOException {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(Main.file);
        config.set(nameList, list);
        config.save(Main.file);
    }
    public static List listLoader(String nameList){
        YamlConfiguration config = YamlConfiguration.loadConfiguration(Main.file);
        return config.getStringList(nameList);
    }




}
