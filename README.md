RF Rotors
========


----------


This is a mod for Minecraft that adds Redstone Flux generating windmills and watermills.
This branch is for the **1.7.10** version of Minecraft.  Uses [Thermal Expansion's](https://minecraft.curseforge.com/projects/thermalexpansion) materials for its recipes. See NEI for recipes and other specifics.


Attribution
-----------
This is a fork of RF Windmills that was originally written by [Piepenguin](https://github.com/dbMansfield) in 2015.  The original Piepenguin's rfwindmills is located [here](  https://github.com/dbMansfield/rfwindmill).  So much of the code has changed that it is barely recognizable.


Changes from the original work
------------------------------
 Much of the code has been reworked.
- Added configuration options for windmill height settings and wind power in the nether and the end.
- Rewrote config file handler to use reasonable limits.
- Created a rotor interface(IRotor) for the rotors.
- Coded the generator to be more generic and accept an IRotor interface from a TileEntity.
- Collapsed the 4 tiers of "windmill blocks" into a single "rotary generator".
- Improved texture for generator block.  Now has direction.
- Improved performance of wind "tunnel" calculation.
- Wind speed varies throughout the game day through a perlin noise function of time.
- Random Wind gusts.
- Completely redone rotor models and textures while remaining true to minecraft stylization.
- Original 4 wind rotors removed and replaced with:
	- 7x7 Sail Rotor(like a classic 4 blade dutch windmill)
	- 7x7 Modern Rotor(resembles modern day 3 blade wind turbines)
	- 5x5 Wooden Water Wheel Rotor(Destroyed by hot fluids)
	- 5x5 Iron Water Wheel Rotor(Lava and High Temp fluid resistant)
- Rotors are more real
	- Added crude physics collision and entity damage to rotors.
	- Custom damage and death message for being killed by a rotor.
	- Added render bounds to rotor block so block doesn't disappear when nearly off the 
	- screen.
	- Added subtick interpolation to render code for smoother rotating animations.
	- Sneak-rightclick with a Buildcraft compatible wrench to remove a rotor from a generator.
- Retextured Items.
- Overhauled recipes.
	- New original metal Aerochromel alloy blend and ingot created for crafting Modern Rotor.
	- Removed fallback recipes for buildcraft. This mod now unapologetically requires Thermal Expansion and Thermal Foundation.
- Removed hand crank capability(May re-add later if it's worth it).


Bugs
----
If you find a bug, please report it on the issue tracker!


Contributing
------------
Gradle files are included in the repo for your convenience. Clone RFRotors and set up the workspace depending on your IDE. e.g.

gradlew setupDecompWorkspace idea --refresh-dependencies
gradlew setupDecompWorkspace eclipse --refresh-dependencies
Compilation should be as easy as

gradlew build
New features and bug fixes (unless urgent) should be developed off of the main dev branch and not from master, which is for stable releases only.

Code is licensed under the GPL v3 so feel free to fork, fix, and include in modpacks without asking permission.
