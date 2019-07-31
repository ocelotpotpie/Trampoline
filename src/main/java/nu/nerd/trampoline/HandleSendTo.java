package nu.nerd.trampoline;

import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

// ----------------------------------------------------------------------------
/**
 * The base of classes that handle sending the player somewhere when they fall
 * into the void.
 */
public abstract class HandleSendTo {
    // ------------------------------------------------------------------------
    /**
     * Return true if this handler is enabled.
     */
    public boolean isEnabled() {
        return _enabled;
    }

    // ------------------------------------------------------------------------
    /**
     * Configure the handler according to the specified section.
     * 
     * @param section the configuration section.
     * @param logger used to log errors.
     * @return true if the handler was loaded successfully.
     */
    public boolean load(ConfigurationSection section, Logger logger) {
        _enabled = section.getBoolean("enabled");
        return true;
    }

    // ------------------------------------------------------------------------
    /**
     * Handle player movement.
     * 
     * @param player the player.
     * @param logger used for logging.
     */
    public abstract void handle(Player player, Logger logger);

    // ------------------------------------------------------------------------
    /**
     * Return a human-readable description of the actions performed by this
     * handler.
     * 
     * @return the description as a string.
     */
    public abstract String getDescription();

    // ------------------------------------------------------------------------
    /**
     * True if this handler is enabled.
     */
    protected boolean _enabled;

} // class HandleSendTo