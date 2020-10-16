# Compact Machines 3
Minecraft Mod. Adds one simple game mechanic: Small rooms inside of blocks.

You can grab the latest build (tagged) in the releases tab.

## Compact Machines

These are the main component of this mod and allow you to build complicated machine contraptions and hide them within a single Compact Machine block.

#### Sizes
There are 6 different sizes of machines:

| Name                        | Description  |
| ----------------------------| -------------|
| Tiny                        | 3x3x3        |
| Small                       | 5x5x5        |
| Normal                      | 7x7x7        |
| Large                       | 9x9x9        |
| Giant                       | 11x11x11     |
| Maximum                     | 13x13x13     |

#### Personal Shrinking Device

This little tool allows you to enter your Compact Machines. Right clicking a Compact Machine world will teleport you inside.

#### Spawn location

By default, you spawn in the center of the room. You can change this by shift-right-clicking a Personal Shrinking Device at the location you want to set the spawn to.

Make sure not to obstruct the spawn location, there is currently no mechanic to automatically search for a better spawn location. You don't want to have to break the blocks you spawn in.

#### Recovery

If you accidentally deleted your Compact Machines block with all your stuff still inside the Machine it can be recovered using the ```/compactmachines machines view``` command.

It allows browsing through all existing Compact Machines and  provides a button to give yourself a Compact Machine Block linked to the viewed machine. Be aware: this breaks already existing links, i.e. if the machine should already exist somewhere it is being disconnected from the room.

This command only works for server operators.


#### Technical aspects

- The inside of Compact Machines are rooms in another dimension.
- The rooms in the other dimension are automatically chunk-loaded when the Compact Machine itself is chunk-loaded.
- They only trigger chunk reloads when a block next to one of the tunnels changes.
  There are no redstone signals being transferred between the tunnels and machine block.
- There are two kinds of Compact Machine Wall blocks. The actual machines are made out of unbreakable blocks (you can still break them in Creative mode while holding a Personal Shrinking Device), while the machine structures you build for crafting are made out of breakable Wall blocks.
- Right clicking a Compact Machine renders the contents of the machine in a GUI. On multiplayer games this is handled by sending the chunk contents to the client every time he opens the GUI. There is currently no auto-refresh of the machines' content.
- Compact Machines can be nested.
- Compact Machines spawn using a spiral algorithm in the Compact Machine dimension at y=60. This means they all fit in exactly one chunk.
- Mobs and animals will spawn inside of machine blocks if the conditions are right,
  e.g. light-level for mobs, grass for animals...
- Players cannot leave the machine block they should currently be in according to the last one they've entered using a Personal Shrinking Device. Trying to enter a Compact Machines with other means results in the player being thrown out of the machine and getting some bad status effect applied. They might even die.
- If players die without a bed in the CM dimension or with the `allowRespawning` config option disabled, they'll be teleported out of the machine dimension.

## Tunnels

Use these inside your Compact Machine room to create connections to the outside faces of your Compact Machine blocks. You can place them by right-clicking one of the wall blocks inside your Compact Machine with a Tunnel Tool.

By right-clicking them again you can cycle through the outside faces it is connected to - The One Probe helps with that by showing the face as well.

No face can be connected to twice, which means you can place a total of six tunnels anywhere in your Machine.

### Redstone Tunnels

They are a separate system, meaning you can place another 6 of them, but they only transfer redstone signals. They are also not bidirectional, i.e. you have to specify whether it is an input or output tunnel. You can change their mode by hitting the colored indicator square in the top right corner.

#### Compatibility

Tunnels are basically proxy blocks utilizing Forge Capabilities to provide a connection between the two blocks. This means that TileEntities using capabilities to talk to each other will work out of the box, this includes e.g. items, fluids and forge energy.

Other systems, usually multiblock-structures like cables and pipes, most of the time require custom implementations to work properly. 

This is why e.g. EnderIO conduits and Applied Energistics connections do not work. This might change in the future when they either switch to a fully capability-based system or when this mod adds compatibility layers itself. Both are lots of work, so don't expect this to happen.

Since this is asked very frequently:
- Refined Storage is supported directly since they are using capabilities.
- There is a AE2 addon mod called
  [ME Capability Adapter](https://minecraft.curseforge.com/projects/capability-adapter)
  that adds a capability proxy block.

## Miniaturization Crafting
See the [Compact Crafting] mod instead.

#### Setup

You will require 4 Miniaturization Field Projectors placed in a cross shape creating an odd-sized cube structure. Right click a Field Project to let it tell you where you need to place the next projector - missing locations will be highlighted in the world.

You might have to dig out the floor or place the projectors on pedestals.

Once you placed the projectors correctly and no blocks are obstructing the miniaturization
field, the field should visualize.

Watch this (outdated) youtube video for a basic crafting example:

[![Watch video on youtube](https://img.youtube.com/vi/p-F8ScV3z4U/0.jpg)](https://www.youtube.com/watch?v=p-F8ScV3z4U)

You can disable the field projection by applying a redstone signal to any of the projectors.


#### Creating custom recipes
If you're looking for the Miniaturization Crafting system, take a look at [Compact Crafting] instead.


## World Gen

"Broken" Compact Machines sometimes generate in the overworld. Harvest their blocks to use for your own machines.

This can be disabled in the config files.

[Compact Crafting]: https://github.com/robotgryphon/CompactCrafting
