package de.halbjunk.dcbot.dcEvent;

import de.halbjunk.dcbot.Main;
import de.halbjunk.dcbot.clans.ClanMCCommand;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class GuildMemberRemove extends ListenerAdapter {
    public void onGuildMemberRemove (GuildMemberRemoveEvent event){
        if(Main.dcToMcID.containsKey(event.getUser().getId())){
            Server server = Bukkit.getServer();
            System.out.println(server.getWhitelistedPlayers());
            OfflinePlayer player = null;
            for (OfflinePlayer offlinePlayer : server.getWhitelistedPlayers()) {
                if (offlinePlayer.getUniqueId().toString().equals(Main.dcToMcID.get(event.getUser().getId()))) {
                    player = offlinePlayer;
                    break;
                }
            }

            if (player != null) {
                player.setWhitelisted(false);
                Bukkit.reloadWhitelist();
            }
            Main.dcToMcID.remove(event.getUser().getId());
            if(ClanMCCommand.isClan((Player) player)){
                if (ClanMCCommand.isAdmin((Player) player)){
                    ClanMCCommand.getClanByPlayer((Player) player).delete();
                } else {
                    ClanMCCommand.getClanByPlayer((Player) player).leave((Player) player);
                }
            }

        }

    }
}
