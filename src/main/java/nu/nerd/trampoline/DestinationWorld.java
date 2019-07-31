package nu.nerd.trampoline;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

// ----------------------------------------------------------------------------
/**
 * Records the details of worlds that are teleport targets.
 */
public class DestinationWorld {
    // ------------------------------------------------------------------------
    /**
     * Load details of the world from the specified configuration section.
     * 
     * @param section the section.
     * @param logger used to log errors.
     * @return true if the world was loaded successfully.
     */
    @SuppressWarnings("unchecked")
    public boolean load(ConfigurationSection section, Logger logger) {
        _worldName = section.getName();

        ConfigurationSection failSafeLocation = section.getConfigurationSection("fail-safe-location");
        _failSafeX = failSafeLocation.getInt("x");
        _failSafeY = failSafeLocation.getInt("y");
        _failSafeZ = failSafeLocation.getInt("z");

        List<Map<?, ?>> exclusionZones = section.getMapList("exclusion-zones");
        if (exclusionZones != null) {
            for (Map<?, ?> map : exclusionZones) {
                MemoryConfiguration zoneConfig = new MemoryConfiguration();
                zoneConfig.addDefaults((Map<String, Object>) map);
                ExclusionZone exclusionZone = new ExclusionZone();
                if (exclusionZone.load(zoneConfig, logger)) {
                    _exclusionZones.add(exclusionZone);
                } else {
                    logger.severe(_worldName + " could not load an exclusion zone.");
                }
            }
        }
        return true;
    }

    // ------------------------------------------------------------------------
    /**
     * Return a description of this destination world.
     * 
     * @return the description.
     */
    public String getDescription() {
        String exclusions = _exclusionZones.size() != 0 ? " exclude " +
                                                          _exclusionZones.stream()
                                                          .map(ExclusionZone::getDescription)
                                                          .collect(Collectors.joining(", "))
                                                        : "";
        return exclusions + " failsafe (" + _failSafeX + "," + _failSafeY + "," + _failSafeZ + ")";
    }

    // ------------------------------------------------------------------------
    /**
     * Return the fail-safe location in the world.
     * 
     * @return a guaranteed safe location in the world.
     */
    public Location getFailSafeLocation() {
        World world = getWorld();
        return new Location(world, _failSafeX, _failSafeY, _failSafeZ);
    }

    // ------------------------------------------------------------------------
    /**
     * Return true if this world allows the player to teleport into it at the
     * specified location.
     * 
     * @param loc the location.
     * @return true if loc is an allowed entry point.
     */
    public boolean allowsTeleportTo(Location loc) {
        return !_exclusionZones.stream().anyMatch(zone -> zone.contains(loc));
    }

    // ------------------------------------------------------------------------
    /**
     * Return the corresponding world.
     * 
     * @return the world.
     */
    public World getWorld() {
        return Bukkit.getWorld(_worldName);
    }

    // ------------------------------------------------------------------------
    /**
     * The name of the world.
     */
    protected String _worldName;

    /**
     * X, Y and Z coordinates of the fail safe location.
     */
    protected int _failSafeX, _failSafeY, _failSafeZ;

    /**
     * Zones that the player is not allowed to teleport into.
     */
    protected List<ExclusionZone> _exclusionZones = new ArrayList<>();
} // class DestinationWorld