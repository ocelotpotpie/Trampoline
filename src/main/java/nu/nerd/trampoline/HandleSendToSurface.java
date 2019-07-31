package nu.nerd.trampoline;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

// ----------------------------------------------------------------------------
/**
 * Handle sending the player to the surface in the current world.
 */
public class HandleSendToSurface extends HandleSendTo {
    // --------------------------------------------------------------------------
    /**
     * @see nu.nerd.trampoline.HandleSendTo#load(org.bukkit.configuration.ConfigurationSection,
     *      java.util.logging.Logger)
     */
    @Override
    public boolean load(ConfigurationSection section, Logger logger) {
        super.load(section, logger);
        ConfigurationSection safeScan = section.getConfigurationSection("surface.safe-scan");
        if (safeScan != null) {
            _safeScanEnabled = safeScan.getBoolean("enabled");
            _safeScanStep = Math.max(1, safeScan.getInt("step"));
            _safeScanTries = safeScan.getInt("tries");
        }
        return true;
    }

    // ------------------------------------------------------------------------
    /**
     * @see nu.nerd.trampoline.HandleSendTo#handle(org.bukkit.entity.Player)
     */
    @Override
    public void handle(Player player, Logger logger) {
        Location loc = player.getLocation();
        if (loc.getY() <= Trampoline.CONFIG.TELEPORT_Y) {
            Location safeLoc = findSafeLocation(loc, logger);

            // Don't dump the player at the bottom of the ocean either!
            while (safeLoc.getBlock().getType() == Material.WATER) {
                safeLoc.add(0, 1, 0);
            }
            Location destination = Util.centredXZ(safeLoc);

            // Bounce the player upwards slightly.
            player.setVelocity(new Vector(0, 0.1, 0));
            player.setFallDistance(0);
            player.teleport(destination);
            logger.info(player.getName() + " teleported from " +
                        Util.formatLocation(loc) + " to " +
                        Util.formatLocation(destination) + ".");

            player.sendMessage(ChatColor.DARK_AQUA + "Well, that was awkward!");
            player.sendMessage(ChatColor.DARK_AQUA + "You shouldn't be able to fall out of this world.");
            player.sendMessage(ChatColor.DARK_AQUA + "Let us put you here instead!");

            // If the player fell out of the world because of ModMode+Spectator
            if (player.getGameMode() == GameMode.SPECTATOR) {
                player.sendMessage(ChatColor.DARK_AQUA + "Looks like spectator mode might be the issue. Let's fix that too.");
                player.setGameMode(GameMode.SURVIVAL);
                logger.info(player.getName() + " had their game mode reset from spectator to survival.");
            }
        }
    }

    // ------------------------------------------------------------------------
    /**
     * @see nu.nerd.trampoline.HandleSendTo#getDescription()
     */
    @Override
    public String getDescription() {
        return "send to surface";
    }

    // ------------------------------------------------------------------------
    /**
     * Return a safe location to put the player, based on their starting
     * location.
     * 
     * @param loc the player's current location.
     * @param logger for logging to console.
     * @return a safe location to stand.
     */
    protected Location findSafeLocation(Location loc, Logger logger) {
        // Set up to step towards (0,0) along whichever axis
        // loc is furthest from (0,0).
        int dx = 0, dz = 0;
        int retries = _safeScanEnabled ? _safeScanTries : 0;
        if (retries > 0) {
            if (Math.abs(loc.getBlockX()) > Math.abs(loc.getBlockZ())) {
                dx = -_safeScanStep * Util.sign(loc.getBlockX());
                dz = 0;
            } else {
                dx = 0;
                dz = -_safeScanStep * Util.sign(loc.getBlockZ());
            }
        }

        for (;;) {
            Block ground = Util.highestUnpassableBlock(loc);

            // We need to scan or use the fail safe if there is nothing
            // to stand on in the column.
            if (ground.getType() != Material.VOID_AIR) {
                Block feet = ground.getRelative(0, 1, 0);
                Material material = feet.getType();
                if (material != Material.VOID_AIR &&
                    material != Material.LAVA) {
                    return feet.getLocation();
                }
            }

            if (retries > 0) {
                loc.add(dx, 0, dz);
                --retries;
            } else {
                return failSafeLocation(loc.getWorld());
            }
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Return the fail safe location in the specified world, defaulting to atop
     * the highest block at (0,0) if no fail safe is configured.
     * 
     * @param world the world containing the location.
     * @return the fail safe location.
     */
    protected Location failSafeLocation(World world) {
        DestinationWorld destinationWorld = Trampoline.CONFIG.DESTINATION_WORLDS.get(world.getName());
        if (destinationWorld == null) {
            return Util.atop(world.getHighestBlockAt(new Location(world, 0, 0, 0)));
        } else {
            return Util.passableSpawnLocation(destinationWorld.getFailSafeLocation());
        }
    }

    // ------------------------------------------------------------------------
    /**
     * If true, the plugin will scan for a safe location if the player is in an
     * all void column or if placing them on top of the highest solid block
     * would put their feet in lava.
     */
    protected boolean _safeScanEnabled;

    /**
     * Distance to step, in blocks, when scanning for a safe spot.
     */
    protected int _safeScanStep;

    /**
     * Number of times to try finding a safe spot by scanning.
     */
    protected int _safeScanTries;
} // class HandleSendToSurface