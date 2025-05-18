# Patchwork Adventures Multiworld

This is a heavily modified (and strictly incompatible!) fork of https://github.com/IsaiahMC/multiworld .

Main changes:
- New clone, delete, load, unload commands
- Fixed storage of world configuration: previously it was stored
  per-minecraft instance, but it has to be per-save
- Improved create command: can now specify preset for world
- Each world now stores if it's currently loaded or not
- Improved list command: shows unloaded worlds
- Make commands usable without a player entity (on permission level 4)
- Drop compatibility for Minecraft versions before v1.21.0
- Don't force java 8 compatibility

Warts:
- Only Minecraft 1.21.1 with Fabric is supported!
- Should really change the name...
- Builds against a fork of fantasy in ../fantasy, because the version
  of fantasy for 1.21.1 does not properly restore forceloaded chunks

## Commands

- `/mw list`
  Displays a list of all loaded and known worlds.

- `/mw tp <id>`
  Teleports the player to the spawn of the world identified by `<id>`.

- `/mw spawn`
  Teleports the player to the spawn point of the current world.

- `/mw setspawn`
  Sets the spawn point of the current world to the player's current position.

- `/mw gamerule <rule> <value>`
  Changes a game rule for the current or specified world. Note that `/gamerule` does not work for custom dimensions!

- `/mw difficulty <value> [world id]`
  Sets the difficulty level for a specified world (or the current one, if no ID is given). Note that the normal difficulty command does not work for custom dimensions!

- `/mw create <id> <world_preset> [<world_preset_dimension>]`
  Creates a new world with the specified ID, preset, and preset dimension name.
  The dimension name defaults to `minecraft:overworld`.

  Example presets:

  - `minecraft:normal minecraft:overworld` - the default overworld
  - `minecraft:normal minecraft:the_nether` - the default nether
  - `minecraft:flat` - flat world

- `/mw clone <existing id> <new id>`
  Creates a duplicate of an existing world under a new ID. The existing world does not need to be loaded, the newly created world will be loaded.

- `/mw delete <id>`
  Permanently deletes the specified world and all of its files. This action is irreversible and should be used with caution.

- `/mw load <id>`
  Loads the world with the specified ID.

- `/mw unload <id>`
  Unloads the specified world, freeing up server resources.


## Building

- Currently you need https://github.com/KrisBichocolate/fantasy/tree/pwa-v1.21.1 checked out into ../fantasy
- Build with the gradle wrapper

## License & Credits

Multiworld is licensed under the terms of the [LGPL v3](LICENSE).

Note: Multiworld makes use of the Fantasy library by NucleoidMC for creation of runtime worlds, (also LGPLv3).
