package nu.nerd.trampoline;

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

// ----------------------------------------------------------------------------
/**
 * Represents a circular area of the world that the player cannot teleport into
 * when falling through the void into the sky of another world.
 */
public class ExclusionZone {
    // ------------------------------------------------------------------------
    /**
     * Load this zone from the specified section.
     * 
     * @param section the configuration section.
     * @param logger used to log errors.
     */
    public boolean load(ConfigurationSection section, Logger logger) {
        _x = section.getInt("x");
        _z = section.getInt("z");
        _radius = section.getInt("radius");
        return true;
    }

    // ------------------------------------------------------------------------
    /**
     * Return true if the zone contains the specified location.
     * 
     * @param loc the location.
     * @return true if loc is in the zone.
     */
    public boolean contains(Location loc) {
        double dx = loc.getX() - _x;
        double dz = loc.getZ() - _z;
        return Math.sqrt(dx * dx + dz * dz) <= _radius;
    }

    // ------------------------------------------------------------------------
    /**
     * Return a human-readable description of this zone.
     * 
     * @return a human-readable description of this zone.
     */
    public String getDescription() {
        return "within " + _radius + " blocks of (" + _x + "," + _z + ")";
    }

    // ------------------------------------------------------------------------
    /**
     * Centre x and z of the zone.
     */
    protected int _x, _z;

    /**
     * Circular radius of the zone in blocks.
     */
    protected int _radius;
} // class ExclusionZone