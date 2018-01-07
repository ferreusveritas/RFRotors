package com.ferreusveritas.rfrotors;

import com.ferreusveritas.rfrotors.blocks.BlockGenerator;
import com.ferreusveritas.rfrotors.blocks.BlockRotor;
import com.ferreusveritas.rfrotors.lib.ModConfiguration;

import net.minecraft.block.Block;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Creates {@link Block} instances of all the blocks in the mod.
 */
public class ModBlocks {

	public static Block generatorBlock;
	public static Block rotorBlock;

	/**
	 * Create {@link Block} instances of all the blocks in the mod.
	 */
	public static void preInit() {
		//float efficiency = ModConfiguration.getGeneratorEfficiency();
		int energyStorage = ModConfiguration.getGeneratorEnergyStorage();
		generatorBlock = new BlockGenerator("generatorBlock", energyStorage);
		rotorBlock = new BlockRotor();
		
	}
	
	public static void register(IForgeRegistry<Block> registry) {
		registry.register(generatorBlock);
		registry.register(rotorBlock);
	}
}
