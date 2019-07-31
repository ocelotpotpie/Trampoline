package nu.nerd.trampoline;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.block.Block;

// ----------------------------------------------------------------------------
/**
 * General utility methods.
 */
public class Util {
    // ------------------------------------------------------------------------
    /**
     * Return a random integer in the range [0, max - 1].
     * 
     * @param min the minimum result (inclusive).
     * @param max the maximum result (inclusive).
     * @return a random integer in the range [0, max - 1].
     */
    public static int randomInt(int max) {
        return _random.nextInt(max);
    }

    // ------------------------------------------------------------------------
    /**
     * Return a random integer in the range [min, max].
     * 
     * @param min the minimum result (inclusive).
     * @param max the maximum result (inclusive).
     * @return a random integer in the range [min, max].
     */
    public static int randomInt(int min, int max) {
        return min + _random.nextInt(max - min + 1);
    }

    // ------------------------------------------------------------------------
    /**
     * Return the sign of x as +1 for positive x, -1 for negative x, and 0 when
     * x is 0.
     * 
     * @return the sign of x.
     */
    public static int sign(int x) {
        return x < 0 ? -1 : (x > 0 ? 1 : 0);
    }

    // ------------------------------------------------------------------------
    /**
     * Return the highest unpassable block at the specified location.
     * 
     * @param loc the location.
     * @return the highest solid block; or a block of type VOID_AIR if the
     *         location is passable to the void.
     */
    public static Block highestUnpassableBlock(Location loc) {
        Block highest = loc.getWorld().getHighestBlockAt(loc);
        while (highest.getY() >= 0 && highest.isPassable()) {
            highest = highest.getRelative(0, -1, 0);
        }
        return highest;
    }

    // ------------------------------------------------------------------------
    /**
     * Return the location on top of the specified block.
     * 
     * @param block the block,
     * @return the location of the block.
     */
    public static Location atop(Block block) {
        return block.getRelative(0, 1, 0).getLocation();
    }

    // ------------------------------------------------------------------------
    /**
     * Return the location centred XZ in the block.
     * 
     * @param loc a location.
     * @return the location of centre of the block in the XZ plane.
     */
    public static Location centredXZ(Location loc) {
        return loc.getBlock().getLocation().add(0.5, 0, 0.5);
    }

    // ------------------------------------------------------------------------
    /**
     * Return a location at the same X and Z as the argument, ensuring that it
     * is passable for the player's head and feet.
     * 
     * If the location argument satisfies that criteria, simply return that.
     * Otherwise, look for a passable location on top of the highest solid block
     * at the supplied X,Z coords.
     * 
     * @param loc the location to consider.
     * @return a passable location to spawn the player at.
     */
    public static Location passableSpawnLocation(Location loc) {
        Block feetBlock = loc.getBlock();
        Block headBlock = feetBlock.getRelative(0, 1, 0);
        if (!feetBlock.isPassable() || !headBlock.isPassable()) {
            loc = atop(highestUnpassableBlock(loc));
        }
        return loc;
    }

    // ------------------------------------------------------------------------
    /**
     * Format a location as a string, with floating point coordinates.
     * 
     * @param loc the location.
     * @return the formatted location.
     */
    public static String formatLocation(Location loc) {
        return String.format("(%s,%5.1f,%5.1f,%5.1f)",
                             loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
    }

    // ------------------------------------------------------------------------
    /**
     * Format a block's location as a string containing integer coordinates.
     * 
     * @param loc the location.
     * @return the formatted location.
     */
    public static String formatBlockLocation(Location loc) {
        return "(" + loc.getWorld().getName() + "," +
               loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + ")";
    }

    // ------------------------------------------------------------------------
    /**
     * For random number generation.
     */
    protected static Random _random = new Random();
} // class Util