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

## Building

- Currently you need https://github.com/KrisBichocolate/fantasy/tree/pwa-v1.21.1 checked out into ../fantasy
- Build with the gradle wrapper

## License & Credits

Multiworld is licensed under the terms of the [LGPL v3](LICENSE).

Note: Multiworld makes use of the Fantasy library by NucleoidMC for creation of runtime worlds, (also LGPLv3).
