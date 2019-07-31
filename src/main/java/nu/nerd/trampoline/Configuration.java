package nu.nerd.trampoline;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

// ----------------------------------------------------------------------------
/**
 * Handles configuration loading.
 */
public class Configuration {
    /**
     * Period in ticks between checks of player locations.
     */
    public int CHECK_TICKS;

    /**
     * Y coordinate at or below which potion effects are applied.
     */
    public int EFFECT_Y;

    /**
     * Y coordinate at or below which the player will teleport.
     */
    public int TELEPORT_Y;

    /**
     * Y coordinate at or below which to teleport non-player entities.
     */
    public int ENTITY_TELEPORT_Y;

    /**
     * Minimum distance from the (vanilla) world border that players can
     * teleport to when teleporting to the sky in any world.
     */
    public int WORLD_BORDER_BUFFER;

    /**
     * Map from world name to handler that sends the player somewhere.
     */
    public Map<String, HandleSendTo> SEND_TO_HANDLERS = new HashMap<>();

    /**
     * Map from world name to details of the world when used as a teleport
     * destination.
     */
    public Map<String, DestinationWorld> DESTINATION_WORLDS = new HashMap<>();

    // ------------------------------------------------------------------------
    /**
     * Reload the configuration.
     * 
     * @param logged if true, configuration settings are logged to the console.
     */
    public void reload(boolean logged) {
        Trampoline.PLUGIN.reloadConfig();
        FileConfiguration config = Trampoline.PLUGIN.getConfig();
        Logger logger = Trampoline.PLUGIN.getLogger();

        CHECK_TICKS = Math.max(1, config.getInt("check-ticks"));
        EFFECT_Y = config.getInt("effect-y");
        TELEPORT_Y = config.getInt("teleport-y");
        ENTITY_TELEPORT_Y = config.getInt("entity-teleport-y");
        WORLD_BORDER_BUFFER = config.getInt("world-border-buffer");

        SEND_TO_HANDLERS.clear();
        ConfigurationSection exitSection = config.getConfigurationSection("exit");
        for (String worldName : exitSection.getKeys(false)) {
            ConfigurationSection handlerSection = exitSection.getConfigurationSection(worldName);
            String sendTo = handlerSection.getString("send-to");
            HandleSendTo handler = null;
            if (sendTo.equals("sky")) {
                handler = new HandleSendToSky();
            } else if (sendTo.equals("surface")) {
                handler = new HandleSendToSurface();
            }

            if (handler == null) {
                logger.severe("Unsupported send-to action \"" + sendTo + "\" for world \"" + worldName + "\".");
            } else {
                if (handler.load(handlerSection, logger)) {
                    SEND_TO_HANDLERS.put(worldName, handler);
                }
            }
        }

        DESTINATION_WORLDS.clear();
        ConfigurationSection enterSection = config.getConfigurationSection("enter");
        for (String worldName : enterSection.getKeys(false)) {
            DestinationWorld destinationWorld = new DestinationWorld();
            if (destinationWorld.load(enterSection.getConfigurationSection(worldName), logger)) {
                DESTINATION_WORLDS.put(worldName, destinationWorld);
            }
        }

        if (logged) {
            logger.info("CHECK_TICKS: " + CHECK_TICKS);
            logger.info("EFFECT_Y: " + EFFECT_Y);
            logger.info("TELEPORT_Y: " + TELEPORT_Y);
            logger.info("ENTITY_TELEPORT_Y: " + ENTITY_TELEPORT_Y);
            logger.info("WORLD_BORDER_BUFFER: " + WORLD_BORDER_BUFFER);

            for (Map.Entry<String, HandleSendTo> sendToHandler : SEND_TO_HANDLERS.entrySet()) {
                logger.info("Leaving " + sendToHandler.getKey() + ", " + sendToHandler.getValue().getDescription());
            }

            for (Map.Entry<String, DestinationWorld> destination : DESTINATION_WORLDS.entrySet()) {
                logger.info("Entering " + destination.getKey() + "," + destination.getValue().getDescription());
            }
        }
    } // reload

    // ------------------------------------------------------------------------
} // class Configuration