# Compact Machines 3
Minecraft Mod. Adds one simple game mechanic: Small rooms inside of blocks.


## Compact Machines

These are the main component of this mod and they allow you to build complicated
machine contraptions and hide them within a single Compact Machine block.

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

#### Spawn location

By default you spawn in the center of the room. You can change this by shift-right-clicking
a Personal Shrinking Device at the location you want to set the spawn to.

Make sure not to obstruct the spawn location, there is currently no mechanic that automatically
searches a better spawn location. And you don't want to have to break the blocks you spawn in.

#### Technical aspects

- The inside of Compact Machines are rooms in another dimension (id=144 by default)
- The rooms in the other dimension are automatically chunk-loaded when the
  Compact Machine itself is chunkloaded
- They only trigger chunk reloads when a block next to one of the tunnels changes.
  There is also no redstone signals being transferred between the tunnels and the
  machine block.
- There are two kinds of Compact Machine Wall blocks. The actual machines are made
  out of unbreakable blocks (you can still break them in creative mode while holding
  a Personal Shrinking Device), while the machine structures you build for crafting
  are made out of breakable Wall blocks.
- Right clicking a Compact Machine renders the contents of the machine in a GUI.
  On multiplayer games this is handled by sending the chunk contents to the client
  every time he opens the GUI. There is currently no auto-refresh of the machines
  content.
- Compact Machines can be nested
- Compact Machines spawn along the x-axis in the Compact Machine dimension at y=40,
  x=1024*machine-id, z=0. This means they all fit in exactly one chunk.



## Tunnels

Use these inside your Compact Machine room to create connections to the outside faces
of your Compact Machine blocks. You can place them by right-clicking one of the wall
blocks inside your Compact Machine with a Tunnel Tool.

By right-clicking them again you can cycle through the outside faces it is connected
to - Waila/The One Probe helps with that by showing the face as well.

No face can be connected to twice, which means you can place a total of six tunnels
anywhere in your Machine.

#### Compatibility

Tunnels are basically proxy blocks utilizing Forge Capabilities to provide a connection
between the two blocks. This means that TileEntities using capabilities to talk to each
other will work out of the box, this includes e.g. items, fluids and forge energy.

Other systems, usually multiblock-structures like cables and pipes, most of the time
require custom implementations to work properly. This is why e.g. EnderIO conduits,
Refined Storage or Applied Energistics connections do not work. This might change in
the future when they either switch to a fully capability based system or when this mod
adds compatibility layers itself. Both are lots of work, so don't expect this to happen.



## Miniaturization Crafting

This is a crafting mechanic added to create the Compact Machine blocks and some of the
utility blocks and items it provides. This can also be easily extended by pack makers
for custom recipes.

#### Setup

You will require 4 Miniaturization Field Projectors placed in a cross shape creating a
odd-sized cube structure. Right click a Field Project to let it tell you where you
need to place the next projector - missing locations will be highlighted in the world.
You might have to dig out the floor or place the projectors on pedestals.

Once you placed the projectors correctly and no blocks are obstructing the miniaturization
field, the field should visualize.

Watch this youtube video for a basic crafting example:

[![Watch video on youtube](https://img.youtube.com/vi/p-F8ScV3z4U/0.jpg)](https://www.youtube.com/watch?v=p-F8ScV3z4U)

You can disable the field projection by applying a redstone signal to any of the projectors.


#### Creating custom recipes

Recipes are added by placing .json files in the config/compactmachines3/recipes folder.
- Only recipes in that folder are being loaded.
- The folder is being extracted from the jar only if it does not exist. This allows
  you to remove original recipes simply by removing the .json file.
- The format should be intuitive enough that it does not require further explanation.
  If it isn't feel free to open an issue.



## World Gen

"Broken" Compact Machines sometimes spawn in the overworld. Harvest their blocks to use for
your own usage.

This can be disabled in the config files.
