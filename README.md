# Trampoline

Bounces players out of the void back onto solid ground.


## Features

Trampoline implements two options for handling players that fall into the void,
as specified by the `send-to` option in the configuration section corresponding
to the player's current world. In both cases, the objective is to prevent
players from suffocating in the void.

 * When a player falls into the void, they can be teleported to a random 
   location in a specified world (`send-to: sky`).
   * The destination world and Y coordinate are configurable.
   * The X and Z coordinates are randomly selected within the vanilla world
     border of the destination world, backed off from the border by a 
     configurable buffer.
   * The plugin will make up to 10 attempts to avoid locations within a list of
     circular exclusion zones, specified as centre (X,Z) and radius. If the
     previous 9 attempts have all failed to avoid the exclusion zones, the
     coordinates chosen on the 10th attempt will be used as if the exclusion
     zones do not exist.
   * A configurable set of entity types will also be teleported according to the
     same rules. By default, horses are handled in this way.
   * When the player falls into the void, potion effects are applied to them.
   * A randomly selected message will be sent to the player.

 * Alternatively, players can be placed back on the ground in their current 
   world (`send-to: surface`). This is not so much a game mechanic as it is
   dealing with impossible physics glitches like unloaded chunks. Worlds
   with solid bedrock should be configured in this way.
   * If configured, the plugin will avoid placing the player in a surface
     location where they would immediately fall in lava. The plugin will instead
     scan locations on the axis (North-South or East-West) for which the player
     is furthest from the origin, moving towards the origin, looking for a 
     non-lava block to place them on.
   * If the `safe-scan` option is enabled and the configured number of `tries`
     is exhausted, the player is teleported to a configured fail-safe location
     (typically near the origin of the world).
   * If the player is found to be in spectator mode, their gamemode will be
     set back to survival, on the principle that their gamemode was the cause
     of the void fall.


## Configuration
### General Settings

| Setting | Default | Description |
| :--- | :--- | :---
| `check-ticks` | 4 | The period in ticks between checks of player and entity locations. |
| `effect-y` | -20 | The Y coordinate below which potion effects are applied to players. |
| `teleport-y` | -40 | The Y coordinate below which the player is teleported to the destination world. |
| `entity-teleport-y` | -30 | The Y coordinate below which configured entity types are teleported (from `send-to: sky` worlds only). |
| `world-border-buffer` | 50 | The buffer zone at the world border into which players cannot be teleported. |


### Configuration Sections

After the general settings, the configuration file, `config.yml` contains two
major dictionary sections: `exit:` and `enter:`.

The `exit:` section has an entry for each world where some action should be
performed on a player when falling into the void in that world. The entry is
named after the world. The entry configures the handling discussed in the
*Features* section, for that specific world.

Each `exit:` sub-section has a child section named either `surface` or `sky`.
In fact, a given world can contain both of these sections. Only the section
named in the `send-to:` configuration setting for the world is consulted. 

The other top-level section, `enter:`, has an entry named after each world that
specifies the fail-safe location in that world, and the list of exclusion zones
that are considered when processing `send-to: sky`.


### Exit: Teleporting the Player to a Location in the Sky

The configuration settings for `send-to: sky` are described using the default
configuration as an example:

```
exit:
  world_the_end:
    enabled: true
    send-to: sky
    sky:
      world: world
      y: 500
      messages:
      - '&3You fell out of the sky!'
      - '&3Whoopsie!'
      effects:
      - ==: PotionEffect
        effect: 15
        duration: 200
        amplifier: 3
        ambient: true
        has-particles: false
        has-icon: false
      - ==: PotionEffect
        effect: 9
        duration: 300
        amplifier: 1
        ambient: true
        has-particles: false
        has-icon: false
      teleport-mobs:
      - HORSE
      - SKELETON_HORSE
      - ZOMBIE_HORSE
```

 * The configuration above describes the processing applied when a player or
   entity falls out of `world_the_end`.
 * Processing is `enabled` (true). If false, the player will simply fall into
   the void and start taking suffocation damage at about Y -65 (vanilla
   behaviour in 1.14).
 * The player is teleported to Y 500 in the world named `world` (the overworld).
 * A randomly selected message (from a list of two) is sent to the player.
 * Both of the two potion effects (blindness and nausea for 200 and 300 ticks,
   respectively) are applied to the player.
 * Only `HORSE`, `SKELETON_HORSE` and `ZOMBIE_HORSE` in `world_the_end` are
   monitored for void falls.


### Exit: Teleporting the Player to the Surface

The configuration settings for `send-to: surface` are described using the default
configuration as an example:

```
  world:
    enabled: true
    send-to: surface
    surface:
      safe-scan:
        enabled: true
        step: 8
        tries: 5
```

 * The configuration above describes the processing applied when a player or
   entity falls out of `world` (the overworld).
 * Processing is `enabled` (true). If false, the player will simply fall into
   the void.
 * The plugin will scan for a safe location to stand the player such that they
   are not in or directly above lava.
 * The scan will check up to 5 locations, 8 blocks apart each time.


### Enter Configuration Section

The configuration settings applied when a player teleports into or around a
world are illustrated by an example from the default configuration:

```
enter:
  world:
    fail-safe-location:
      x: 0
      y: 70
      z: 0
    exclusion-zones:
    - x: 0
      z: 0
      radius: 500
```

 * The settings affect entry and teleport within the overworld (`world`).
 * The fail-safe location is (0,70,0) within that world.
 * A circle of 500 blocks centred on (0,0) cannot be selected as a teleport
   destination by `send-to: sky`. More zones can be added.


## Commands

 * `/trampoline help` - Show usage help. Equivalent to `/help /trampoline`.  
 * `/trampoline reload` - Reload the configuration.


## Permissions

 * `trampoline.console` - Permission to administer the plugin (run console 
   commands).

