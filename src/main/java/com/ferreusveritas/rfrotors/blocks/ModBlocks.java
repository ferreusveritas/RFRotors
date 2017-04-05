package com.ferreusveritas.rfrotors.blocks;

import com.ferreusveritas.rfrotors.lib.ModConfiguration;

import net.minecraft.block.Block;

/**
 * Creates {@link Block} instances of all the blocks in the mod.
 */
public class ModBlocks {

    public static Block generatorBlock;
    public static Block rotorBlock;

    /**
     * Create {@link Block} instances of all the blocks in the mod.
     */
    public static void init() {
        float efficiency = ModConfiguration.getGeneratorEfficiency();
        int energyStorage = ModConfiguration.getGeneratorEnergyStorage();
        generatorBlock = new BlockGenerator("generatorBlock", energyStorage);
        rotorBlock = new BlockRotor();
    }
}
