package nu.nerd.trampoline;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

// ----------------------------------------------------------------------------
/**
 * A Runnable task that checks player locations for falls into the void and
 * processes them according to the configuration.
 * 
 * The task also teleports entities of configured types in worlds that send to
 * the sky of other worlds.
 */
public class MovementTask implements Runnable {
    // ------------------------------------------------------------------------
    /**
     * Schedule this task to run (again) in the future.
     */
    public void schedule() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(Trampoline.PLUGIN, this, Trampoline.CONFIG.CHECK_TICKS);
    }

    // ------------------------------------------------------------------------
    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        Logger logger = Trampoline.PLUGIN.getLogger();

        for (Player player : Bukkit.getOnlinePlayers()) {
            Location loc = player.getLocation();
            World world = loc.getWorld();
            HandleSendTo handler = Trampoline.CONFIG.SEND_TO_HANDLERS.get(world.getName());
            if (handler != null && handler.isEnabled()) {
                handler.handle(player, logger);
            }
        }

        for (HandleSendTo sendTo : Trampoline.CONFIG.SEND_TO_HANDLERS.values()) {
            if (sendTo.isEnabled() && sendTo instanceof HandleSendToSky) {
                HandleSendToSky sendToSky = (HandleSendToSky) sendTo;
                sendToSky.teleportEntities(logger);
            }
        }
        schedule();
    }
} // class MovementTask