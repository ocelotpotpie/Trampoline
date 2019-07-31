package nu.nerd.trampoline;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

// --------------------------------------------------------------------------
/**
 * Handle sending the player into the sky of another world.
 */
public class HandleSendToSky extends HandleSendTo {
    // --------------------------------------------------------------------------
    /**
     * @see nu.nerd.trampoline.HandleSendTo#load(org.bukkit.configuration.ConfigurationSection,
     *      java.util.logging.Logger)
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean load(ConfigurationSection section, Logger logger) {
        super.load(section, logger);
        _sourceWorldName = section.getName();

        ConfigurationSection skySection = section.getConfigurationSection("sky");
        if (skySection != null) {
            _destinationWorldName = skySection.getString("world");
            if (_destinationWorldName == null) {
                logger.severe(section.getName() + " sends to unspecified world.");
            }
            _destinationY = skySection.getInt("y");
            if (_destinationY <= 0) {
                logger.severe(section.getName() + " sends to invalid Y coordinate, " + _destinationY + ".");
            }
            _messages = new ArrayList<>(skySection.getStringList("messages"));
            try {
                _potionEffects = (List<PotionEffect>) skySection.getList("effects");
            } catch (Exception ex) {
                logger.severe(section.getName() + " send to potion effects are invalid.");
            }
            _teleportedMobs.clear();
            for (String entityTypeName : skySection.getStringList("teleport-mobs")) {
                try {
                    _teleportedMobs.add(EntityType.valueOf(entityTypeName.toUpperCase()));
                } catch (IllegalArgumentException ex) {
                    logger.severe("Invalid entity type for teleportation: " + entityTypeName);
                }
            }
        }
        return _destinationWorldName != null && _destinationY > 0;
    }

    // --------------------------------------------------------------------------
    /**
     * @see nu.nerd.trampoline.HandleSendTo#handle(org.bukkit.entity.Player)
     */
    @Override
    public void handle(Player player, Logger logger) {
        Location loc = player.getLocation();
        if (loc.getY() <= Trampoline.CONFIG.EFFECT_Y) {
            // Re-adding the same effect for lower Y is a no-op.
            _potionEffects.stream().forEach(player::addPotionEffect);
        }

        if (loc.getY() <= Trampoline.CONFIG.TELEPORT_Y) {
            teleportEntity(player, logger);

            String message = _messages.get(Util.randomInt(_messages.size())).replace("&p", player.getName());
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Teleport entities that have fallen out of the world if they are eligible
     * for teleportation.
     * 
     * @param logger for logging to console.
     */
    public void teleportEntities(Logger logger) {
        if (_teleportedMobs.isEmpty()) {
            return;
        }

        World world = Bukkit.getWorld(_sourceWorldName);
        for (Entity entity : world.getEntities()) {
            if (_teleportedMobs.contains(entity.getType()) &&
                entity.getLocation().getY() <= Trampoline.CONFIG.ENTITY_TELEPORT_Y) {
                teleportEntity(entity, logger);
            }
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Teleport the specified entity from its current world to the destination
     * world.
     * 
     * @param entity the entity to teleport.
     * @param logger for logging to console.
     */
    protected void teleportEntity(Entity entity, Logger logger) {
        Location loc = entity.getLocation();
        DestinationWorld destinationWorld = Trampoline.CONFIG.DESTINATION_WORLDS.get(_destinationWorldName);
        World world = Bukkit.getWorld(_destinationWorldName);
        if (destinationWorld == null || world == null) {
            logger.severe("Can't send " + entity.getType() + " " + entity.getName() +
                          " to non-existent world " + _destinationWorldName + "!");
            return;
        }

        WorldBorder border = world.getWorldBorder();
        int radius = (int) border.getSize() / 2 - Trampoline.CONFIG.WORLD_BORDER_BUFFER;
        Location centre = border.getCenter();
        int centreX = centre.getBlockX();
        int centreZ = centre.getBlockZ();

        // Have a few tries to honour all the exclusions.
        // Don't care too much if we can't.
        Location destination = null;
        for (int i = 0; i < 10; ++i) {
            int x = Util.randomInt(centreX - radius, centreX + radius);
            int z = Util.randomInt(centreZ - radius, centreZ + radius);
            destination = Util.centredXZ(new Location(world, x, _destinationY, z));
            if (destinationWorld.allowsTeleportTo(destination)) {
                break;
            }
        }

        entity.teleport(destination);
        logger.info(entity.getName() + " (" + entity.getUniqueId().toString() + ") teleported from " +
                    Util.formatLocation(loc) + " to " +
                    Util.formatLocation(destination) + ".");
    }

    // ------------------------------------------------------------------------
    /**
     * @see nu.nerd.trampoline.HandleSendTo#getDescription()
     */
    @Override
    public String getDescription() {
        String potionEffects = _potionEffects.isEmpty() ? "no effects"
                                                        : _potionEffects.size() + " effects: " +
                                                          _potionEffects.stream()
                                                          .map(p -> p.getDuration() + " ticks " +
                                                                    p.getType().getName() + " x " +
                                                                    (p.getAmplifier() + 1))
                                                          .collect(Collectors.joining(", "));
        String messages = _messages.isEmpty() ? "no messages"
                                              : _messages.size() + ", messages:\n" +
                                                _messages.stream().map(b -> "    " + b).collect(Collectors.joining("\n"));
        String teleportedEntities = _teleportedMobs.stream().map(EntityType::name).collect(Collectors.joining(","));
        return "send to sky, Y" + _destinationY + ", along with [" + teleportedEntities +
               "], " + potionEffects + ", " + messages;
    }

    // ------------------------------------------------------------------------
    /**
     * The name of the world to monitor for void falls.
     */
    protected String _sourceWorldName;

    /**
     * Name of the world to send the player to.
     */
    protected String _destinationWorldName;

    /**
     * Destination Y coordinate to send the player to.
     */
    protected int _destinationY;

    /**
     * List of messages, one of which is randomly selected for broadcast when
     * the player falls into the void.
     */
    protected ArrayList<String> _messages = new ArrayList<>();

    /**
     * List of potion effects, all of which are applied to the falling player.
     */
    protected List<PotionEffect> _potionEffects = new ArrayList<>();

    /**
     * List of types of mobs to be teleported if they fall out of the world.
     */
    protected EnumSet<EntityType> _teleportedMobs = EnumSet.noneOf(EntityType.class);
} // class HandleSendToSky