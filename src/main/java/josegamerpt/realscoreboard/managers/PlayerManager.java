package josegamerpt.realscoreboard.managers;

import josegamerpt.realscoreboard.RealScoreboard;
import josegamerpt.realscoreboard.scoreboard.ScoreboardTask;
import josegamerpt.realscoreboard.config.PlayerData;
import josegamerpt.realscoreboard.config.Config;
import josegamerpt.realscoreboard.utils.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.MetadataValue;

import java.util.HashMap;
import java.util.UUID;

public class PlayerManager implements Listener {

    private RealScoreboard rs;

    public PlayerManager(RealScoreboard rs)
    {
        this.rs = rs;
    }

    private HashMap<UUID, ScoreboardTask> tasks = new HashMap<>();

    public void check(Player p) {
        if (Config.file().getList("Config.Bypass-Worlds").contains(p.getWorld().getName())) {
            if (this.tasks.containsKey(p.getUniqueId())) {
                this.tasks.get(p.getUniqueId()).cancel();
            }
            this.tasks.remove(p.getUniqueId());
        } else {
            if (Config.file().getBoolean("Config.RealScoreboard-Disabled-By-Default")) {
                PlayerData playerData = RealScoreboard.getInstance().getDatabaseManager().getPlayerData(p.getUniqueId());
                playerData.setScoreboardON(false);
                RealScoreboard.getInstance().getDatabaseManager().savePlayerData(playerData, true);
            }
            this.tasks.put(p.getUniqueId(), new ScoreboardTask(p, this.rs));
            this.tasks.get(p.getUniqueId()).runTaskTimerAsynchronously(RealScoreboard.getInstance(), Config.file().getInt("Config.Scoreboard-Refresh"), Config.file().getInt("Config.Scoreboard-Refresh"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void join(PlayerJoinEvent e) {
        check(e.getPlayer());
        if (e.getPlayer().isOp() && RealScoreboard.newUpdate) {
            Text.send(e.getPlayer(), "&6&lWARNING &fThere is a new version of RealScoreboard! https://www.spigotmc.org/resources/realscoreboard-1-13-to-1-19-2.22928/");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void changeWorld(PlayerChangedWorldEvent e) {
        check(e.getPlayer());
    }

    private String[] vanishCommands = {"/pv", "/vanish", "/premiumvanish"};

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();

        for (String cmd : vanishCommands) {
            if (e.getMessage().toLowerCase().startsWith(cmd)) {
                if (isVanished(p))
                {
                    PlayerData playerData = RealScoreboard.getInstance().getDatabaseManager().getPlayerData(p.getUniqueId());
                    playerData.setScoreboardON(false);
                    RealScoreboard.getInstance().getDatabaseManager().savePlayerData(playerData, true);
                } else {
                    PlayerData playerData = RealScoreboard.getInstance().getDatabaseManager().getPlayerData(p.getUniqueId());
                    playerData.setScoreboardON(true);
                    RealScoreboard.getInstance().getDatabaseManager().savePlayerData(playerData, true);
                }
            }
        }
    }

    private boolean isVanished(Player player) {
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }
        return false;
    }

    public HashMap<UUID, ScoreboardTask> getTasks() {
        return this.tasks;
    }
}
