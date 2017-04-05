#RF Rotors

A mod for Minecraft that adds Redstone Flux generating windmills inspired by IC2's equivalent.

This is a fork of RF Windmills that was originally written by Piepenguin in 2015.

Compatible with Minecraft version 1.7.10. Uses Thermal Expansion's materials for its recipes. See NEI for specifics.

###Bugs

If you find a bug, please report it on the issue tracker!

###Contributing

Gradle files are not included in the repo and must be downloaded from http://files.minecraftforge.net/ (the src version). Copy the gradle folder, gradlew, and gradlew.bat files into this repo and set up the workspace depending on your IDE. e.g.

gradlew setupDecompWorkspace idea --refresh-dependencies
gradlew setupDecompWorkspace eclipse --refresh-dependencies
Compilation should be as easy as

gradlew build
New features and bug fixes (unless urgent) should be developed off of the main dev branch and not from master, which is for stable releases only.

Code is licensed under the GPL v3 so feel free to fork, fix, and include in modpacks without asking permission.
